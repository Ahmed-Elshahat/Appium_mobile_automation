package com.urpay.helpers;

import com.urpay.core.ConfigManager;
import com.urpay.helpers.RegistrationApiHelper.PoiType;
import com.urpay.helpers.RegistrationApiHelper.Session;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Family Registration API Helper — registers a parent + a kid (&lt; 18) and establishes the
 * guardianship link between them, entirely through the backend.
 *
 * Replicates Katalon's {@code ParentAndChildWallet.createParentAndChildWallet(parent, child)}
 * (plus the per-user registration that precedes it). Source of the flow + data: the
 * {@code Family_Suite / CreateDelinkFamilyRequestlessthan18} scenario.
 *
 * Per member:
 *   1. seed Tahaqoq info (simulator)        — so the POI passes ID verification
 *   2. register the consumer                — pre-login → otp → consumers/registration → login
 * Then, once both exist, the link:
 *   3. yakeen-info  (parent &amp; kid)       — seeds the real identity (names/DOB/gender) in Yakeen
 *   4. yakeen-relation (parent &amp; kid)    — relates the two NINs via the kinship code
 *                                             (parent kinshipCode 1, kid kinshipCode 6)
 *   5. forceVerification(parent)            — DB: activate the parent so the kid can verify against it
 *
 * The actual in-app "link request → parent approve" (CreateDelinkFamilyRequest) is a UI journey;
 * this helper only performs the backend setup that makes that link verifiable.
 *
 * Best-effort + console-logged like {@link RegistrationApiHelper}. The SIT API host, the Yakeen/
 * Tahaqoq simulator and the Oracle DB require neoleap VPN — not expected to run from a laptop.
 */
public final class FamilyRegistrationApiHelper {

    private static final Logger log = LoggerFactory.getLogger(FamilyRegistrationApiHelper.class);

    private FamilyRegistrationApiHelper() {
    }

    /** A family member (parent or kid) with the identity attributes Yakeen/Tahaqoq expect. */
    static final class Member {
        String role;
        String poi;
        String mobile;
        String poiType = PoiType.NAT.code();
        String relationCode;
        String depthPoi;
        String firstName;
        String fatherName;
        String grandFatherName;
        String familyName;
        String englishFirstName;
        String englishSecondName;
        String englishThirdName;
        String englishLastName;
        String gender;
        String placeOfBirth;
        String birthDateG;
        String dateOfBirthH;
        String idExpiryDate;
        String idExpirationDateH;
        String partyId;
    }

    /**
     * Register a parent + a kid and seed the Yakeen guardianship link between them.
     *
     * @return {@code true} only if both registrations and the link seeding succeeded
     */
    @Step("Register parent + kid and link them via API")
    public static boolean registerAndLinkParentChild() {
        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("registration.baseUrl", "https://192.168.100.71:14301/walletapp/v1");

        Member parent = buildParent();
        Member kid = buildKid();
        // Each is the other's "dependant" POI for the Yakeen relation.
        parent.depthPoi = kid.poi;
        kid.depthPoi = parent.poi;

        log.info("=== Registering family (parent + kid) ===");
        log.info("  parent : mobile {} | poi {} | relationCode {}", parent.mobile, parent.poi, parent.relationCode);
        log.info("  kid    : mobile {} | poi {} | relationCode {}", kid.mobile, kid.poi, kid.relationCode);

        try {
            RestAssured.useRelaxedHTTPSValidation();

            if (!registerMember(baseUrl, parent)) {
                log.warn("Family setup aborted: parent registration failed");
                return false;
            }
            if (!registerMember(baseUrl, kid)) {
                log.warn("Family setup aborted: kid registration failed");
                return false;
            }

            // Seed Yakeen identity + relationship for both members.
            seedYakeenInfo(parent);
            seedYakeenInfo(kid);
            seedYakeenRelation(parent);
            seedYakeenRelation(kid);

            // Activate BOTH members so each can log in: the kid to send the link request,
            // the parent (Nazeer) to approve it. Without an ACTIVE PARTY_PRODUCT the login
            // chain's devices/register call is rejected (HTTP 400). Only the parent is bumped
            // to full tier 5 — the kid keeps its registration-default tier (report does the same).
            kid.partyId = forceVerification(kid.mobile, kid.poi, false);
            parent.partyId = forceVerification(parent.mobile, parent.poi, true);

            // Drive the link entirely through the backend, mirroring the BE report
            // (Family_Suite / CreateDelinkFamilyRequestlessthan18):
            //   kid logs in   → POST /family-requests/create
            //   parent logs in → GET receiver inbox for the pending id → PUT .../{id}/update APPROVE
            if (!sendLinkRequest(baseUrl, kid, parent)) {
                log.warn("Family setup: link request was not created");
                return false;
            }
            boolean approved = approveLinkRequest(baseUrl, parent, kid);

            RegistrationApiHelper.logCredentials(
                    "PARENT CREDENTIALS (" + parent.poiType + ")", parent.poiType, parent.mobile, parent.poi, parent.partyId);
            RegistrationApiHelper.logCredentials(
                    "KID CREDENTIALS (" + kid.poiType + ", relation " + kid.relationCode + ")",
                    kid.poiType, kid.mobile, kid.poi, kid.partyId);
            log.info("=== Family setup complete | parent poi {} (partyId {}) linked with kid poi {} | approved {} ===",
                    parent.poi, parent.partyId, kid.poi, approved ? "YES" : "NO");
            return approved;
        } catch (Exception e) {
            log.warn("Family registration/link failed: {}", e.getMessage());
            return false;
        }
    }

