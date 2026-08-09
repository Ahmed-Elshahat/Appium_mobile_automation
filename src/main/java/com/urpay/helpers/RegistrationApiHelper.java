package com.urpay.helpers;

import com.urpay.core.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Registration API Helper — provisions a brand-new, fully KYC-verified and active URPay
 * consumer entirely through the backend, for any POI type (NAT / IQA / BOR-Visitor).
 *
 * Replicates Katalon's {@code RegisterUrpayUser.toRegisterUrpayUser(idType)} end-to-end:
 *   1. pre-login                  → authentication/consumers/pre-login
 *   2. generateOTP (purpose 001)  → otp/generate          (returns otpReference + X-OTP-Token)
 *   3. verifyOTP   (otp 1234)     → otp/verify            (returns X-OTP-Token)
 *   4. seedTahaqoqInfo            → {simulator}/__admin/tahaqoq-info (Nafath/Tahaqoq verification)
 *   5. registration              → consumers/registration (creates the consumer)
 *   6. Oracle EPAY_PARTY seeding  → tier 5, KYC verified, names/DOB/nationality/address, ACTIVE
 *   7. login chain               → pre-login → otp → devices/register → consumers/login
 *
 * The new user's mobile and POI are randomly generated (Luhn-valid POI). The registered
 * passcode is {@code 2233} (the encrypted blob below is the same one Katalon registers with).
 *
 * Best-effort: every stage is guarded; on any failure the helper logs a warning and returns
 * {@code false} so a caller can decide how to proceed. All identifiers are written to the log
 * (console) as they are produced, so a created user can be reused/inspected manually.
 *
 * NOTE: the SIT API host, the Tahaqoq simulator and the Oracle DB are only reachable from the
 * internal network (VPN). This helper is not expected to run from a developer laptop.
 */
public final class RegistrationApiHelper {

    private static final Logger log = LoggerFactory.getLogger(RegistrationApiHelper.class);

    private static final String OTP_TOKEN_HEADER = "X-OTP-Token";
    private static final String DEVICE_TOKEN_HEADER = "X-Device-Token";
    private static final String SECURITY_TOKEN_HEADER = "X-Security-Token";

    /** Plaintext passcode that {@link #PASSCODE_2233_BLOB} encrypts — logged so the account is usable. */
    static final String PASSCODE_PLAINTEXT = "2233";

    /** Visitor (BOR) unverified date of birth used for age/validate + registration (from the BE report). */
    private static final String VISITOR_DOB = "1994-01-22";

    private static final Random RANDOM = new Random();

    private RegistrationApiHelper() {
    }

    /** Proof-of-identity type and the leading digit used when generating a POI number. */
    public enum PoiType {
        /** Saudi national ID. */
        NAT("NAT", '1'),
        /** Resident (Iqama). */
        IQA("IQA", '2'),
        /** Visitor (border number). */
        BOR("BOR", '3');

        private final String code;
        private final char prefix;

        PoiType(String code, char prefix) {
            this.code = code;
            this.prefix = prefix;
        }

        public String code() {
            return code;
        }

        public char prefix() {
            return prefix;
        }
    }

    // ── Public API ─────────────────────────────────────────────────

    /** Register a new National (NAT) consumer. */
    @Step("Register new NAT consumer via API")
    public static boolean registerNationalConsumer() {
        return register(PoiType.NAT);
    }

    /** Register a new Resident/Iqama (IQA) consumer. */
    @Step("Register new IQA consumer via API")
    public static boolean registerResidentConsumer() {
        return register(PoiType.IQA);
    }

    /** Register a new Visitor (BOR) consumer. */
    @Step("Register new Visitor (BOR) consumer via API")
    public static boolean registerVisitorConsumer() {
        return register(PoiType.BOR);
    }

    /**
     * Register a new National (NAT) consumer and return its login credentials so a caller can
     * drive a fresh UI login (e.g. a suite that must run against a brand-new, never-used account).
     *
     * @return the provisioned credentials, or {@code null} if the backend chain failed
     */
    @Step("Register new NAT consumer via API and return its credentials")
    public static Provisioned registerNationalAndReturn() {
        return registerAndReturn(PoiType.NAT);
    }

    /** Register a new Resident/Iqama (IQA) consumer and return its login credentials. */
    @Step("Register new IQA consumer via API and return its credentials")
    public static Provisioned registerResidentAndReturn() {
        return registerAndReturn(PoiType.IQA);
    }

    /** Register a new Visitor (BOR) consumer and return its login credentials. */
    @Step("Register new Visitor (BOR) consumer via API and return its credentials")
    public static Provisioned registerVisitorAndReturn() {
        return registerAndReturn(PoiType.BOR);
    }

    /**
     * Register a new consumer of the given POI type, seed full KYC in the DB and log it in.
     *
     * @param poiType NAT, IQA or BOR
     * @return {@code true} only if the whole chain (including the final login) succeeded
     */
    @Step("Register new {poiType} consumer via API (full flow)")
    public static boolean register(PoiType poiType) {
        return registerAndReturn(poiType) != null;
    }

    /**
     * Register a new consumer of the given POI type, seed full KYC in the DB and log it in,
     * returning the generated credentials (mobile / POI / passcode) for a subsequent UI login.
     *
     * @param poiType NAT, IQA or BOR
     * @return the provisioned credentials, or {@code null} if any stage of the chain failed
     */
    @Step("Register new {poiType} consumer via API (full flow, returns credentials)")
    public static Provisioned registerAndReturn(PoiType poiType) {
        return registerAndReturn(poiType, true);
    }

    /**
     * Register a new Default-tier (NAT) consumer but STOP before completing KYC: it registers, seeds
     * the DB and logs the user in, then returns WITHOUT calling the KYC / accept-terms APIs. Use this
     * to provision a fresh account that still needs to go through its in-app KYC / verification journey.
     *
     * @return the provisioned credentials, or {@code null} if the backend chain failed
     */
    @Step("Register new Default-tier consumer via API (stop before KYC)")
    public static Provisioned registerDefaultTierAndReturn() {
        return registerAndReturn(PoiType.NAT, false);
    }

