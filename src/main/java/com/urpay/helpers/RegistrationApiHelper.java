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
     * Register a new consumer of the given POI type, seed full KYC in the DB and log it in.
     *
     * @param poiType NAT, IQA or BOR
     * @return {@code true} only if the whole chain (including the final login) succeeded
     */
    @Step("Register new {poiType} consumer via API (full flow)")
    public static boolean register(PoiType poiType) {
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
                return false;
            }

            Response verify = verifyOtp(baseUrl, mobile, otpReference, genToken);
            String verifyToken = verify.getHeader(OTP_TOKEN_HEADER);
            if (verifyToken == null) {
                log.warn("Registration aborted: OTP verification returned no token (status {})",
                        verify.getStatusCode());
                return false;
            }

            seedTahaqoqInfo(poiNumber, mobile);

            Response registration = registerConsumer(baseUrl, mobile, otpReference, poiNumber,
                    poiType.code(), verifyToken);
            int regStatus = registration.getStatusCode();
            if (regStatus < 200 || regStatus >= 300) {
                log.warn("Consumer registration failed for {} (status {}): {}",
                        mobile, regStatus, registration.getBody().asString());
                return false;
            }
            log.info("Consumer registration succeeded for {} (status {})", mobile, regStatus);

            String partyId = seedConsumerInDb(poiNumber);
            log.info("  partyId      : {}", partyId);

            boolean loggedIn = loginChain(baseUrl, mobile, poiNumber, poiType.code());

            logCredentials(poiType.code() + " CONSUMER CREDENTIALS", poiType.code(), mobile, poiNumber, partyId);
            log.info("=== Registration complete for {} | mobile {} | poi {} | partyId {} | login {} ===",
                    poiType.code(), mobile, poiNumber, partyId, loggedIn ? "OK" : "FAILED");
            return loggedIn;
        } catch (Exception e) {
            log.warn("Registration via API failed for {} ({}): {}", mobile, poiType.code(), e.getMessage());
            return false;
        }
    }

    // ── API steps (mirror the Katalon keyword) ─────────────────────

    @Step("API pre-login")
    static Response preLogin(String baseUrl, String mobile, String poi, String poiType) {
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"poi\":{\"poiNumber\":\"" + poi
                + "\",\"poiType\":\"" + poiType + "\"}}";
        // Matches the BE report's pre-login header set: no X-Client-Secret, plus X-Forwarded-For.
        return baseHeaders(null, false)
                .header("X-Forwarded-For", "51.235.115.205")
                .header("Content-Type", "application/json")
                .body(body)
                .post(baseUrl + "/authentication/consumers/pre-login");
    }

    @Step("API generate OTP")
    static Response generateOtp(String baseUrl, String mobile) {
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"purpose\":\"001\"}";
        return baseHeaders(null)
                .header("X-Forwarded-For", "1")
                .header("Content-Type", "application/json")
                .body(body)
                .post(baseUrl + "/otp/generate");
    }

    @Step("API verify OTP")
    static Response verifyOtp(String baseUrl, String mobile, String otpReference, String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String otp = config.get("registration.otp", "1234");
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"otp\":\"" + otp + "\",\"otpReference\":\""
                + otpReference + "\",\"purpose\":\"001\"}";
        return baseHeaders(otpToken)
                .header("X-Forwarded-For", "1")
                .header("Content-Type", "application/json")
                .body(body)
                .post(baseUrl + "/otp/verify");
    }

    /** Seed the Tahaqoq/Nafath simulator so the POI passes ID verification during registration. */
    @Step("API seed Tahaqoq info (simulator)")
    static Response seedTahaqoqInfo(String poiNumber, String mobile) {
        ConfigManager config = ConfigManager.getInstance();
        String simBaseUrl = config.get("registration.simBaseUrl",
                "https://neoleap-backend-simulator-sit.apps.ocpuat.neoleap.com.sa");
        String apiKey = config.get("registration.simApiKey",
                "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G");
        return baseHeaders(null)
                .header("X-Api-Key", apiKey)
                .header("Content-Type", "application/json")
                .queryParam("IDNumber", poiNumber)
                .queryParam("MobileNumber", mobile)
                .body("{}")
                .post(simBaseUrl + "/__admin/tahaqoq-info");
    }

    @Step("API consumer registration")
    static Response registerConsumer(String baseUrl, String mobile, String otpReference,
                                             String poi, String poiType, String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String passcodeBlob = config.get("registration.passcodeBlob", PASSCODE_2233_BLOB);
        String body = "{\"mobileNumber\":\"" + mobile + "\",\"otpReference\":\"" + otpReference
                + "\",\"passCode\":\"" + passcodeBlob + "\",\"poi\":{\"poiNumber\":\"" + poi
                + "\",\"poiType\":\"" + poiType + "\"}}";

        // No Thread.sleep (framework rule): instead retry to absorb the brief propagation
        // delay between the Tahaqoq seed and the registration becoming accepted.
        Response response = null;
        for (int attempt = 1; attempt <= 4; attempt++) {
            response = baseHeaders(otpToken)
                    .header("X-Longitude", "46.679297")
                    .header("X-Latitude", "24.705742")
                    .header("X-Forwarded-For", "1")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .post(baseUrl + "/consumers/registration");
            int status = response.getStatusCode();
            if (status >= 200 && status < 300) {
                return response;
            }
            log.info("Registration attempt {} returned status {} — retrying", attempt, status);
        }
        return response;
    }

    @Step("API device register")
    private static Response deviceRegister(String baseUrl, String mobile, String poi, String poiType,
                                           String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String passcodeBlob = config.get("registration.passcodeBlob", PASSCODE_2233_BLOB);
        String body = "{\"poi\":{\"poiNumber\":\"" + poi + "\",\"poiType\":\"" + poiType
                + "\"},\"mobileNumber\":\"" + mobile + "\",\"passCode\":\"" + passcodeBlob + "\"}";
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
                .post(baseUrl + "/devices/register");
    }

    @Step("API consumer login")
    private static Response consumerLogin(String baseUrl, String deviceToken, String requestId,
                                          String otpToken) {
        ConfigManager config = ConfigManager.getInstance();
        String passcodeBlob = config.get("registration.passcodeBlob", PASSCODE_2233_BLOB);
        String body = "{\"passCode\":\"" + passcodeBlob + "\",\"firstLoginFlg\":false}";
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
        return spec.post(baseUrl + "/authentication/consumers/login");
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
            int status = login.getStatusCode();
            if (status >= 200 && status < 300 && securityToken != null) {
                ConfigManager config = ConfigManager.getInstance();
                log.info("New consumer logged in (security token acquired)");
                return new Session(securityToken, sessionId, deviceToken,
                        config.get("registration.deviceId", "5237008156"),
                        config.get("registration.deviceName", "test1262472071"));
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

        Session(String securityToken, String sessionId, String deviceToken,
                String deviceId, String deviceName) {
            this.securityToken = securityToken;
            this.sessionId = sessionId;
            this.deviceToken = deviceToken;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
        }
    }

    // ── Oracle DB seeding (EPAY_PARTY) ─────────────────────────────

    /**
     * Look up the new PARTY_ID by POI then seed it as a fully verified, active, tier-5 wallet.
     * Mirrors the four UPDATE statements run by the Katalon keyword.
     *
     * @return the PARTY_ID, or {@code null} if the row was not found / DB unreachable
     */
    @Step("Seed consumer KYC + activation in Oracle (poi {poiNumber})")
    private static String seedConsumerInDb(String poiNumber) {
        try (Connection conn = openDbConnection()) {
            String partyId = lookupPartyId(conn, poiNumber);
            if (partyId == null) {
                log.warn("DB seeding skipped: no CONSUMER row for POI_ID {}", poiNumber);
                return null;
            }

            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET PRODUCT_TIER_ID = '5' "
                    + "WHERE PARTY_ID = ?", partyId);

            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET FULL_NAME = 'Ayman Abbas', "
                    + "FULL_NAME_AR = '\u0623\u064A\u0645\u0646 \u0639\u0628\u0627\u0633', "
                    + "FIRST_NAME = 'Ayman', FIRST_NAME_AR = '????', "
                    + "FAMILY_NAME = 'Abbas', FAMILY_NAME_AR = '????', "
                    + "POI_EXPIRY_DATE = TO_TIMESTAMP('2026-12-30 03:00:00.000000000', 'YYYY-MM-DD HH24:MI:SS.FF'), "
                    + "POI_EXPIRY_DATE_HIJRI = '1460-06-29', POI_EXPIRY_STATUS = 'N', "
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

            log.info("DB seeding done for partyId {} (tier 5, KYC verified, ACTIVE)", partyId);
            return partyId;
        } catch (SQLException e) {
            log.warn("DB seeding failed for POI {} — continuing: {}", poiNumber, e.getMessage());
            return null;
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
    static String generatePoiNumber(char prefix) {
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
    static String generateMobileNumber() {
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
