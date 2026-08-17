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
 *   host: walletsit.neoleap.com.sa, basePath: /walletapppci/v1/cards
 *   Auth: X-Client-Id (ApiKey) + X-Security-Token (JWT) — partyId is derived server-side from
 *   the JWT and is NOT sent in the request body.
 *
 * Mirrors the same authenticated-session pattern as {@link RegistrationApiHelper} (used by every
 * other implemented backend API in this project): log the user back in via the API to obtain a
 * fresh JWT + walletNumber, then call the endpoint with {@link RegistrationApiHelper#authedRequest}.
 *
 * The card's Vpan/ExpiryDate aren't shown anywhere in the app UI (only a masked PAN), so — same
 * as {@link IvrSkipHelper} does for the IVR x-request-id — they are looked up from the Oracle DB
 * message dump of the most recent card-issuance request.
 */
public final class CardActivationApiHelper {

    private static final Logger log = LoggerFactory.getLogger(CardActivationApiHelper.class);

    private CardActivationApiHelper() {}

    /**
     * Activate the most recently issued physical card/bracelet for this consumer: logs the user
     * back in via API to get a fresh JWT, looks up the issued card's Vpan/ExpiryDate from the DB,
     * then calls the Activation Initiate API.
     *
     * @param mobile  local-format mobile number (e.g. 0520XXXXXX)
     * @param poi     national ID / POI number
     * @param poiType POI type code (e.g. "NAT") — see {@link RegistrationApiHelper.PoiType#code()}
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

        RegistrationApiHelper.Session session =
                RegistrationApiHelper.loginAndGetSession(regBaseUrl, mobile, poi, poiType == null ? "NAT" : poiType);
        if (session == null) {
            log.error("Card activation skipped: could not obtain an authenticated session for mobile {}", mobile);
            return false;
        }
        return activatePhysicalCard(session);
    }

    /** Same as {@link #activatePhysicalCard(String, String, String)} but with an already-logged-in session. */
    @Step("Call Physical Card Activation Initiate API")
    static boolean activatePhysicalCard(RegistrationApiHelper.Session session) {
        CardIssuanceInfo cardInfo = getRecentIssuedCardInfo();
        if (cardInfo == null) {
            log.error("Card activation skipped: could not resolve the issued card's Vpan/ExpiryDate from DB");
            return false;
        }

        ConfigManager config = ConfigManager.getInstance();
        String baseUrl = config.get("cardActivation.baseUrl", "https://walletsit.neoleap.com.sa/walletapppci/v1/cards");
        String endpoint = baseUrl + "/activation/initiate";

        String body = "{"
                + "\"walletNumber\":\"" + session.walletNumber + "\","
                + "\"cardInfo\":{"
                + "\"Vpan\":\"" + cardInfo.vpan + "\","
                + "\"ExpiryDate\":\"" + cardInfo.expiryDate + "\""
                + "}"
                + "}";

        log.info("Calling Physical Card Activation Initiate API: {}", endpoint);
        log.info("Request body: {}", body);

        try {
            RestAssured.useRelaxedHTTPSValidation();
            Response response = RegistrationApiHelper.authedRequest(session)
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
     * Query Oracle DB (EAIR.EAI_MESSAGE_DUMP) for the Vpan/ExpiryDate of the most recent card
     * issuance request. Same table/time-window pattern as {@link
     * IvrSkipHelper#getCardIssuanceRequestId}.
     *
     * Bracelet issuance did NOT produce a 'CardIssuanceInitiateRq_Rule' record (confirmed via a
     * real run — 0 rows), so the flow ID must differ for the physical/wearable path. Since the
     * exact ID isn't known yet, this: (1) dumps every distinct flow ID seen in the window so a
     * failed run's log tells us the real one, (2) tries several likely candidate flow IDs, and
     * (3) falls back to a flow-ID-agnostic search for any recent message containing a "vpan"
     * field at all.
     */
    private static CardIssuanceInfo getRecentIssuedCardInfo() {
        ConfigManager config = ConfigManager.getInstance();
        String dbUrl = config.get("ivr.db.url", "jdbc:oracle:thin:@//192.168.100.101:1521/ESBAUDIT_DBSIT");
        String dbUser = config.get("ivr.db.user", "ESBQA");
        String dbPassword = config.get("ivr.db.password", "YFnK#9qy2");

        String[] candidateFlowIds = {
                "CardIssuanceInitiateRq_Rule",
                "PhysicalCardIssuanceInitiateRq_Rule",
                "BraceletIssuanceInitiateRq_Rule",
                "CardActivationInitiateRq_Rule",
                "PhysicalCardActiviationInitiateRq_Rule"
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
            try (Statement diagStmt = conn.createStatement();
                 ResultSet diagRs = diagStmt.executeQuery(diagQuery)) {
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
                    + "search for any recent message containing a 'vpan' field");
            return queryVpanAnyFlow(conn);
        } catch (SQLException e) {
            log.error("DB query for issued card info failed: {}", e.getMessage());
            return null;
        }
    }

    private static CardIssuanceInfo queryVpanForFlowId(Connection conn, String flowId) throws SQLException {
        String query = "SELECT "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.vpan') AS vpan, "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.expiryDate') AS expiry_date "
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
                }
            }
        }
        return null;
    }

    private static CardIssuanceInfo queryVpanAnyFlow(Connection conn) throws SQLException {
        String query = "SELECT "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.vpan') AS vpan, "
                + "JSON_VALUE(md_msg_data, '$.body.cardInfo.expiryDate') AS expiry_date, MD_FLOW_ID "
                + "FROM (SELECT md_msg_data, MD_FLOW_ID FROM EAIR.EAI_MESSAGE_DUMP "
                + "WHERE md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE "
                + "AND UPPER(md_msg_data) LIKE '%VPAN%' "
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
                log.warn("Found a message containing 'vpan' but JSON_VALUE path '$.body.cardInfo.vpan' "
                        + "didn't extract it — the field is nested differently; needs a real payload to fix");
            } else {
                log.warn("No message containing 'vpan' found in the last 10 minutes at all");
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