    /**
     * Register a new consumer, seed the DB and log it in. When {@code doKyc} is {@code true} the
     * user's KYC is completed and the latest terms accepted after login; when {@code false} the flow
     * STOPS right after login (a Default-tier, not-yet-KYC'd account).
     *
     * @param poiType NAT, IQA or BOR
     * @param doKyc   whether to complete KYC + accept terms after login
     * @return the provisioned credentials, or {@code null} if any stage of the chain failed
     */
    @Step("Register new {poiType} consumer via API (doKyc {doKyc})")
    private static Provisioned registerAndReturn(PoiType poiType, boolean doKyc) {
        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("registration.baseUrl", "https://192.168.100.71:14301/walletapp/v1");

        String poiNumber = generatePoiNumber(prefixFor(poiType));
        String mobile = generateMobileNumber();

        log.info("=== Registering new {} consumer ===", poiType.code());
        log.info("  mobileNumber : {}", mobile);
        log.info("  poiNumber    : {}", poiNumber);
        log.info("  poiType      : {}", poiType.code());

        try {
            RestAssured.useRelaxedHTTPSValidation();

            preLogin(baseUrl, mobile, poiNumber, poiType.code());

            Response generate = generateOtp(baseUrl, mobile);
            String otpReference = generate.jsonPath().getString("body.otpReference");
            String genToken = generate.getHeader(OTP_TOKEN_HEADER);
            if (otpReference == null || genToken == null) {
                log.warn("Registration aborted: missing otpReference/token from generate-OTP");
                return null;
            }

            Response verify = verifyOtp(baseUrl, mobile, otpReference, genToken);
            String verifyToken = verify.getHeader(OTP_TOKEN_HEADER);
            if (verifyToken == null) {
                log.warn("Registration aborted: OTP verification returned no token (status {})",
                        verify.getStatusCode());
                return null;
            }

            seedTahaqoqInfo(poiNumber, mobile);

            // Default-tier NAT/IQA: ALSO seed the identity + DOB into Yakeen (in addition to the DB
            // DOB seed in activateOnlyInDb) so the date of birth is present in the national-identity
            // simulator too. Scoped to the default path (doKyc=false); the KYC'd path and BOR are
            // unaffected.
            if (!doKyc && poiType != PoiType.BOR) {
                seedYakeenInfo(poiNumber);
            }

            // Visitor (BOR) has an EXTRA step vs NAT/IQA (per BE report): POST /consumers/age/validate
            // returns an X-Verification-Token that the registration call must carry, and the registration
            // body includes the unverifiedDateOfBirth. NAT/IQA register without either.
            String verificationToken = null;
            String unverifiedDob = null;
            if (poiType == PoiType.BOR) {
                unverifiedDob = config.get("registration.visitor.dob", VISITOR_DOB);
                verificationToken = validateAge(baseUrl, unverifiedDob, poiType.code(), verifyToken);
                if (verificationToken == null) {
                    log.warn("Registration aborted: BOR age/validate returned no X-Verification-Token");
                    return null;
                }
            }

            Response registration = registerConsumer(baseUrl, mobile, otpReference, poiNumber,
                    poiType.code(), verifyToken, verificationToken, unverifiedDob);
            int regStatus = registration.getStatusCode();
            if (regStatus < 200 || regStatus >= 300) {
                log.warn("Consumer registration failed for {} (status {}): {}",
                        mobile, regStatus, registration.getBody().asString());
                return null;
            }
            log.info("Consumer registration succeeded for {} (status {})", mobile, regStatus);

            // Full flow seeds a verified identity + upgraded tier (5 for NAT/IQA, 11 for BOR). Default-tier
            // provisioning only ACTIVATES the consumer and leaves it at the registration tier (3), un-KYC'd.
            String partyId;
            if (doKyc) {
                String tierId = poiType == PoiType.BOR ? "11" : "5";
                partyId = seedConsumerInDb(poiNumber, tierId);
            } else {
                partyId = activateOnlyInDb(poiNumber);
            }
            log.info("  partyId      : {}", partyId);

            Session session = loginAndGetSession(baseUrl, mobile, poiNumber, poiType.code());
            if (session == null) {
                log.warn("=== Registration incomplete for {} | mobile {} | poi {} | login FAILED ===",
                        poiType.code(), mobile, poiNumber);
                return null;
            }

            // Full flow completes KYC + accepts the latest terms after login (BE report + Katalon
            // RegisterUrpayUser); KYC flips the consumer INACTIVE, so re-activate it afterwards.
            // Default-tier provisioning STOPS before KYC so the account still needs its in-app journey.
            if (doKyc) {
                completeKyc(baseUrl, session);
                acceptNewTerms(baseUrl, session);
                acceptPartyConsents(baseUrl, session, poiNumber, poiType.code(), partyId);
                reactivateInDb(mobile, partyId);
            } else {
                log.info("Stopping before KYC/terms (Default-tier account, not KYC-completed)");
            }

            logCredentials(poiType.code() + " CONSUMER CREDENTIALS", poiType.code(), mobile, poiNumber, partyId);
            log.info("=== Registration complete for {} | mobile {} | poi {} | partyId {} | consumerId {} "
                    + "| walletTier {} | login OK ===",
                    poiType.code(), mobile, poiNumber, partyId, session.consumerId, session.walletTier);
            return new Provisioned(mobile, poiNumber, poiType.code(), PASSCODE_PLAINTEXT,
                    session.consumerId, partyId, session.walletNumber, session.walletTier, session.fullName);
        } catch (Exception e) {
            log.warn("Registration via API failed for {} ({}): {}", mobile, poiType.code(), e.getMessage());
            return null;
        }
    }

    // ── API steps (mirror the Katalon keyword) ─────────────────────

    @Step("API pre-login")
    static Response preLogin(String baseUrl, String mobile, String poi, String poiType) {
        String endpoint = baseUrl + "/authentication/consumers/pre-login";
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"poi\":{\"poiNumber\":\"" + poi
                + "\",\"poiType\":\"" + poiType + "\"}}";
        logApiRequest("POST", endpoint, body);
        // Matches the BE report's pre-login header set: no X-Client-Secret, plus X-Forwarded-For.
        return baseHeaders(null, false)
                .header("X-Forwarded-For", "51.235.115.205")
                .header("Content-Type", "application/json")
                .body(body)
            .post(endpoint);
    }

    @Step("API generate OTP")
    static Response generateOtp(String baseUrl, String mobile) {
        String endpoint = baseUrl + "/otp/generate";
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"purpose\":\"001\"}";
        logApiRequest("POST", endpoint, body);
        return baseHeaders(null)
                .header("X-Forwarded-For", "1")
                .header("Content-Type", "application/json")
                .body(body)
            .post(endpoint);
    }

    @Step("API verify OTP")
    static Response verifyOtp(String baseUrl, String mobile, String otpReference, String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String endpoint = baseUrl + "/otp/verify";
        String otp = config.get("registration.otp", "1234");
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"otp\":\"" + otp + "\",\"otpReference\":\""
                + otpReference + "\",\"purpose\":\"001\"}";
        logApiRequest("POST", endpoint, body);
        return baseHeaders(otpToken)
                .header("X-Forwarded-For", "1")
                .header("Content-Type", "application/json")
                .body(body)
            .post(endpoint);
    }

    /** Seed the Tahaqoq/Nafath simulator so the POI passes ID verification during registration. */
    @Step("API seed Tahaqoq info (simulator)")
    public static Response seedTahaqoqInfo(String poiNumber, String mobile) {
        ConfigManager config = ConfigManager.getInstance();
        String simBaseUrl = config.get("registration.simBaseUrl",
                "https://neoleap-backend-simulator-sit.apps.ocpuat.neoleap.com.sa");
        String endpoint = simBaseUrl + "/__admin/tahaqoq-info";
        String apiKey = config.get("registration.simApiKey",
                "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G");
        String cookie = config.get("registration.simCookie", "");
        String body = "{}";
        logApiRequest("POST", endpoint,
            "{\"IDNumber\":\"" + poiNumber + "\",\"MobileNumber\":\"" + mobile + "\",\"body\":" + body + "}");
        RequestSpecification spec = baseHeaders(null)
            .header("X-Do-Not-Track", apiKey)
            .header("x-api-key", apiKey)
                .queryParam("IDNumber", poiNumber)
            .queryParam("MobileNumber", mobile);
        if (!cookie.isEmpty()) {
            spec = spec.header("Cookie", cookie);
        }
        return spec.post(endpoint);
    }