    // ── Per-member registration (reuses RegistrationApiHelper steps) ──

    @Step("Register family member {member.role}")
    private static boolean registerMember(String baseUrl, Member member) {
        log.info("Registering {} (poi {}, mobile {})", member.role, member.poi, member.mobile);

        RegistrationApiHelper.preLogin(baseUrl, member.mobile, member.poi, member.poiType);

        Response generate = RegistrationApiHelper.generateOtp(baseUrl, member.mobile);
        String otpReference = generate.jsonPath().getString("body.otpReference");
        String genToken = generate.getHeader("X-OTP-Token");
        if (otpReference == null || genToken == null) {
            log.warn("{} registration aborted: missing otpReference/token", member.role);
            return false;
        }

        Response verify = RegistrationApiHelper.verifyOtp(baseUrl, member.mobile, otpReference, genToken);
        String verifyToken = verify.getHeader("X-OTP-Token");
        if (verifyToken == null) {
            log.warn("{} registration aborted: OTP verification returned no token", member.role);
            return false;
        }

        RegistrationApiHelper.seedTahaqoqInfo(member.poi, member.mobile);

        Response registration = RegistrationApiHelper.registerConsumer(baseUrl, member.mobile, otpReference,
                member.poi, member.poiType, verifyToken);
        int status = registration.getStatusCode();
        if (status < 200 || status >= 300) {
            log.warn("{} registration failed (status {}): {}", member.role, status,
                    registration.getBody().asString());
            return false;
        }
        log.info("{} registered (status {})", member.role, status);
        // NB: do NOT log in here — the member is not active yet (no DB activation). Login is
        // attempted only after force-verification, right before it is needed (link/approve).
        return true;
    }

    // ── Yakeen simulator seeding ───────────────────────────────────

    /** Seed the member's real identity in the Yakeen simulator (used for guardianship verification). */
    @Step("API seed Yakeen info for {member.role}")
    private static Response seedYakeenInfo(Member member) {
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Api-Key", simApiKey())
                .queryParam("nin", member.poi)
                .queryParam("firstName", member.firstName)
                .queryParam("fatherName", member.fatherName)
                .queryParam("grandFatherName", member.grandFatherName)
                .queryParam("familyName", member.familyName)
                .queryParam("englishFirstName", member.englishFirstName)
                .queryParam("englishSecondName", member.englishSecondName)
                .queryParam("englishThirdName", member.englishThirdName)
                .queryParam("englishLastName", member.englishLastName)
                .queryParam("idExpiryDate", member.idExpiryDate)
                .queryParam("dateOfBirthH", member.dateOfBirthH)
                .queryParam("birthDateG", member.birthDateG)
                .queryParam("gender", member.gender)
                .queryParam("idExpirationDateH", member.idExpirationDateH)
                .queryParam("placeOfBirth", member.placeOfBirth)
                .post(simBaseUrl() + "/__admin/yakeen-info");
    }

    /** Relate the member's NIN to its dependant (the other family member) via the kinship code. */
    @Step("API seed Yakeen relation for {member.role}")
    private static Response seedYakeenRelation(Member member) {
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-Api-Key", simApiKey())
                .queryParam("nin", member.poi)
                .queryParam("deptNin", member.depthPoi)
                .queryParam("kinshipCode", member.relationCode)
                .post(simBaseUrl() + "/__admin/yakeen-relation");
    }

    private static String simApiKey() {
        return ConfigManager.getInstance().get("registration.simApiKey",
                "d2ZWn5RUnS1VPq/FQHY8Og==2dcqHTmi51RZyXHPac9H3r6+eCqig7QMwtJDD49G");
    }

