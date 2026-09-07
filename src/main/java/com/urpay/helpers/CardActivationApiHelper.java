package com.urpay.helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Physical Card / Bracelet Activation Initiate API.
 *
 * Swagger: POST {basePath}/activation/initiate
 *   swagger host: walletsit.neoleap.com.sa (not resolvable from the test network — "No such
 *   host is known"); reachable internal host: 192.168.100.71:14302. basePath: /walletapppci/v1/cards
 *   Auth: X-Client-Id (ApiKey) + X-Security-Token (JWT) — partyId is derived server-side from
 *   the JWT and is NOT sent in the request body.
 *
 * Mirrors the same authenticated-session pattern as
 * {@link RegistrationApiHelper} (used by every other implemented backend API in
 * this project): log the user back in via the API to obtain a fresh JWT +
 * walletNumber, then call the endpoint with
 * {@link RegistrationApiHelper#authedRequest}.
 *
 * The card's Vpan/ExpiryDate aren't shown anywhere in the app UI (only a masked
 * PAN), so — same as {@link IvrSkipHelper} does for the IVR x-request-id — they
 * are looked up from the Oracle DB message dump of the most recent
 * card-issuance request.
 */
public final class CardActivationApiHelper {

    private static final Logger log = LoggerFactory.getLogger(CardActivationApiHelper.class);

    private CardActivationApiHelper() {
    }

    /**
     * Activate the most recently issued physical card/bracelet for this
     * consumer: logs the user back in via API to get a fresh JWT, looks up the
     * issued card's Vpan/ExpiryDate from the DB, then calls the Activation
     * Initiate API.
     *
     * @param mobile local-format mobile number (e.g. 0520XXXXXX)
     * @param poi national ID / POI number
     * @param poiType POI type code (e.g. "NAT") — see
     * {@link RegistrationApiHelper.PoiType#code()}
     * @return true if the API call returned a success status
     */
    @Step("Activate physical card via API — mobile: {mobile}")
    public static boolean activatePhysicalCard(String mobile, String poi, String poiType) {
        if (mobile == null || mobile.isEmpty() || poi == null || poi.isEmpty()) {
            log.error("Card activation skipped: missing mobile/poi credentials");
            return false;
        }
        ConfigManager config = ConfigManager.getInstance();
        String regBaseUrl = config.get("registration.baseUrl", "https://192.168.100.71:14301/walletapp/v1");

        RegistrationApiHelper.Session session
                = RegistrationApiHelper.loginAndGetSession(
                        regBaseUrl,
                        mobile,
                        poi,
                        poiType == null ? "NAT" : poiType);

        if (session == null) {
            log.error("Card activation skipped: could not obtain an authenticated session for mobile {}", mobile);
            return false;
        }

// Get ConsumerId
        String consumerId = session.consumerId; // Replace with actual ConsumerId if different

// Call helper class
        String listVpanToken
                = VpanListApiHelper.getListVpanToken(session, consumerId);
        log.info("Retrieved X-List-VPAN-Token: {}", listVpanToken);

        return activatePhysicalCard(session, listVpanToken, mobile);
    }

    /**
     * Same as {@link #activatePhysicalCard(String, String, String)} but with an
     * already-logged-in session.
     */
    @Step("Call Physical Card Activation Initiate API")
    static boolean activatePhysicalCard(RegistrationApiHelper.Session session, String listVpanToken, String mobile) {
        if (listVpanToken == null || listVpanToken.isEmpty()) {
            // Fail fast with a clear reason instead of RestAssured's generic "Header value cannot
            // be null" — see VpanListApiHelper's warning log for the Cards List response detail.
            log.error("Card activation skipped: Cards List API returned no X-List-VPAN-Token");
            return false;
        }

        // The front-end activation screen shows no OTP step, but the backend still requires an
        // X-OTP-Token generated with purpose "023" (NFC Card Activation), static otp "1234",
        // token from the verify response attached to the initiate call.
        String otpToken = getActivationOtpToken(session, mobile);
        if (otpToken == null || otpToken.isEmpty()) {
            log.error("Card activation skipped: purpose-023 OTP verify returned no X-OTP-Token");
            return false;
        }

        CardIssuanceInfo cardInfo = getRecentIssuedCardInfo();
        if (cardInfo == null) {
            log.error("Card activation skipped: could not resolve the issued card's Vpan/ExpiryDate from DB");
            return false;
        }

        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("cardActivation.baseUrl", "https://192.168.100.71:14302/walletapppci/v1/cards");
        String endpoint = baseUrl + "/activation/initiate";

        String body = "{"
                + "\"cardInfo\":{"
                + "\"Vpan\":\"" + cardInfo.vpan + "\","
                + "\"ExpiryDate\":\"" + cardInfo.expiryDate + "\""
                + "},"
                + "\"walletNumber\":\"" + session.walletNumber + "\","
                + "\"amount\":{"
                + "\"currency\":\"" + "SAR" + "\","
                + "\"value\":" + "80.5"
                + "},"
                + "\"NewPin\":\"" + "2233" + "\""
                + "}";

        log.info("Calling Physical Card Activation Initiate API: {}", endpoint);
        log.info("Request body: {}", body);

        try {
            RestAssured.useRelaxedHTTPSValidation();
            // The Cards API gateway needs X-Api-Key in addition to authedRequest's standard
            // headers (X-Client-Id/X-Security-Token) — omitting it returns 400 "E200978:
            // Authorization Error." (confirmed via a real run). Same key used by
            // RegistrationApiHelper.baseHeaders() for the pre-login/registration gateway.
            // Same PCI gateway (port 14302) as VpanListApiHelper's Cards List call — needs the
            // same X-Principle-Id/X-Principle-Type/X-Encryption-Key headers.
            // authedRequest(session, otpToken) supplies the purpose-023 token directly instead of
            // appending a second X-OTP-Token header on top of the session's stale login token.
            Response response = RegistrationApiHelper.authedRequest(session, otpToken)
                    .header("X-List-VPAN-Token", listVpanToken)
                    .header("X-Principle-Id", "78988180-c573-413d-9ec4-c78a0e7b1c92")
                    .header("X-Principle-Type", "consumer")
                    .header("X-Encryption-Key", ConfigManager.getInstance().get("cardsList.encryptionKey",
                            "KAjy+9lWISrsakpA1Dwx45xMk/IPlGW9qb6/e8OS+U5PTkwkfdoIdhFCezImU/3jkgrorlN3PH+"
                            + "XqofrL0AFqi0L5by2+2mKtpvz/rUEUwGAb+Mhc8EF4KPh9uQHyUjSUWo5LUWCzQRGPQtZI72Y9Q3bC"
                            + "lJd2TDi8Bi22Ac45HQ="))
                    .body(body)
                    .post(endpoint);

            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();
            log.info("Activation Initiate response: {} — {}", statusCode, responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("Physical Card Activation Initiate SUCCESS");
                return true;
            }
            log.error("Physical Card Activation Initiate failed with status: {}", statusCode);
            return false;
        } catch (Exception e) {
            log.error("Physical Card Activation Initiate API call failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Silently obtain an X-OTP-Token for card activation: {@code otp/generate} + {@code
     * otp/verify} with purpose "023" (NFC Card Activation) and the static SIT OTP "1234".
     * Returns null on any failure.
     */
    private static String getActivationOtpToken(RegistrationApiHelper.Session session, String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            log.error("Activation OTP skipped: no mobile number available");
            return null;
        }
        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("registration.baseUrl", "https://192.168.100.71:14301/walletapp/v1");
        String otp = config.get("registration.otp", "1234");

        try {
            RestAssured.useRelaxedHTTPSValidation();

            String generateBody = "{\"mobileNumber\":\"" + mobile + "\",\"purpose\":\"023\"}";
            // No X-OTP-Token needed yet for generate — pass null to avoid sending the stale
            // purpose-001 login token that authedRequest(session) would otherwise bake in.
            Response generate = RegistrationApiHelper.authedRequest(session, null)
                    .body(generateBody)
                    .post(baseUrl + "/otp/generate");
            String generateOtpToken = generate.getHeader("X-OTP-Token");
            String otpReference = generate.jsonPath().getString("body.otpReference");
            if (generateOtpToken == null || otpReference == null) {
                log.error("Activation otp/generate failed: status={} body={}",
                        generate.getStatusCode(), generate.getBody().asString());
                return null;
            }

            String verifyBody = "{\"mobileNumber\":\"" + mobile + "\",\"otp\":\"" + otp
                    + "\",\"otpReference\":\"" + otpReference + "\",\"purpose\":\"023\"}";
            Response verify = RegistrationApiHelper.authedRequest(session, generateOtpToken)
                    .body(verifyBody)
                    .post(baseUrl + "/otp/verify");
            String verifyOtpToken = verify.getHeader("X-OTP-Token");
            if (verifyOtpToken == null) {
                log.error("Activation otp/verify failed: status={} body={}",
                        verify.getStatusCode(), verify.getBody().asString());
                return null;
            }
            log.info("Activation X-OTP-Token (purpose 023) obtained");
            return verifyOtpToken;
        } catch (Exception e) {
            log.error("Activation OTP generate/verify failed: {}", e.getMessage());
            return null;
        }
    }


    /**
     * Query Oracle DB (EAIR.EAI_MESSAGE_DUMP) for the Vpan/ExpiryDate of the
     * most recent card issuance request. Same table/time-window pattern as {@link
     * IvrSkipHelper#getCardIssuanceRequestId}.
     *
     * Confirmed via real runs: bracelet issuance does NOT use
     * 'CardIssuanceInitiateRq_Rule' (the digital-card flow ID); the real
     * candidates are 'IssueCardRq_Rule'/'IssueCardRs_Rule' and 'POST
     * .../cards/issuance/initiate' (seen in the diagnostic dump). Also: the
     * swagger's request schema uses PascalCase ("Vpan"/"ExpiryDate") — Oracle
     * JSON_VALUE member-name matching is case-sensitive, so a lowercase path
     * silently returns null even when a message containing "vpan" text is found
     * (confirmed: LIKE match succeeded, JSON_VALUE extraction didn't). Both
     * casings are tried.
     */
    private static CardIssuanceInfo getRecentIssuedCardInfo() {
        ConfigManager config = ConfigManager.getInstance();
        String dbUrl = config.get("ivr.db.url", "jdbc:oracle:thin:@//192.168.100.101:1521/ESBAUDIT_DBSIT");
        String dbUser = config.get("ivr.db.user", "ESBQA");
        String dbPassword = config.get("ivr.db.password", "YFnK#9qy2");

        String[] candidateFlowIds = {
            "IssueCardRs_Rule",
            "IssueCardRq_Rule",
            "CardIssuanceRq",
            "POST /wallet-financials-apis/v1/cards/issuance/initiate",
            "CardIssuanceInitiateRq_Rule"
        };

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Diagnostic: list every distinct flow ID seen in the window, so a failed run's log
            // reveals the ACTUAL flow ID bracelet issuance uses instead of guessing again.
            String diagQuery = "SELECT DISTINCT MD_FLOW_ID, "
                    + "(SELECT MAX(md_creation_tmstmp) FROM EAIR.EAI_MESSAGE_DUMP d2 "
                    + "WHERE d2.MD_FLOW_ID = d1.MD_FLOW_ID "
                    + "AND d2.md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE) AS last_seen "
                    + "FROM EAIR.EAI_MESSAGE_DUMP d1 "
                    + "WHERE md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE";
            log.info("Diagnostic — flow IDs seen in the last 10 minutes:");
            try (Statement diagStmt = conn.createStatement(); ResultSet diagRs = diagStmt.executeQuery(diagQuery)) {
                int count = 0;
                while (diagRs.next()) {
                    count++;
                    log.info("  flowId='{}' lastSeen={}", diagRs.getString("MD_FLOW_ID"), diagRs.getString("last_seen"));
                }
                if (count == 0) {
                    log.warn("  (no EAI_MESSAGE_DUMP records at all in the last 10 minutes)");
                }
            } catch (SQLException diagEx) {
                log.warn("Diagnostic flow-ID dump failed: {}", diagEx.getMessage());
            }

            for (String flowId : candidateFlowIds) {
                CardIssuanceInfo info = queryVpanForFlowId(conn, flowId);
                if (info != null) {
                    return info;
                }
            }

            log.warn("None of the candidate flow IDs matched — falling back to a flow-ID-agnostic "
                    + "search for any recent message containing a 'vpan'/'Vpan' field");
            return queryVpanAnyFlow(conn);
        } catch (SQLException e) {
            log.error("DB query for issued card info failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Ground-truth path confirmed from a real captured 'CardIssuanceRq'
     * message; older guesses kept as COALESCE fallbacks. ALWAYS logs a raw
     * preview when the flow ID matches, whether or not a value was extracted —
     * the most reliable way to learn the schema if it varies.
     */
    private static CardIssuanceInfo queryVpanForFlowId(Connection conn, String flowId) throws SQLException {
        String query = "SELECT "
                + "COALESCE(JSON_VALUE(md_msg_data, '$.body.CardDetails.ResponseCardIdentifier.VPan'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.Vpan'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.vpan'), "
                + "JSON_VALUE(md_msg_data, '$.body.Vpan'), "
                + "JSON_VALUE(md_msg_data, '$.body.vpan')) AS vpan, "
                + "COALESCE(JSON_VALUE(md_msg_data, '$.body.CardDetails.ExpiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.ExpiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.expiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.ExpiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.expiryDate')) AS expiry_date, "
                + "SUBSTR(md_msg_data, 1, 1500) AS msg_preview "
                + "FROM (SELECT md_msg_data FROM EAIR.EAI_MESSAGE_DUMP "
                + "WHERE md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE "
                + "AND MD_FLOW_ID = ? "
                + "ORDER BY md_creation_tmstmp DESC) WHERE ROWNUM = 1";
        try (var pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, flowId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String vpan = rs.getString("vpan");
                    String expiryDate = rs.getString("expiry_date");
                    if (vpan != null && !vpan.isEmpty()) {
                        log.info("Resolved issued card via flowId='{}': vpan=***{}, expiryDate={}",
                                flowId, vpan.substring(Math.max(0, vpan.length() - 4)), expiryDate);
                        return new CardIssuanceInfo(vpan, expiryDate);
                    }
                    log.info("flowId='{}' matched but no Vpan extracted. Raw preview: {}",
                            flowId, rs.getString("msg_preview"));
                }
            }
        }
        return null;
    }

    /**
     * Last resort: search for an actual JSON key/value named vpan (quoted, e.g.
     * "Vpan":"...") rather than a bare substring — the unquoted substring
     * search previously matched an unrelated header key ("x-list-vpan-token")
     * from a wallet-limits inquiry.
     */
    private static CardIssuanceInfo queryVpanAnyFlow(Connection conn) throws SQLException {
        String query = "SELECT "
                + "COALESCE(JSON_VALUE(md_msg_data, '$.body.CardDetails.ResponseCardIdentifier.VPan'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.Vpan'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.vpan'), "
                + "JSON_VALUE(md_msg_data, '$.body.Vpan'), "
                + "JSON_VALUE(md_msg_data, '$.body.vpan')) AS vpan, "
                + "COALESCE(JSON_VALUE(md_msg_data, '$.body.CardDetails.ExpiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.ExpiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.expiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.ExpiryDate'), "
                + "JSON_VALUE(md_msg_data, '$.body.expiryDate')) AS expiry_date, "
                + "MD_FLOW_ID, SUBSTR(md_msg_data, 1, 1500) AS msg_preview "
                + "FROM (SELECT md_msg_data, MD_FLOW_ID FROM EAIR.EAI_MESSAGE_DUMP "
                + "WHERE md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE "
                + "AND (md_msg_data LIKE '%\"VPan\"%' OR md_msg_data LIKE '%\"Vpan\"%' OR md_msg_data LIKE '%\"vpan\"%') "
                + "ORDER BY md_creation_tmstmp DESC) WHERE ROWNUM = 1";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                String vpan = rs.getString("vpan");
                String expiryDate = rs.getString("expiry_date");
                if (vpan != null && !vpan.isEmpty()) {
                    log.info("Resolved issued card via flow-ID-agnostic search (flowId='{}'): vpan=***{}, expiryDate={}",
                            rs.getString("MD_FLOW_ID"), vpan.substring(Math.max(0, vpan.length() - 4)), expiryDate);
                    return new CardIssuanceInfo(vpan, expiryDate);
                }
                // Still couldn't extract — dump the flow ID + a raw preview so the exact JSON
                // shape is visible in the log instead of guessing a 3rd time.
                log.warn("Found a quoted \"vpan\"/\"Vpan\" key under flowId='{}' but no JSON path "
                        + "extracted it. Raw preview: {}", rs.getString("MD_FLOW_ID"), rs.getString("msg_preview"));
            } else {
                log.warn("No message with a quoted \"vpan\"/\"Vpan\" JSON key found in the last 10 minutes");
            }
            return null;
        }
    }

    private static final class CardIssuanceInfo {

        final String vpan;
        final String expiryDate;

        CardIssuanceInfo(String vpan, String expiryDate) {
            this.vpan = vpan;
            this.expiryDate = expiryDate;
        }
    }
}