    /**
     * Seed the Yakeen simulator with the account's identity INCLUDING the date of birth, so a
     * default-tier NAT/IQA account also carries a DOB in the national-identity simulator (mirrors
     * the family-member Yakeen seed in {@code FamilyRegistrationApiHelper.seedYakeenInfo}).
     * birthDateG {@code 1999-05-30} / Hijri {@code 1420-05-08} match {@link #seedConsumerInDb}'s
     * DATE_OF_BIRTH ('30-MAY-99'). Both dates are configurable so a different DOB can be provisioned
     * without a code change.
     */
    @Step("API seed Yakeen info (identity + DOB) for poi {poiNumber}")
    public static Response seedYakeenInfo(String poiNumber) {
        ConfigManager config = ConfigManager.getInstance();
        String simBaseUrl = config.get("registration.simBaseUrl",
                "https://neoleap-backend-simulator-sit.apps.ocpuat.neoleap.com.sa");
        String endpoint = simBaseUrl + "/__admin/yakeen-info";
        String apiKey = config.get("registration.simApiKey",
                "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G");
        String cookie = config.get("registration.simCookie", "");
        String birthDateG = config.get("registration.default.birthDateG", "1999-05-30");
        String dateOfBirthH = config.get("registration.default.dateOfBirthH", "1420-05-08");
        String birthDateGIso = birthDateG.contains("T") ? birthDateG : birthDateG + "T00:00:00";
        String visaExpiryDate = config.get("registration.default.visaExpiryDate", "2025-08-21T00:00:00");
        String nationalityCode = config.get("registration.default.nationalityCode", "113");
        String nationalityDescAr = config.get("registration.default.nationalityDescAr", "المملكة العربية السعودية");
        String requestView = "{"
            + "\"nin\":\"" + poiNumber + "\","
            + "\"firstName\":\"أيمن\","
            + "\"fatherName\":\"عبدالإله\","
            + "\"grandFatherName\":\"إبراهيم\","
            + "\"familyName\":\"عباس\","
            + "\"englishFirstName\":\"Ayman\","
            + "\"englishSecondName\":\"Abdulailah\","
            + "\"englishThirdName\":\"Ibrahim\","
            + "\"englishLastName\":\"Abbas\","
            + "\"idExpiryDate\":\"2034-10-11T00:00:00\","
            + "\"dateOfBirthH\":\"" + dateOfBirthH + "\","
            + "\"birthDateG\":\"" + birthDateG + "\","
            + "\"gender\":\"M\","
            + "\"idExpirationDateH\":\"1456-07-28\","
            + "\"placeOfBirth\":\"الرياض\""
            + "}";
        logApiRequest("POST", endpoint, requestView);
        String body = "{"
            + "\"visaVisitorInfo\":{\"visaExpiryDate\":\"" + visaExpiryDate + "\"},"
            + "\"personBasicInfo\":{"
            + "\"birthDateG\":\"" + birthDateGIso + "\","
            + "\"familyName\":\"عباس\","
            + "\"familyNameT\":\"Abbas\","
            + "\"fatherName\":\"عبدالإله\","
            + "\"fatherNameT\":\"Abdulailah\","
            + "\"firstName\":\"أيمن\","
            + "\"firstNameT\":\"Ayman\","
            + "\"grandFatherName\":\"إبراهيم\","
            + "\"grandFatherNameT\":\"Ibrahim\","
            + "\"nationalityCode\":\"" + nationalityCode + "\","
            + "\"nationalityDescAr\":\"" + nationalityDescAr + "\","
            + "\"sexCode\":\"1\","
            + "\"sexDescAr\":\"ذكر\","
            + "\"convertDate\":{\"dateString\":\"" + dateOfBirthH + "\"}"
            + "}"
            + "}";
        logApiRequest("POST", endpoint, body);
        RequestSpecification spec = baseHeaders(null)
            .header("x-api-key", apiKey)
            .header("Content-Type", "application/json")
                .queryParam("nin", poiNumber)
                .queryParam("firstName", "أيمن")
                .queryParam("fatherName", "عبدالإله")
                .queryParam("grandFatherName", "إبراهيم")
                .queryParam("familyName", "عباس")
                .queryParam("englishFirstName", "Ayman")
                .queryParam("englishSecondName", "Abdulailah")
                .queryParam("englishThirdName", "Ibrahim")
                .queryParam("englishLastName", "Abbas")
                .queryParam("idExpiryDate", "2034-10-11T00:00:00")
                .queryParam("dateOfBirthH", dateOfBirthH)
                .queryParam("birthDateG", birthDateG)
                .queryParam("gender", "M")
                .queryParam("idExpirationDateH", "1456-07-28")
                .queryParam("placeOfBirth", "\u0627\u0644\u0631\u064A\u0627\u0636")
                .body(body);
        if (!cookie.isEmpty()) {
            spec = spec.header("Cookie", cookie);
        }
        return spec.post(endpoint);
    }

    /**
     * Seed the Nafath simulator so the POI passes Nafath identity verification during registration.
     * Uses a JSON body (not query params) matching the /__admin/nafath-info contract.
     * DOB Hijri is stored as an integer YYYYMMDD (e.g. 14200508 for 1420-05-08).
     */
    @Step("API seed Nafath info (simulator) for poi {poiNumber}")
    public static Response seedNafathInfo(String poiNumber) {
        ConfigManager config = ConfigManager.getInstance();
        String simBaseUrl = config.get("registration.simBaseUrl",
                "https://neoleap-backend-simulator-sit.apps.ocpuat.neoleap.com.sa");
        String endpoint = simBaseUrl + "/__admin/nafath-info";
        String apiKey = config.get("registration.simApiKey",
                "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G");
        String dobG   = config.get("registration.default.birthDateG", "1999-05-30");
        // Hijri DOB as integer YYYYMMDD — strip the dashes from the H date
        String dobHStr = config.get("registration.default.dateOfBirthH", "1420-05-08").replace("-", "");
        int dobH;
        try { dobH = Integer.parseInt(dobHStr); } catch (NumberFormatException e) { dobH = 14200508; }

        String body = "{"
                + "\"id\":\"" + poiNumber + "\","
                + "\"id_version\":null,"
                + "\"id_issue_date#g\":null,"
                + "\"id_issue_date#h\":null,"
                + "\"id_expiry_date#g\":\"2034-10-11\","
                + "\"id_expiry_date#h\":14560728,"
                + "\"card_issue_place#ar\":null,"
                + "\"card_issue_place#en\":null,"
                + "\"scenario\":\"Accepted\","
                + "\"first_name#ar\":\"أيمن\","
                + "\"father_name#ar\":\"عبدالإله\","
                + "\"grand_name#ar\":\"إبراهيم\","
                + "\"family_name#ar\":\"عباس\","
                + "\"first_name#en\":\"Ayman\","
                + "\"father_name#en\":\"Abdulailah\","
                + "\"grand_name#en\":\"Ibrahim\","
                + "\"family_name#en\":\"Abbas\","
                + "\"two_names#ar\":\"أيمن عباس\","
                + "\"two_names#en\":\"Ayman Abbas\","
                + "\"full_name#ar\":\"أيمن عبدالإله إبراهيم عباس\","
                + "\"full_name#en\":\"Ayman Abbas\","
                + "\"gender\":\"M\","
                + "\"dob#g\":\"" + dobG + "\","
                + "\"dob#h\":" + dobH + ","
                + "\"nationality\":113,"
                + "\"nationality#ar\":\"المملكة العربية السعودية\","
                + "\"nationality#en\":\"Kingdom of Saudi Arabia\","
                + "\"language\":\"A\""
                + "}";
            logApiRequest("POST", endpoint, body);
        return RestAssured.given()
                .header("X-Api-Key", apiKey)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(body)
                .post(endpoint);
    }

