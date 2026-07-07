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
        String consumerId;
    }

    /**
     * Register a parent + a kid and seed the Yakeen guardianship link between them.
     *
     * @return {@code true} only if both registrations and the link seeding succeeded
     */
    public static boolean registerAndLinkParentChild() {
        return provisionLinkedParentAndKid() != null;
    }

    /**
     * Register a FRESH parent + kid (randomly generated POI/mobile on every call) and link them via
     * the backend, returning the generated credentials so a UI test can log in as the kid. Returns
     * {@code null} if any registration / link step failed.
     */
    @Step("Register parent + kid and link them via API")
    public static LinkedPair provisionLinkedParentAndKid() {
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

            // ── BE createFamilyRequest order (the order matters) ──────────────────────────
            // 1. Seed the simulator for BOTH members BEFORE registering: tahaqoq + yakeen-info +
            //    yakeen-relation must exist first so the guardianship is recognised at registration.
            //    Verify each seed — a silently-rejected seed makes the guardianship lookup fail later.
            if (!seedFamilyPair(kid, parent)) {
                log.error("=== Family setup aborted: simulator seeding did not take effect, so the "
                        + "guardianship link would fail. Check the neoleap VPN + simulator ({}). ===",
                        simBaseUrl());
                return null;
            }

            // 2. Register the PARENT fully (register → activate/seed → KYC), then the KID — BE order.
            //    Tier 5 for the parent; the kid keeps its registration-default tier 3.
            if (!registerMember(baseUrl, parent)) {
                log.warn("Family setup aborted: parent registration failed");
                return null;
            }
            parent.partyId = forceVerification(parent, true);
            completeParentKyc(baseUrl, parent);

            if (!registerMember(baseUrl, kid)) {
                log.warn("Family setup aborted: kid registration failed");
                return null;
            }
            // The kid MUST start BARE (tier 3, null identity, unverified) — its identity + tier-9 promotion
            // are populated by the LINK from the Yakeen guardianship seeded before registration. Pre-seeding
            // the kid via forceVerification made it look like a standalone verified consumer, so the link
            // created the family relation WITHOUT promoting the kid (half-linked) → app CreateFamilyRequest
            // then collides with the existing relation (E430151). activateOnly matches the proven contract.
            kid.partyId = activateOnly(kid);

            // 3. Link: kid logs in → POST /family-requests/create; parent logs in → inbox → APPROVE
            //    → family-member KYC (createFamilyRequest).
            if (!sendLinkRequest(baseUrl, kid, parent)) {
                log.warn("Family setup: link request was not created");
                return null;
            }
            boolean approved = approveLinkRequest(baseUrl, parent, kid);

            // Final Nazeer clear pass for BOTH members. The backend re-sets the kid's NATHEER_STATUS
            // asynchronously after the link settles, which makes the in-app "verification" step reappear;
            // the BE test runs updateNateerStatus twice for this reason. This second pass (after the link
            // is fully done) clears it so the kid opens without the verification step.
            if (approved) {
                reactivate(parent);
                reactivate(kid);
            }

            RegistrationApiHelper.logCredentials(
                    "PARENT CREDENTIALS (" + parent.poiType + ")", parent.poiType, parent.mobile, parent.poi, parent.partyId);
            RegistrationApiHelper.logCredentials(
                    "KID CREDENTIALS (" + kid.poiType + ", relation " + kid.relationCode + ")",
                    kid.poiType, kid.mobile, kid.poi, kid.partyId);
            log.info("=== Family setup complete | parent poi {} (partyId {}) linked with kid poi {} | approved {} ===",
                    parent.poi, parent.partyId, kid.poi, approved ? "YES" : "NO");
            return approved ? new LinkedPair(parent, kid) : null;
        } catch (Exception e) {
            log.warn("Family registration/link failed: {}", e.getMessage());
            return null;
        }
    }

    /** The freshly generated + linked parent/kid pair returned by {@link #provisionLinkedParentAndKid()}. */
    public static final class LinkedPair {
        private final Member parent;
        private final Member kid;

        LinkedPair(Member parent, Member kid) {
            this.parent = parent;
            this.kid = kid;
        }

        /** Parent mobile in the generated {@code +966...} form. */
        public String parentMobile() { return parent.mobile; }

        public String parentPoi() { return parent.poi; }

        /** Kid mobile in the generated {@code +966...} form. */
        public String kidMobile() { return kid.mobile; }

        public String kidPoi() { return kid.poi; }
    }

    /**
     * Provision a linkable parent + kid pair and STOP before the backend link. It seeds the
     * simulators (tahaqoq + Yakeen guardianship), then registers + activates BOTH consumers exactly
     * like {@link #registerAndLinkParentChild()} — but does NOT create/approve the family request.
     *
     * <p>Use this to drive the LINK (kid creates the request → parent approves → family-member KYC)
     * manually from the app while capturing the real APIs, to validate them against our automated
     * implementation. Both members' credentials (mobile / POI / passcode / party id) are logged.
     *
     * @return {@code true} if both members were registered and activated
     */
    @Step("Provision a linkable parent + kid pair (no backend link)")
    public static boolean registerFamilyPairForManualLink() {
        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("registration.baseUrl", "https://192.168.100.71:14301/walletapp/v1");

        Member parent = buildParent();
        Member kid = buildKid();
        parent.depthPoi = kid.poi;
        kid.depthPoi = parent.poi;

        log.info("=== Provisioning family pair (NO backend link — manual app flow) ===");
        log.info("  parent : mobile {} | poi {} | relationCode {}", parent.mobile, parent.poi, parent.relationCode);
        log.info("  kid    : mobile {} | poi {} | relationCode {}", kid.mobile, kid.poi, kid.relationCode);

        try {
            RestAssured.useRelaxedHTTPSValidation();

            // 1. Seed the simulators (tahaqoq + Yakeen guardianship) BEFORE registration. Verify each
            //    seed took effect — a silently-rejected seed yields a member that logs in fine but is
            //    NOT linkable (the parent's APPROVE does a Yakeen dependent lookup on the kid poi + DOB).
            if (!seedFamilyPair(kid, parent)) {
                log.error("=== Family pair aborted: simulator seeding did not take effect, so the pair "
                        + "would NOT be linkable from the app. Check the neoleap VPN + simulator ({}). ===",
                        simBaseUrl());
                return false;
            }

            // 2. Register + activate the PARENT fully (register → seed/activate → KYC), then the KID.
            if (!registerMember(baseUrl, parent)) {
                log.warn("Family pair aborted: parent registration failed");
                return false;
            }
            parent.partyId = forceVerification(parent, true);
            completeParentKyc(baseUrl, parent);

            if (!registerMember(baseUrl, kid)) {
                log.warn("Family pair aborted: kid registration failed");
                return false;
            }
            // BE createFamilyRequest leaves the kid UN-seeded (null name / unverified, tier 3) with only a
            // 3-query activation — its identity is populated by the LINK. Match that so the app link is
            // validated from the same starting state.
            kid.partyId = activateOnly(kid);

            // 3. STOP here — the link (create → approve → family-member KYC) is driven from the app.
            RegistrationApiHelper.logCredentials(
                    "PARENT CREDENTIALS (" + parent.poiType + ")", parent.poiType, parent.mobile, parent.poi, parent.partyId);
            RegistrationApiHelper.logCredentials(
                    "KID CREDENTIALS (" + kid.poiType + ", relation " + kid.relationCode + ")",
                    kid.poiType, kid.mobile, kid.poi, kid.partyId);
            log.info("=== Family pair READY. Now drive the LINK from the app: log in as the KID and send a "
                    + "family request to the PARENT, then approve as the PARENT. ===");
            log.info("  parent poi {} (partyId {}) | kid poi {} (partyId {}) | passcode {}",
                    parent.poi, parent.partyId, kid.poi, kid.partyId, RegistrationApiHelper.PASSCODE_PLAINTEXT);
            return true;
        } catch (Exception e) {
            log.warn("Family pair provisioning failed: {}", e.getMessage());
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

    /**
     * Seed BOTH members into the simulators (tahaqoq + Yakeen identity + guardianship relation) and
     * verify every seed actually took effect. Returns {@code false} if any seed was rejected — the
     * callers must abort, because a member whose Yakeen record never landed logs in normally yet can
     * NOT be linked: the parent's APPROVE resolves the kid via a Yakeen dependent lookup on the kid's
     * poi + Hijri DOB. This is exactly the "kid looks fine but the link fails" failure mode.
     */
    private static boolean seedFamilyPair(Member kid, Member parent) {
        // Non-short-circuit (&) so every seed runs and is logged even if an earlier one failed.
        return verifySeed("tahaqoq kid",              RegistrationApiHelper.seedTahaqoqInfo(kid.poi, kid.mobile))
             & verifySeed("tahaqoq parent",           RegistrationApiHelper.seedTahaqoqInfo(parent.poi, parent.mobile))
             & verifySeed("yakeen-info kid",          seedYakeenInfo(kid))
             & verifySeed("yakeen-info parent",       seedYakeenInfo(parent))
             & verifySeed("yakeen-relation kid->parent", seedYakeenRelation(kid))
             & verifySeed("yakeen-relation parent->kid", seedYakeenRelation(parent));
    }

    /** Log a simulator seed's outcome; treat only 2xx as success so a rejected seed aborts loudly. */
    private static boolean verifySeed(String label, Response resp) {
        int code = resp.getStatusCode();
        if (code >= 200 && code < 300) {
            log.info("Seed OK     [{}] (status {})", label, code);
            return true;
        }
        log.error("Seed FAILED [{}] (status {}): {}", label, code, resp.getBody().asString());
        return false;
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
        // Per the BE report (createFamilyRequest): the KID does NOT complete its own KYC here.
        // It just logs in and sends the request; its KYC is done by the parent AFTER approval
        // via /consumers/family-member/kyc. Remember the kid's consumerId for that step.
        kid.consumerId = kidSession.consumerId;
        // Accept the latest T&C for the kid so the app does not present the terms screen on first
        // login (the UI journeys log in as the kid and would otherwise stall on it).
        RegistrationApiHelper.acceptNewTerms(baseUrl, kidSession);
        // Body mirrors the app's CreateFamilyRequestRq_Rule exactly: the parent (receiver) is
        // resolved by mobile only (no receiverPoi*), and the kid is named via requesterConsumerId.
        String body = "{"
                + "\"familyMemberFirstNameAr\":\"" + kid.firstName + "\","
                + "\"familyMemberFirstNameEn\":\"" + kid.englishFirstName + "\","
                + "\"familyMemberNewMobileNumber\":\"" + kid.mobile + "\","
                + "\"familyRequestType\":\"LINK_TO_FAMILY\","
                + "\"receiverMobileNumber\":\"" + parent.mobile + "\","
                + "\"requesterConsumerId\":\"" + kidSession.consumerId + "\""
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
        // Parent is already KYC'd + ACTIVE (completeParentKyc ran during setup). Fresh login to approve.
        Session parentSession = RegistrationApiHelper.loginAndGetSession(baseUrl, parent.mobile, parent.poi,
                parent.poiType);
        if (parentSession == null) {
            log.warn("Approve aborted: parent login failed");
            return false;
        }

        // Read the parent's RECEIVER inbox to obtain the pending request id. Mirrors the app:
        // statusCategory=PENDING + invalidateCache=true forces a fresh read so the just-created
        // request is visible immediately (avoids a stale-cache miss right after create).
        Response inbox = authedRequest(parentSession)
                .queryParam("requestUserRole", "RECEIVER")
                .queryParam("statusCategory", "PENDING")
                .queryParam("invalidateCache", "true")
                .queryParam("offset", "0")
                .queryParam("limit", "10")
                .get(baseUrl + "/consumers/family-requests");
        String requestId = inbox.jsonPath().getString("body.familyRequests[0].id");
        log.info("Parent inbox status {} | pending requestId {}", inbox.getStatusCode(), requestId);
        if (requestId == null) {
            log.warn("Approve aborted: no pending family request in parent inbox: {}", inbox.getBody().asString());
            return false;
        }

        String body = "{\"action\":\"APPROVE\",\"familyMemberDateOfBirth\":\""
                + kid.dateOfBirthH + "\"}";
        Response response = authedRequest(parentSession)
                .body(body)
                .put(baseUrl + "/consumers/family-requests/" + requestId + "/update");
        int status = response.getStatusCode();
        String resultStatus = response.jsonPath().getString("body.status");
        // The approve response returns the kid's consumerId ONLY when the link actually settled
        // (BE ran the Yakeen dependent lookup and promoted the kid). A missing familyMemberConsumerId
        // means the link did NOT settle — treat it as a failure instead of masking it with kid.consumerId,
        // otherwise the kid is left half-linked (tier 3 + family relation) and the app link hits E430151.
        String kidConsumerId = response.jsonPath().getString("body.familyMemberConsumerId");
        boolean settled = status >= 200 && status < 300 && kidConsumerId != null;
        if (!settled) {
            log.warn("Family request approve did not settle (status {}, body.status {}): {}",
                    status, resultStatus, response.getBody().asString());
            return false;
        }
        log.info("Family request {} approved (body.status {}, familyMemberConsumerId {})",
                requestId, resultStatus, kidConsumerId);
        // After approval the parent completes the kid's KYC as a family member (createFamilyRequest).
        // The tier-9 promotion depends on this — if it fails the kid stays half-linked, so gate on it.
        if (!completeFamilyMemberKyc(baseUrl, parentSession, kidConsumerId)) {
            log.warn("Family-member KYC did not complete for kid consumer {} — kid may remain half-linked",
                    kidConsumerId);
            return false;
        }
        // The family-member KYC flips the kid INACTIVE; re-activate + clear Nazeer so the kid shows
        // ACTIVE/verified when opened (mirrors the report's post-link EPAYPARTY updateNateerStatus).
        reactivate(kid);
        return true;
    }

    /**
     * Parent completes the kid's KYC as a family member after the link is approved
     * ({@code PUT /consumers/family-member/kyc}), exactly as the BE report's createFamilyRequest does.
     */
    @Step("API parent completes family-member KYC for kid {kidConsumerId}")
    private static boolean completeFamilyMemberKyc(String baseUrl, Session parentSession, String kidConsumerId) {
        if (kidConsumerId == null) {
            log.warn("Family-member KYC skipped: kid login returned no consumerId");
            return false;
        }
        // Minor/family-member KYC as sent by the app: INVESTMENT RETURNS / Student / incomeRange 4,
        // with no employer/email/jobCategory (that adult-style body belongs to the parent's own KYC).
        String body = "{"
                + "\"familyMemberConsumerId\":\"" + kidConsumerId + "\","
                + "\"basicIncomeSource\":\"INVESTMENT RETURNS\","
                + "\"employmentStatus\":\"Student\","
                + "\"incomeRange\":\"4\","
                + "\"isPoliticallyRelated\":false"
                + "}";
        RequestSpecification spec = authedRequest(parentSession);
        if (parentSession.otpToken != null) {
            spec = spec.header("X-OTP-Token", parentSession.otpToken);
        }
        Response resp = spec.body(body).put(baseUrl + "/consumers/family-member/kyc");
        int status = resp.getStatusCode();
        log.info("Family-member KYC for kid consumer {} -> status {} body {}", kidConsumerId,
                status, resp.getBody().asString());
        return status >= 200 && status < 300;
    }

    /**
     * Complete the consumer's KYC (income / employment) exactly like the BE report's
     * {@code ConsumerUpdateKYCAPI} — {@code PUT /consumers/{consumerId}/kyc}. Authenticated call
     * that additionally carries the login OTP token (as the report does). Best-effort.
     */
    @Step("API complete KYC for consumer {session.consumerId}")
    private static void completeKyc(String baseUrl, Session session) {
        if (session.consumerId == null) {
            log.warn("KYC skipped: login returned no consumerId");
            return;
        }
        String body = "{"
                + "\"additionalIncomeSource\":\"SALARY\","
                + "\"basicIncomeSource\":\"SALARY\","
                + "\"email\":\"email1@domain.com\","
                + "\"employer\":\"AlRajhi Bank\","
                + "\"employmentStatus\":\"Government Sector\","
                + "\"incomeRange\":\"1\","
                + "\"jobCategory\":\"21\""
                + "}";
        RequestSpecification spec = authedRequest(session);
        if (session.otpToken != null) {
            spec = spec.header("X-OTP-Token", session.otpToken);
        }
        Response resp = spec.body(body).put(baseUrl + "/consumers/" + session.consumerId + "/kyc");
        log.info("KYC for consumer {} -> status {} body {}", session.consumerId,
                resp.getStatusCode(), resp.getBody().asString());
    }

    /**
     * Re-activate a member after KYC. The {@code ConsumerUpdateKYCAPI} flips the consumer to
     * INACTIVE; the BE report re-runs a DB batch (and re-logs-in) to flip it back to ACTIVE so the
     * parent can approve (otherwise the approve fails with {@code E430038 "Consumer Not Active"}).
     */
    @Step("DB re-activate {member.role} after KYC")
    private static void reactivate(Member member) {
        if (member.partyId == null) {
            return;
        }
        try (Connection conn = RegistrationApiHelper.openDbConnection()) {
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET STATUS = 'ACTIVE' WHERE Mobile = ?", member.mobile);
            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET NATHEER_STATUS = '', NATHEER_REASON = '' "
                    + "WHERE PARTY_ID = ?", member.partyId);
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET STATUS = 'ACTIVE' WHERE PARTY_ID = ?",
                    member.partyId);
            log.info("Re-activated {} (partyId {}) after KYC", member.role, member.partyId);
        } catch (SQLException e) {
            log.warn("Re-activation failed for {} — continuing: {}", member.role, e.getMessage());
        }
    }

    /**
     * Parent completes its own KYC during setup (createFamilyRequest): log in, call
     * {@code /consumers/{id}/kyc}, then re-activate (KYC flips the consumer to INACTIVE) so the parent
     * is ACTIVE again by the time it approves the link. Best-effort.
     */
    @Step("Parent completes KYC during setup")
    private static void completeParentKyc(String baseUrl, Member parent) {
        Session session = RegistrationApiHelper.loginAndGetSession(baseUrl, parent.mobile, parent.poi,
                parent.poiType);
        if (session == null) {
            log.warn("Parent KYC skipped: parent login failed");
            return;
        }
        completeKyc(baseUrl, session);
        // Accept the latest T&C for the parent (mirrors the main registration flow) so the app does
        // not present the terms screen on first login.
        RegistrationApiHelper.acceptNewTerms(baseUrl, session);
        reactivate(parent);
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
    @Step("DB force-verification for {member.role}")
    private static String forceVerification(Member member, boolean fullTier) {
        try (Connection conn = RegistrationApiHelper.openDbConnection()) {
            String partyId = lookupPartyId(conn, member.poi);
            if (partyId == null) {
                log.warn("Force-verification skipped: no CONSUMER row for POI_ID {}", member.poi);
                return null;
            }

            String fullNameEn = member.englishFirstName + " " + member.englishSecondName + " "
                    + member.englishThirdName + " " + member.englishLastName;
            String fullNameAr = member.firstName + " " + member.fatherName + " "
                    + member.grandFatherName + " " + member.familyName;

            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET STATUS = 'ACTIVE' WHERE Mobile = ?", member.mobile);

            // Seed the consumer's real identity (name EN/AR, DOB G/H, gender) AND KYC-verify it, mirroring
            // the report's post-registration DB batch. Without the name the app shows a null name; without
            // ID_VERIFIED_FLAG the user appears unverified and login is rejected (E201023). The kid keeps
            // its < 18 DOB (only the tier differs between parent and kid).
            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET "
                    + "FULL_NAME = '" + fullNameEn + "', FULL_NAME_AR = '" + fullNameAr + "', "
                    + "FIRST_NAME = '" + member.englishFirstName + "', FIRST_NAME_AR = '" + member.firstName + "', "
                    + "FATHER_NAME = '" + member.englishSecondName + "', FATHER_NAME_AR = '" + member.fatherName + "', "
                    + "GRAND_NAME = '" + member.englishThirdName + "', GRAND_NAME_AR = '" + member.grandFatherName + "', "
                    + "FAMILY_NAME = '" + member.englishLastName + "', FAMILY_NAME_AR = '" + member.familyName + "', "
                    + "DATE_OF_BIRTH = TO_DATE('" + member.birthDateG + "', 'YYYY-MM-DD'), "
                    + "DATE_OF_BIRTH_HIJRI = '" + member.dateOfBirthH + "', GENDER = '" + member.gender + "', "
                    + "POI_EXPIRY_DATE = TO_TIMESTAMP('2035-01-01 03:00:00.000000000', 'YYYY-MM-DD HH24:MI:SS.FF'), "
                    + "POI_EXPIRY_DATE_HIJRI = '1456-10-21', "
                    + "NATHEER_STATUS = '', NATHEER_REASON = '', "
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

            log.info("Force-verification done for {} partyId {} ({}, name '{}', DOB {})",
                    member.role, partyId, fullTier ? "tier 5" : "default tier", fullNameEn, member.birthDateG);
            return partyId;
        } catch (SQLException e) {
            log.warn("Force-verification failed for {} — continuing: {}", member.role, e.getMessage());
            return null;
        }
    }

    /**
     * Activate a consumer WITHOUT seeding its identity — mirrors the BE createFamilyRequest kid path,
     * where the kid registers at tier 3 with a null name / unverified and only a 3-query batch runs
     * (PARTY + PARTY_PRODUCT ACTIVE, Nazeer cleared). The kid's real identity is then populated by the
     * LINK itself (from the Yakeen guardianship seeded before registration).
     *
     * @return the PARTY_ID, or {@code null} if not found / DB unreachable
     */
    @Step("DB activate-only for {member.role} (no identity seed — matches BE kid)")
    private static String activateOnly(Member member) {
        try (Connection conn = RegistrationApiHelper.openDbConnection()) {
            String partyId = lookupPartyId(conn, member.poi);
            if (partyId == null) {
                log.warn("Activate-only skipped: no CONSUMER row for POI_ID {}", member.poi);
                return null;
            }
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY SET STATUS = 'ACTIVE' WHERE Mobile = ?", member.mobile);
            executeUpdate(conn, "UPDATE EPAY_PARTY.CONSUMER SET NATHEER_STATUS = '', NATHEER_REASON = '' "
                    + "WHERE PARTY_ID = ?", partyId);
            executeUpdate(conn, "UPDATE EPAY_PARTY.PARTY_PRODUCT SET STATUS = 'ACTIVE' WHERE PARTY_ID = ?", partyId);
            log.info("Activate-only done for {} partyId {} (tier unchanged, no identity seed)",
                    member.role, partyId);
            return partyId;
        } catch (SQLException e) {
            log.warn("Activate-only failed for {} — continuing: {}", member.role, e.getMessage());
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
        m.dateOfBirthH = "1430-06-06";
        m.birthDateG = "2009-06-01";                                          // ~17 yrs (>16 and <18)
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