    private static String simBaseUrl() {
        return ConfigManager.getInstance().get("registration.simBaseUrl",
                "https://neoleap-backend-simulator-sit.apps.ocpuat.neoleap.com.sa");
    }

    // ── Backend link request (kid) + approve (parent) ──────────────

    /** Kid logs in and sends a LINK_TO_FAMILY request naming the parent as receiver. */
    @Step("API kid sends family link request")
    private static boolean sendLinkRequest(String baseUrl, Member kid, Member parent) {
        Session kidSession = RegistrationApiHelper.loginAndGetSession(baseUrl, kid.mobile, kid.poi, kid.poiType);
        if (kidSession == null) {
            log.warn("Link request aborted: kid login failed");
            return false;
        }
        String body = "{"
                + "\"familyMemberFirstNameAr\":\"" + kid.firstName + "\","
                + "\"familyMemberFirstNameEn\":\"" + kid.englishFirstName + "\","
                + "\"familyMemberNewMobileNumber\":\"" + kid.mobile + "\","
                + "\"familyRequestType\":\"LINK_TO_FAMILY\","
                + "\"receiverMobileNumber\":\"" + parent.mobile + "\","
                + "\"receiverPoiNumber\":\"" + parent.poi + "\","
                + "\"receiverPoiType\":\"" + parent.poiType + "\""
                + "}";
        Response response = authedRequest(kidSession)
                .body(body)
                .post(baseUrl + "/family-requests/create");
        int status = response.getStatusCode();
        if (status >= 200 && status < 300) {
            log.info("Family link request created (status {}): {}", status, response.getBody().asString());
            return true;
        }
        log.warn("Family link request failed (status {}): {}", status, response.getBody().asString());
        return false;
    }

    /**
     * Parent logs in, finds the pending request in its RECEIVER inbox (as the BE report does),
     * then approves it. The approve body carries the kid's Hijri date of birth.
     */
    @Step("API parent fetches the pending request and approves it")
    private static boolean approveLinkRequest(String baseUrl, Member parent, Member kid) {
        Session parentSession = RegistrationApiHelper.loginAndGetSession(baseUrl, parent.mobile, parent.poi,
                parent.poiType);
        if (parentSession == null) {
            log.warn("Approve aborted: parent login failed");
            return false;
        }

        // Read the parent's RECEIVER inbox to obtain the pending LINK_TO_FAMILY request id.
        Response inbox = authedRequest(parentSession)
                .queryParam("requestType", "LINK_TO_FAMILY")
                .queryParam("requestUserRole", "RECEIVER")
                .queryParam("offset", "0")
                .queryParam("limit", "1")
                .get(baseUrl + "/consumers/family-requests");
        String requestId = inbox.jsonPath().getString("body.familyRequests[0].id");
        log.info("Parent inbox status {} | pending requestId {}", inbox.getStatusCode(), requestId);
        if (requestId == null) {
            log.warn("Approve aborted: no pending family request in parent inbox: {}", inbox.getBody().asString());
            return false;
        }

        String body = "{\"action\":\"APPROVE\",\"reason\":\"string\",\"familyMemberDateOfBirth\":\""
                + kid.dateOfBirthH + "\"}";
        Response response = authedRequest(parentSession)
                .body(body)
                .put(baseUrl + "/consumers/family-requests/" + requestId + "/update");
        int status = response.getStatusCode();
        String resultStatus = response.jsonPath().getString("body.status");
        if (status >= 200 && status < 300) {
            log.info("Family request {} approved (body.status {})", requestId, resultStatus);
            return true;
        }
        log.warn("Family request approve failed (status {}): {}", status, response.getBody().asString());
        return false;
    }