    /** Seed the Nafath ELM simulator for identity verification. */
    @Step("API seed NafathElm info (simulator) for poi {poiNumber}")
    public static Response seedNafathElmInfo(String poiNumber) {
        ConfigManager config = ConfigManager.getInstance();
        String simBaseUrl = config.get("registration.simBaseUrl",
                "https://neoleap-backend-simulator-sit.apps.ocpuat.neoleap.com.sa");
        String endpoint = simBaseUrl + "/__admin/nafathElm-info";
        String apiKey = config.get("registration.simApiKey",
                "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G");
        String cookie = config.get("registration.simCookie", "");
        String dobG = config.get("registration.default.birthDateG", "1999-05-30");
        int dobH = toHijriIntFromGregorianDate(dobG);
        String errorStatus = config.get("registration.nafathElm.errorStatus", "422-031-046");

        String body = "{"
                + "\"id\":" + poiNumber + ","
                + "\"scenario\":\"Completed\","
                + "\"error_status\":\"" + errorStatus + "\","
                + "\"first_name#ar\":\"أيمن\","
                + "\"father_name#ar\":\"عبدالإله\","
                + "\"grand_name#ar\":\"إبراهيم\","
                + "\"family_name#ar\":\"عباس\","
                + "\"first_name#en\":\"Ayman\","
                + "\"father_name#en\":\"Abdulailah\","
                + "\"grand_name#en\":\"Ibrahim\","
                + "\"family_name#en\":\"Abbas\","
                + "\"two_names#ar\":\"أيمن عباس\","
                + "\"two_names#en\":\"Ayman Abbas\","
                + "\"full_name#ar\":\"أيمن عبدالإله إبراهيم عباس\","
                + "\"full_name#en\":\"Ayman Abbas\","
                + "\"gender\":\"M\","
                + "\"dob#g\":\"" + dobG + "\","
                + "\"dob#h\":" + dobH + ","
                + "\"nationality\":113,"
                + "\"nationality#ar\":\"المملكة العربية السعودية\","
                + "\"nationality#en\":\"Kingdom of Saudi Arabia\","
                + "\"language\":\"A\""
                + "}";
            logApiRequest("POST", endpoint, body);
            RequestSpecification spec = RestAssured.given()
                .header("x-api-key", apiKey)
                .header("Content-Type", "text/plain")
                .body(body);
            if (!cookie.isEmpty()) {
                spec = spec.header("Cookie", cookie);
            }
            return spec.post(endpoint);
    }