    /** Build an authenticated request for a logged-in consumer session. */
    private static RequestSpecification authedRequest(Session session) {
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
            spec = spec.header("X-Device-Token", session.deviceToken);
        }
        return spec;
    }

    // ── DB force-verification (activate the parent / Nazeer) ────────

    /**
     * Activate the user in the DB so the guardianship verification passes (mirrors Katalon's
     * {@code forceVerification}): set PARTY + PARTY_PRODUCT to ACTIVE and clear the Nazeer status.
     *
     * @return the PARTY_ID, or {@code null} if the row was not found / DB unreachable
     */
    @Step("DB force-verification for {mobile} (poi {poi})")
    private static String forceVerification(String mobile, String poi, boolean fullTier) {
        try (Connection conn = RegistrationApiHelper.openDbConnection()) {
            String partyId = lookupPartyId(conn, poi);
            if (partyId == null) {
                log.warn("Force-verification skipped: no CONSUMER row for POI_ID {}", poi);
                return null;
            }

            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET STATUS = 'ACTIVE' WHERE Mobile = ?", mobile);
            // Mark the consumer KYC-verified (same flags the standalone seed sets) WITHOUT overwriting
            // the real identity/DOB — the kid must stay < 18. Login's pre-login/devices-register reject
            // an un-verified consumer (E201023), so STATUS=ACTIVE alone is not enough.
            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET NATHEER_STATUS = '', NATHEER_REASON = '', "
                    + "ID_VERIFIED_FLAG = 'Y', TAHAKOOK_VERIFIED_FLAG = 'Y', ID_VERIFIED_SOURCE = 'NAFATH', "
                    + "ID_VERIFIED_DATE = TO_TIMESTAMP('2024-01-25 01:26:03.440000000', 'YYYY-MM-DD HH24:MI:SS.FF'), "
                    + "POI_EXPIRY_STATUS = 'N', POLITICALLY_RELATED_FLAG = 'N' WHERE PARTY_ID = ?", partyId);
            // Only the PARENT (Nazeer) is bumped to full tier 5. The report never sets the kid's
            // PRODUCT_TIER_ID — a < 18 kid at tier 5 fails the link create with E430129 "Invalid Product tier".
            if (fullTier) {
                executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET PRODUCT_TIER_ID = '5', STATUS = 'ACTIVE' "
                        + "WHERE PARTY_ID = ?", partyId);
            } else {
                executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET STATUS = 'ACTIVE' WHERE PARTY_ID = ?", partyId);
            }

            log.info("Force-verification done for partyId {} (KYC verified, {}PARTY + PARTY_PRODUCT ACTIVE)",
                    partyId, fullTier ? "tier 5, " : "default tier, ");
            return partyId;
        } catch (SQLException e) {
            log.warn("Force-verification failed for POI {} — continuing: {}", poi, e.getMessage());
            return null;
        }
    }

    private static String lookupPartyId(Connection conn, String poiNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT PARTY_ID FROM EPAY_PARTY.CONSUMER WHERE POI_ID = ?")) {
            ps.setString(1, poiNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PARTY_ID");
                }
            }
        }
        return null;
    }

    private static void executeUpdate(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            int rows = ps.executeUpdate();
            log.debug("DB update affected {} row(s)", rows);
        }
    }

    // ── Member builders (data from the Katalon Family_Suite setup) ──

    private static Member buildParent() {
        Member m = new Member();
        m.role = "PARENT";
        m.poi = RegistrationApiHelper.generatePoiNumber(PoiType.NAT.prefix());
        m.mobile = RegistrationApiHelper.generateMobileNumber();
        m.relationCode = "1";
        m.idExpiryDate = "2034-10-11T00:00:00";
        m.idExpirationDateH = "1456-07-28";
        m.dateOfBirthH = "1400-08-15";
        m.birthDateG = "1980-06-27";
        m.gender = "M";
        m.placeOfBirth = "\u0627\u0644\u0631\u064A\u0627\u0636";            // الرياض
        m.firstName = "\u0645\u0639\u062A\u0632";                            // معتز
        m.fatherName = "\u0635\u0644\u0627\u062D";                           // صلاح
        m.grandFatherName = "\u0639\u0645\u0631";                            // عمر
        m.familyName = "\u0627\u0644\u063A\u0627\u0645\u062F\u064A";         // الغامدي
        m.englishFirstName = "Mutez";
        m.englishSecondName = "Salah";
        m.englishThirdName = "Omar";
        m.englishLastName = "Alghamdi";
        return m;
    }

    private static Member buildKid() {
        Member m = new Member();
        m.role = "KID";
        m.poi = RegistrationApiHelper.generatePoiNumber(PoiType.NAT.prefix());
        m.mobile = RegistrationApiHelper.generateMobileNumber();
        m.relationCode = "6";
        m.idExpiryDate = "2034-10-11T00:00:00";
        m.idExpirationDateH = "1456-07-28";
        m.dateOfBirthH = "1437-08-15";
        m.birthDateG = "2015-05-27";                                          // < 18
        m.gender = "M";
        m.placeOfBirth = "\u0627\u0644\u0631\u064A\u0627\u0636";            // الرياض
        m.firstName = "\u0645\u0639\u062A\u0632";                            // معتز
        m.fatherName = "\u0635\u0644\u0627\u062D";                           // صلاح
        m.grandFatherName = "\u0639\u0645\u0631";                            // عمر
        m.familyName = "\u0627\u0644\u063A\u0627\u0645\u062F\u064A";         // الغامدي
        m.englishFirstName = "Mutez";                                        // matches BE report (معتز)
        m.englishSecondName = "Salah";
        m.englishThirdName = "Omar";
        m.englishLastName = "Alghamdi";
        return m;
    }
}