    /** Convert Gregorian yyyy-MM-dd to Hijri YYYYMMDD integer for simulator fields like dob#h. */
    private static int toHijriIntFromGregorianDate(String dobG) {
        try {
            LocalDate gregorian = LocalDate.parse(dobG);
            HijrahDate hijri = HijrahDate.from(gregorian);
            return (hijri.get(java.time.temporal.ChronoField.YEAR_OF_ERA) * 10000)
                    + (hijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) * 100)
                    + hijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH);
        } catch (Exception e) {
            log.warn("Invalid birthDateG '{}' — falling back to default Hijri DOB 14200508", dobG);
            return 14200508;
        }
    }

    /** Backwards-compatible registration (NAT/IQA): no age-verification token, no unverified DOB. */
    static Response registerConsumer(String baseUrl, String mobile, String otpReference,                                             String poi, String poiType, String otpToken) {
        return registerConsumer(baseUrl, mobile, otpReference, poi, poiType, otpToken, null, null);
    }

    @Step("API consumer registration")
    static Response registerConsumer(String baseUrl, String mobile, String otpReference,
                                             String poi, String poiType, String otpToken,
                                             String verificationToken, String unverifiedDob) {
        String endpoint = baseUrl + "/consumers/registration";
        ConfigManager config = ConfigManager.getInstance();
        String passcodeBlob = config.get("registration.passcodeBlob", PASSCODE_2233_BLOB);
        StringBuilder body = new StringBuilder("{\"mobileNumber\":\"" + mobile + "\",\"otpReference\":\""
                + otpReference + "\",\"passCode\":\"" + passcodeBlob + "\",\"poi\":{\"poiNumber\":\"" + poi
                + "\",\"poiType\":\"" + poiType + "\"}");
        // Visitor (BOR) registration additionally sends the unverifiedDateOfBirth (BE report).
        if (unverifiedDob != null) {
            body.append(",\"unverifiedDateOfBirth\":\"").append(unverifiedDob).append("\"");
        }
        body.append("}");
        logApiRequest("POST", endpoint, body.toString());

        // No Thread.sleep (framework rule): instead retry to absorb the brief propagation
        // delay between the Tahaqoq seed and the registration becoming accepted.
        Response response = null;
        for (int attempt = 1; attempt <= 4; attempt++) {
            RequestSpecification spec = baseHeaders(otpToken)
                    .header("X-Longitude", "46.679297")
                    .header("X-Latitude", "24.705742")
                    .header("X-Forwarded-For", "1")
                    .header("Content-Type", "application/json");
            // Visitor (BOR) carries the token returned by /consumers/age/validate.
            if (verificationToken != null) {
                spec = spec.header("X-Verification-Token", verificationToken);
            }
            response = spec.body(body.toString())
                    .post(endpoint);
            int status = response.getStatusCode();
            if (status >= 200 && status < 300) {
                return response;
            }
            log.info("Registration attempt {} returned status {} — retrying", attempt, status);
        }
        return response;
    }

    /**
     * Visitor (BOR) age validation — {@code POST /consumers/age/validate}. Returns the
     * {@code X-Verification-Token} that the subsequent registration must carry (BE report step
     * absent for NAT/IQA).
     *
     * @return the verification token, or {@code null} if the call failed / returned no token
     */
    @Step("API validate age (Visitor/BOR)")
    static String validateAge(String baseUrl, String dateOfBirth, String poiType, String otpToken) {
        String endpoint = baseUrl + "/consumers/age/validate";
        String body = "{\"dateOfBirth\":\"" + dateOfBirth + "\",\"poiType\":\"" + poiType + "\"}";
        logApiRequest("POST", endpoint, body);
        Response resp = baseHeaders(otpToken)
                .header("X-Forwarded-For", "1")
                .header("Content-Type", "application/json")
                .body(body)
            .post(endpoint);
        String token = resp.getHeader("X-Verification-Token");
        log.info("Age validate ({}) status {} -> isAdult {} | verification token {}",
                poiType, resp.getStatusCode(), resp.jsonPath().getString("body.isAdult"),
                token != null ? "acquired" : "MISSING");
        return token;
    }

    @Step("API device register")
    private static Response deviceRegister(String baseUrl, String mobile, String poi, String poiType,
                                           String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String endpoint = baseUrl + "/devices/register";
        String passcodeBlob = config.get("registration.passcodeBlob", PASSCODE_2233_BLOB);
        String body = "{\"poi\":{\"poiNumber\":\"" + poi + "\",\"poiType\":\"" + poiType
                + "\"},\"mobileNumber\":\"" + mobile + "\",\"passCode\":\"" + passcodeBlob + "\"}";
        logApiRequest("POST", endpoint, body);
        // Matches the BE report's devices/register header set exactly: no X-Client-Secret,
        // no X-Api-Key, no X-Device-Token; adds X-Latitude/X-Longitude/X-Forwarded-For.
        return RestAssured.given()
                .header("X-Session-Language", "EN")
                .header("X-Client-Id", config.get("registration.clientId", "1278490422"))
                .header("X-Device-Id", config.get("registration.deviceId", "5237008156"))
                .header("X-Device-Name", config.get("registration.deviceName", "test1262472071"))
                .header("X-Device-Platform", config.get("registration.devicePlatform", "IOS"))
                .header("X-App-Version", config.get("registration.appVersion", "456"))
                .header("X-Request-Id", UUID.randomUUID().toString())
                .header(OTP_TOKEN_HEADER, otpToken)
                .header("X-Latitude", "24.705742")
                .header("X-Longitude", "46.679297")
                .header("X-Forwarded-For", "51.235.115.207")
                .header("Content-Type", "application/json")
                .body(body)
                .post(endpoint);
    }

    @Step("API consumer login")
    private static Response consumerLogin(String baseUrl, String deviceToken, String requestId,
                                          String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String endpoint = baseUrl + "/authentication/consumers/login";
        String passcodeBlob = config.get("registration.passcodeBlob", PASSCODE_2233_BLOB);
        String body = "{\"passCode\":\"" + passcodeBlob + "\",\"firstLoginFlg\":false}";
        logApiRequest("POST", endpoint, body);
        // Matches the BE report's consumers/login header set exactly: NO X-Client-Secret, NO X-Api-Key,
        // NO X-Host-IP; X-Request-Id is a fresh UUID; real X-Device-Token + X-OTP-Token; lat/long/fwd-for
        // and x-push-notification-os-enabled-flag included.
        RequestSpecification spec = RestAssured.given()
                .header("X-Session-Language", "EN")
                .header("X-Client-Id", config.get("registration.clientId", "1278490422"))
                .header("X-Device-Id", config.get("registration.deviceId", "5237008156"))
                .header("X-Device-Name", config.get("registration.deviceName", "test1262472071"))
                .header("X-Device-Platform", config.get("registration.devicePlatform", "IOS"))
                .header("X-App-Version", config.get("registration.appVersion", "456"))
                .header("X-Request-Id", UUID.randomUUID().toString())
                .header(OTP_TOKEN_HEADER, otpToken)
                .header("X-Latitude", "24.705742")
                .header("X-Longitude", "46.679297")
                .header("X-Forwarded-For", "51.235.115.209")
                .header("x-push-notification-os-enabled-flag", "true")
                .header("Content-Type", "application/json")
                .body(body);
        if (deviceToken != null) {
            spec = spec.header(DEVICE_TOKEN_HEADER, deviceToken);
        }
        return spec.post(endpoint);
    }

    /** Re-run pre-login → otp → device register → consumer login to log the new user in. */
    @Step("API login chain for newly registered consumer")
    static boolean loginChain(String baseUrl, String mobile, String poi, String poiType) {
        return loginAndGetSession(baseUrl, mobile, poi, poiType) != null;
    }

    /**
     * Log a consumer in and return its authenticated {@link Session} (security token, session id,
     * device token) so subsequent authenticated calls can be made on its behalf.
     *
     * @return the session, or {@code null} if any step failed
     */
    @Step("API login (capture session) for {mobile}")
    static Session loginAndGetSession(String baseUrl, String mobile, String poi, String poiType) {
        try {
            Response pre = preLogin(baseUrl, mobile, poi, poiType);
            log.info("Login> pre-login status {} body {}", pre.getStatusCode(), pre.getBody().asString());

            Response generate = generateOtp(baseUrl, mobile);
            log.info("Login> otp/generate status {} body {}", generate.getStatusCode(), generate.getBody().asString());
            String otpReference = generate.jsonPath().getString("body.otpReference");
            String genToken = generate.getHeader(OTP_TOKEN_HEADER);
            if (otpReference == null || genToken == null) {
                log.warn("Login aborted: missing otpReference/token from generate-OTP");
                return null;
            }

            Response verify = verifyOtp(baseUrl, mobile, otpReference, genToken);
            log.info("Login> otp/verify status {} body {}", verify.getStatusCode(), verify.getBody().asString());
            String verifyToken = verify.getHeader(OTP_TOKEN_HEADER);
            if (verifyToken == null) {
                log.warn("Login aborted: OTP verification returned no token");
                return null;
            }

            Response device = deviceRegister(baseUrl, mobile, poi, poiType, verifyToken);
            log.info("Login> devices/register status {} body {}", device.getStatusCode(), device.getBody().asString());
            String deviceToken = device.getHeader(DEVICE_TOKEN_HEADER);
            String requestId = device.jsonPath().getString("header.requestId");
            if (deviceToken == null) {
                log.warn("Login aborted: device register returned no device token (status {}): {}",
                        device.getStatusCode(), device.getBody().asString());
                return null;
            }

            Response login = consumerLogin(baseUrl, deviceToken, requestId, verifyToken);
            log.info("Login> consumers/login status {} body {}", login.getStatusCode(), login.getBody().asString());
            String securityToken = login.getHeader(SECURITY_TOKEN_HEADER);
            String sessionId = login.getHeader("X-Session-Id");
            String consumerId = login.jsonPath().getString("body.consumerId");
            String walletNumber = login.jsonPath().getString("body.wallets[0].walletNumber");
            String walletTier = login.jsonPath().getString("body.wallets[0].walletTier");
            String fullName = login.jsonPath().getString("body.consumerName.fullName");
            int status = login.getStatusCode();
            if (status >= 200 && status < 300 && securityToken != null) {
                ConfigManager config = ConfigManager.getInstance();
                log.info("New consumer logged in (security token acquired)");
                return new Session(securityToken, sessionId, deviceToken,
                        config.get("registration.deviceId", "5237008156"),
                        config.get("registration.deviceName", "test1262472071"),
                        consumerId, verifyToken, walletNumber, walletTier, fullName);
            }
            log.warn("Consumer login failed (status {}): {}", status, login.getBody().asString());
            return null;
        } catch (Exception e) {
            log.warn("Login failed for {}: {}", mobile, e.getMessage());
            return null;
        }
    }

    /** Authenticated session for a logged-in consumer. */
    public static final class Session {
        public final String securityToken;
        public final String sessionId;
        public final String deviceToken;
        public final String deviceId;
        public final String deviceName;
        public final String consumerId;
        public final String otpToken;
        public final String walletNumber;
        public final String walletTier;
        public final String fullName;

        Session(String securityToken, String sessionId, String deviceToken,
                String deviceId, String deviceName, String consumerId, String otpToken,
                String walletNumber, String walletTier, String fullName) {
            this.securityToken = securityToken;
            this.sessionId = sessionId;
            this.deviceToken = deviceToken;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.consumerId = consumerId;
            this.otpToken = otpToken;
            this.walletNumber = walletNumber;
            this.walletTier = walletTier;
            this.fullName = fullName;
        }
    }

    /**
     * Login credentials for a freshly provisioned consumer, returned by
     * {@link #registerAndReturn(PoiType)} so a caller can drive a UI login against a brand-new account.
     */
    public static final class Provisioned {
        public final String mobile;
        public final String poi;
        public final String poiType;
        public final String passcode;
        public final String consumerId;
        public final String partyId;
        public final String walletNumber;
        public final String walletTier;
        public final String fullName;

        Provisioned(String mobile, String poi, String poiType, String passcode,
                    String consumerId, String partyId, String walletNumber, String walletTier,
                    String fullName) {
            this.mobile = mobile;
            this.poi = poi;
            this.poiType = poiType;
            this.passcode = passcode;
            this.consumerId = consumerId;
            this.partyId = partyId;
            this.walletNumber = walletNumber;
            this.walletTier = walletTier;
            this.fullName = fullName;
        }

        /**
         * The provisioned consumer's data as a {@code Map} (mobile / poi / poiType / passcode /
         * consumerId / partyId / walletNumber / walletTier / fullName) so a caller can log in with
         * these credentials. {@code null} values are returned as empty strings.
         */
        public Map<String, String> toMap() {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("mobile", nullToEmpty(mobile));
            data.put("poi", nullToEmpty(poi));
            data.put("poiType", nullToEmpty(poiType));
            data.put("passcode", nullToEmpty(passcode));
            data.put("consumerId", nullToEmpty(consumerId));
            data.put("partyId", nullToEmpty(partyId));
            data.put("walletNumber", nullToEmpty(walletNumber));
            data.put("walletTier", nullToEmpty(walletTier));
            data.put("fullName", nullToEmpty(fullName));
            return data;
        }

        private static String nullToEmpty(String s) {
            return s == null ? "" : s;
        }
    }

    // ── Oracle DB seeding (EPAY_PARTY) ─────────────────────────────

    /**
     * Look up the new PARTY_ID by POI then seed it as a fully verified, active, tier-5 wallet.
     * Mirrors the four UPDATE statements run by the Katalon keyword.
     *
     * @return the PARTY_ID, or {@code null} if the row was not found / DB unreachable
     */
    @Step("Seed consumer KYC + activation in Oracle (poi {poiNumber}, tier {tierId})")
    private static String seedConsumerInDb(String poiNumber, String tierId) {
        try (Connection conn = openDbConnection()) {
            String partyId = lookupPartyId(conn, poiNumber);
            if (partyId == null) {
                log.warn("DB seeding skipped: no CONSUMER row for POI_ID {}", poiNumber);
                return null;
            }

            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET PRODUCT_TIER_ID = '" + tierId + "' "
                    + "WHERE PARTY_ID = ?", partyId);

            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET FULL_NAME = 'Ayman Abbas', "
                    + "FULL_NAME_AR = '\u0623\u064A\u0645\u0646 \u0639\u0628\u0627\u0633', "
                    + "FIRST_NAME = 'Ayman', FIRST_NAME_AR = '????', "
                    + "FAMILY_NAME = 'Abbas', FAMILY_NAME_AR = '????', "
                    + "POI_EXPIRY_DATE = TO_TIMESTAMP('2035-01-01 03:00:00.000000000', 'YYYY-MM-DD HH24:MI:SS.FF'), "
                    + "POI_EXPIRY_DATE_HIJRI = '1456-10-21', POI_EXPIRY_STATUS = 'N', "
                    + "ID_VERIFIED_FLAG = 'Y', TAHAKOOK_VERIFIED_FLAG = 'Y', "
                    + "ID_VERIFIED_DATE = TO_TIMESTAMP('2024-01-25 01:26:03.440000000', 'YYYY-MM-DD HH24:MI:SS.FF'), "
                    + "ID_VERIFIED_SOURCE = 'NAFATH', DATE_OF_BIRTH_HIJRI = '1420-05-08', "
                    + "POLITICALLY_RELATED_FLAG = 'N', DATE_OF_BIRTH = '30-MAY-99', "
                    + "FATHER_NAME = 'Abdulailah', FATHER_NAME_AR = '\u0639\u0628\u062F\u0627\u0644\u0627\u0644\u0647', "
                    + "GRAND_NAME = 'Ibrahim', GRAND_NAME_AR = '\u0627\u0628\u0631\u0627\u0647\u064A\u0645', "
                    + "GENDER = 'M', BIRTH_COUNTRY = 'EGY', "
                    + "POI_ISSUE_DATE = '12-MAY-23 12.00.00.000000000 AM', POI_ISSUE_DATE_HIJRI = '1447-10-22' "
                    + "WHERE PARTY_ID = ?", partyId);

            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET NATIONALITY = 'EGY', NATIONALITY_CODE = 'EGY', "
                    + "ADDRESS = '10 El Geish Street', TOWN_COUNTRY = 'ALEXANDRIA', BUILDING_NO = '10', "
                    + "STREET_NAME = 'EL GEISH STREET', DISTRICT = 'GLEEM', CITY = 'ALEXANDRIA', "
                    + "POSTAL_CODE = '212222', ADDITIONAL_NUMBER = '010000000', UNIT_NUMBER = '10' "
                    + "WHERE ID = ?", partyId);

            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET STATUS = 'ACTIVE' "
                    + "WHERE PARTY_ID = ?", partyId);

            log.info("DB seeding done for partyId {} (tier {}, KYC verified, ACTIVE)", partyId, tierId);
            return partyId;
        } catch (SQLException e) {
            log.warn("DB seeding failed for POI {} — continuing: {}", poiNumber, e.getMessage());
            return null;
        }
    }

    /**
     * Activate a freshly registered consumer WITHOUT bumping the tier or completing KYC: the
     * consumer stays at its registration default (tier 3) and un-KYC'd. Flips PARTY /
     * PARTY_PRODUCT to ACTIVE, clears Nazeer, and seeds the DATE_OF_BIRTH ({@code 30-MAY-99}, same
     * value as {@link #seedConsumerInDb}) so the account has a DOB on record. Used by
     * {@link #registerDefaultTierAndReturn()}.
     *
     * @return the PARTY_ID, or {@code null} if the row was not found / DB unreachable
     */
    @Step("DB activate-only (Default tier 3, DOB seeded) for poi {poiNumber}")
    private static String activateOnlyInDb(String poiNumber) {
        try (Connection conn = openDbConnection()) {
            String partyId = lookupPartyId(conn, poiNumber);
            if (partyId == null) {
                log.warn("Activate-only skipped: no CONSUMER row for POI_ID {}", poiNumber);
                return null;
            }
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET STATUS = 'ACTIVE' WHERE ID = ?", partyId);
            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET NATHEER_STATUS = '', NATHEER_REASON = '', "
                    + "DATE_OF_BIRTH = '30-MAY-99' WHERE PARTY_ID = ?", partyId);
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET STATUS = 'ACTIVE' WHERE PARTY_ID = ?", partyId);
            log.info("Activate-only done for partyId {} (tier unchanged = registration default 3, DOB 30-MAY-99 seeded)",
                    partyId);
            return partyId;
        } catch (SQLException e) {
            log.warn("Activate-only failed for POI {} — continuing: {}", poiNumber, e.getMessage());
            return null;
        }
    }

    // ── Post-login: KYC + terms + re-activation (every user) ───────

    /** Build an authenticated request spec for a logged-in consumer session. */
    static RequestSpecification authedRequest(Session session) {
        ConfigManager config = ConfigManager.getInstance();
        RequestSpecification spec = RestAssured.given()
                .header("X-Session-Language", "EN")
                .header("X-Client-Id", config.get("registration.clientId", "1278490422"))
                .header("X-Device-Platform", config.get("registration.devicePlatform", "IOS"))
                .header("X-App-Version", config.get("registration.appVersion", "456"))
                .header("X-Device-Id", session.deviceId)
                .header("X-Device-Name", session.deviceName)
                .header("X-Security-Token", session.securityToken)
                .header("X-Request-Id", UUID.randomUUID().toString())
                .header("X-Forwarded-For", "51.235.115.210")
                .header("Content-Type", "application/json");
        if (session.sessionId != null) {
            spec = spec.header("X-Session-Id", session.sessionId);
        }
        if (session.deviceToken != null) {
            spec = spec.header(DEVICE_TOKEN_HEADER, session.deviceToken);
        }
        if (session.otpToken != null) {
            spec = spec.header(OTP_TOKEN_HEADER, session.otpToken);
        }
        return spec;
    }

    /**
     * Complete the consumer's KYC (income / employment) — {@code PUT /consumers/{id}/kyc}, exactly
     * as the BE report's {@code ConsumerUpdateKYCAPI} and Katalon {@code RegisterUrpayUser}. Called
     * for every freshly registered user. Best-effort.
     */
    @Step("API complete KYC for consumer {session.consumerId}")
    static void completeKyc(String baseUrl, Session session) {
        if (session.consumerId == null) {
            log.warn("KYC skipped: login returned no consumerId");
            return;
        }
        String endpoint = baseUrl + "/consumers/" + session.consumerId + "/kyc";
        String body = "{"
                + "\"additionalIncomeSource\":\"SALARY\","
                + "\"basicIncomeSource\":\"SALARY\","
                + "\"email\":\"email1@domain.com\","
                + "\"employer\":\"AlRajhi Bank\","
                + "\"employmentStatus\":\"Government Sector\","
                + "\"incomeRange\":\"1\","
                + "\"jobCategory\":\"21\""
                + "}";
        logApiRequest("PUT", endpoint, body);
        Response resp = authedRequest(session)
                .body(body)
            .put(endpoint);
        log.info("KYC for consumer {} -> status {} body {}", session.consumerId,
                resp.getStatusCode(), resp.getBody().asString());
    }

    /**
     * Accept the latest terms &amp; conditions after login — {@code POST
     * /consumers/new-terms/accept/after-login} with body {@code {termsVersion}}, as Katalon
     * {@code RegisterUrpayUser} does for every user after registration. Best-effort.
     */
    @Step("API accept new terms after login")
    static void acceptNewTerms(String baseUrl, Session session) {
        ConfigManager config = ConfigManager.getInstance();
        String endpoint = baseUrl + "/consumers/new-terms/accept/after-login";
        String termsVersion = config.get("registration.termsVersion", "16");
        String body = "{\"termsVersion\":\"" + termsVersion + "\"}";
        logApiRequest("POST", endpoint, body);
        Response resp = authedRequest(session)
                .header("X-Principle-Type", "Consumer")
                .body(body)
            .post(endpoint);
        log.info("Accept new terms (v{}) -> status {} body {}", termsVersion,
                resp.getStatusCode(), resp.getBody().asString());
    }

    /**
     * Record the consumer's policy consents through the Consents service, exactly as the URPay app
     * does after registration (see the {@code ConsumerConsentsInquiry} then
     * {@code ConsumerConsentsCreate} backend trace):
     * <ol>
     *   <li>{@code GET /consumers/party-consents} — inquire the existing consents;</li>
     *   <li>{@code POST /consumers/party-consents} — create a granular consent record per policy.</li>
     * </ol>
     * Unlike {@link #acceptNewTerms} (which only flags a single global terms version), this creates
     * one consent per policy: Terms &amp; Conditions ({@code tnc_UrPay}), Privacy ({@code pv_UrPay})
     * and the two marketing consents ({@code UrPayMCSMS} = SMS, {@code UrPayMCPN} = push). The policy
     * list, version and channel are config-overridable. Best-effort.
     *
     * @param entityId     the consumer's POI number (national / iqama / border id)
     * @param entityIdType the POI type (NAT / IQA / BOR)
     * @param partyId      the consumer's party id
     */
    @Step("API accept party consents (policies) for party {partyId}")
    static void acceptPartyConsents(String baseUrl, Session session, String entityId,
                                    String entityIdType, String partyId) {
        if (partyId == null || entityId == null) {
            log.warn("Party consents skipped: missing partyId/entityId");
            return;
        }
        ConfigManager config = ConfigManager.getInstance();
        String policies = config.get("registration.consentPolicies",
                "tnc_UrPay,pv_UrPay,UrPayMCSMS,UrPayMCPN");
        String policyVersion = config.get("registration.consentPolicyVersion", "NONE");
        String channelType = config.get("registration.consentChannelType", "Urpay");
        String inquiryEndpoint = baseUrl + "/consumers/party-consents";
        String createEndpoint = baseUrl + "/consumers/party-consents";

        // The app first inquires the existing consents (GET) before creating them.
        logApiRequest("GET", inquiryEndpoint, "{}");
        Response inquiry = authedRequest(session).get(inquiryEndpoint);
        log.info("Party consents inquiry -> status {} body {}", inquiry.getStatusCode(),
                inquiry.getBody().asString());

        StringBuilder consents = new StringBuilder();
        for (String raw : policies.split(",")) {
            String policyId = raw.trim();
            if (policyId.isEmpty()) {
                continue;
            }
            if (consents.length() > 0) {
                consents.append(",");
            }
            consents.append("{\"entityId\":\"").append(entityId)
                    .append("\",\"entityIdType\":\"").append(entityIdType)
                    .append("\",\"policyId\":\"").append(policyId)
                    .append("\",\"policyVersion\":\"").append(policyVersion)
                    .append("\",\"channelType\":\"").append(channelType)
                    .append("\"}");
        }
        String body = "{\"partyConsents\":[" + consents + "],\"partyId\":\"" + partyId + "\"}";
        logApiRequest("POST", createEndpoint, body);
        Response resp = authedRequest(session)
                .body(body)
            .post(createEndpoint);
        log.info("Accept party consents ({}) -> status {} body {}", policies,
                resp.getStatusCode(), resp.getBody().asString());
    }

        private static void logApiRequest(String method, String endpoint, String body) {
        log.info("API Request > {} {} body {}", method, endpoint, body);
        }

    /**
     * Re-activate a consumer after KYC. {@code /consumers/{id}/kyc} flips the consumer to INACTIVE;
     * re-run the DB batch (PARTY + PARTY_PRODUCT STATUS = ACTIVE, Nazeer cleared) so the account is
     * usable for login, mirroring the BE report's post-KYC queries. Best-effort.
     */
    @Step("DB re-activate consumer after KYC (party {partyId})")
    static void reactivateInDb(String mobile, String partyId) {
        if (partyId == null) {
            return;
        }
        try (Connection conn = openDbConnection()) {
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET STATUS = 'ACTIVE' WHERE Mobile = ?", mobile);
            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET NATHEER_STATUS = '', NATHEER_REASON = '' "
                    + "WHERE PARTY_ID = ?", partyId);
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET STATUS = 'ACTIVE' WHERE PARTY_ID = ?", partyId);
            log.info("Re-activated consumer (partyId {}) after KYC", partyId);
        } catch (SQLException e) {
            log.warn("Re-activation failed for partyId {} — continuing: {}", partyId, e.getMessage());
        }
    }

    /** Open a connection to the EPAY_PARTY Oracle DB (shared by the family helper). */
    static Connection openDbConnection() throws SQLException {
        ConfigManager config = ConfigManager.getInstance();
        String dbUrl = config.get("registration.db.url",
                "jdbc:oracle:thin:@//192.168.102.37:1521/wltdbsit.neoleap.com.sa");
        String dbUser = config.get("registration.db.user", "EPAY_TESTING");
        String dbPassword = config.get("registration.db.password", "JLSK#2936gkldw#Hjls");
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private static String lookupPartyId(Connection conn, String poiNumber) throws SQLException {
        String query = "SELECT PARTY_ID FROM EPAY_PARTY.CONSUMER WHERE POI_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, poiNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PARTY_ID");
                }
            }
        }
        return null;
    }

    private static void executeUpdate(Connection conn, String sql, String partyId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, partyId);
            int rows = ps.executeUpdate();
            log.debug("DB update affected {} row(s)", rows);
        }
    }

    // ── Shared header set (matches the Katalon TestObjectProperty list) ──

    static RequestSpecification baseHeaders(String otpToken) {
        return baseHeaders(otpToken, true);
    }

    static RequestSpecification baseHeaders(String otpToken, boolean withClientSecret) {
        ConfigManager config = ConfigManager.getInstance();
        RequestSpecification spec = RestAssured.given()
                .header("X-Session-Language", "EN")
                .header("X-Client-Id", config.get("registration.clientId", "1278490422"))
                .header("X-Api-Key", config.get("registration.apiKey",
                        "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G"))
                .header("X-Device-Id", config.get("registration.deviceId", "5237008156"))
                .header("X-Device-Name", config.get("registration.deviceName", "test1262472071"))
                .header("X-Device-Platform", config.get("registration.devicePlatform", "IOS"))
                .header("X-App-Version", config.get("registration.appVersion", "456"))
                .header("X-Request-Id", UUID.randomUUID().toString());
        if (withClientSecret) {
            spec = spec.header("X-Client-Secret", config.get("registration.clientSecret", "64"));
        }
        if (otpToken != null) {
            spec = spec.header(OTP_TOKEN_HEADER, otpToken);
        }
        return spec;
    }

    // ── POI / mobile generation ────────────────────────────────────

    private static char prefixFor(PoiType poiType) {
        ConfigManager config = ConfigManager.getInstance();
        // Allow overriding the leading digit per type (e.g. registration.poiPrefix.bor=4).
        String override = config.get("registration.poiPrefix." + poiType.name().toLowerCase(), "");
        if (override != null && !override.isEmpty()) {
            return override.charAt(0);
        }
        return poiType.prefix();
    }

    /** Generate a Luhn-valid 10-digit POI number with the given leading digit. */
    public static String generatePoiNumber(char prefix) {
        StringBuilder id = new StringBuilder().append(prefix);
        for (int i = 1; i <= 8; i++) {
            id.append(RANDOM.nextInt(10));
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(id.charAt(i));
            if (i % 2 == 0) {
                int doubled = digit * 2;
                sum += doubled / 10 + doubled % 10;
            } else {
                sum += digit;
            }
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return id.append(checkDigit).toString();
    }

    /** Generate a Saudi mobile in the {@code +966520XXXXXX} form the API expects. */
    public static String generateMobileNumber() {
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            suffix.append(RANDOM.nextInt(10));
        }
        return "+966520" + suffix;
    }

    /**
     * Prints a clearly delimited credential block to the console so the freshly
     * provisioned account (mobile / POI / passcode / party id) can be reused.
     */
    static void logCredentials(String label, String poiType, String mobile, String poi, String partyId) {
        log.info("************************************************************");
        log.info("*  {}", label);
        log.info("*  poiType  : {}", poiType);
        log.info("*  mobile   : {}", mobile);
        log.info("*  poi / id : {}", poi);
        log.info("*  passcode : {}", PASSCODE_PLAINTEXT);
        log.info("*  partyId  : {}", partyId);
        log.info("************************************************************");
    }

    /** Encrypted payload representing passcode {@code 2233} (same blob Katalon registers with). */
    private static final String PASSCODE_2233_BLOB =
            "kB79xh81HKhWD+M+Lzv1MgDWFAcKysoHXgQTDiOKRyIRxKxa/fILbL7+Zl2CCz1fS8dpGthtoze/femsZpWWd+p23atQHxB"
            + "b31Ux0mVOB3mvZBpHHP5u6efIjlCF9Q3xBvQvrR38fjOKDoRZcdvspT/TKLgSgI24wmxtN428BDo1Y+u7ip5OjY+dnjfG"
            + "wjS0r+bpoWllghai9fy7neqd9XQtRG9g1087HPBeClWvxP0+iJHmJ/0ChidQZIBOa+FI5K9kv52S5uG1QMMoZYCq9mOsX"
            + "b63XuvjMRTKbyd3W/5gLeTOVQJp5jBEMKK2yFurGRxSZPElrJIkeX9LXQgRhQ==";
}
