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
 * IVR Skip Helper — bypasses the phone verification call during card issuance.
 *
 * Replicates Katalon's:
 *   - BeneficiaryKeyword.getResourceIdCardAddIVR() → queries Oracle DB for x-request-id
 *   - Login.CardsAndPayemntIVRSKIP() → calls IVR callback API to skip verification
 *
 * Flow:
 *   1. Query EAIR.EAI_MESSAGE_DUMP for the card issuance x-request-id
 *   2. POST to /v1/ivr/callback/initiate with that request ID
 *   3. The app receives the callback and proceeds past the Verification Call screen
 */
public class IvrSkipHelper {

    private static final Logger log = LoggerFactory.getLogger(IvrSkipHelper.class);

    /**
     * Complete IVR skip: query DB for request ID, then call callback API.
     *
     * @param userId the internal UserId from the card issuance API body
     * @return true if IVR was successfully skipped
     */
    @Step("Skip IVR verification for card issuance — userId: {userId}")
    public static boolean skipCardIssuanceIvr(String userId) {
        String requestId = getCardIssuanceRequestId(userId);
        if (requestId == null || requestId.isEmpty()) {
            log.error("Failed to get card issuance request ID from DB for userId: {}", userId);
            return false;
        }

        log.info("Card issuance request ID: {}", requestId);
        return callIvrCallback(requestId);
    }

    /**
     * Query Oracle DB (EAIR.EAI_MESSAGE_DUMP) for the x-request-id
     * of the most recent CardIssuanceInitiateRq_Rule flow.
     * Searches by UserId in the JSON message body (new API structure).
     */
    @Step("Query DB for card issuance request ID — userId: {identifier}")
    public static String getCardIssuanceRequestId(String identifier) {
        ConfigManager config = ConfigManager.getInstance();
        String dbUrl = config.get("ivr.db.url", "jdbc:oracle:thin:@//192.168.100.101:1521/ESBAUDIT_DBSIT");
        String dbUser = config.get("ivr.db.user", "ESBQA");
        String dbPassword = config.get("ivr.db.password", "YFnK#9qy2");

        log.info("========== DB CONNECTION CONFIG ==========");
        log.info("DB URL: {}", dbUrl);
        log.info("DB User: {}", dbUser);
        log.info("DB Password: {}", dbPassword.replaceAll(".", "*"));
        log.info("===========================================");

        // Diagnostic query: prints all recent CardIssuance records
        String diagQuery = "SELECT md_creation_tmstmp, MD_FLOW_ID, SUBSTR(md_msg_data, 1, 500) AS msg_preview "
                + "FROM EAIR.EAI_MESSAGE_DUMP "
                + "WHERE md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE "
                + "AND MD_FLOW_ID = 'CardIssuanceInitiateRq_Rule' "
                + "ORDER BY md_creation_tmstmp DESC";

        // Main query: get x-request-id from the most recent CardIssuance record
        String mainQuery = "SELECT JSON_VALUE(md_msg_data, '$.headers.\"x-request-id\"') AS x_request_id "
                + "FROM (SELECT md_msg_data, md_creation_tmstmp FROM EAIR.EAI_MESSAGE_DUMP "
                + "WHERE md_creation_tmstmp >= SYSDATE - INTERVAL '10' MINUTE "
                + "AND MD_FLOW_ID = 'CardIssuanceInitiateRq_Rule' "
                + "ORDER BY md_creation_tmstmp DESC) WHERE ROWNUM = 1";

        log.info("========== DB QUERIES ==========");
        log.info("Diagnostic: {}", diagQuery);
        log.info("Main: {}", mainQuery);
        log.info("================================");

        try {
            log.info("Connecting to Oracle DB...");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            log.info("DB connection established successfully");

            // Run diagnostic query first
            log.info("========== DIAGNOSTIC: Recent CardIssuance records ==========");
            try (Statement diagStmt = conn.createStatement();
                 ResultSet diagRs = diagStmt.executeQuery(diagQuery)) {
                int rowCount = 0;
                while (diagRs.next()) {
                    rowCount++;
                    log.info("Row {}: timestamp={}, flowId={}, preview={}",
                            rowCount,
                            diagRs.getString("md_creation_tmstmp"),
                            diagRs.getString("MD_FLOW_ID"),
                            diagRs.getString("msg_preview"));
                }
                if (rowCount == 0) {
                    log.warn("NO CardIssuanceInitiateRq_Rule records found in last 10 minutes!");
                } else {
                    log.info("Found {} record(s)", rowCount);
                }
            }
            log.info("=============================================================");

            // Run main query to get x-request-id
            log.info("Executing main query...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(mainQuery);

            String requestId = null;
            if (rs.next()) {
                requestId = rs.getString("x_request_id");
                log.info("========== DB QUERY RESULT ==========");
                log.info("x_request_id (CardResourceID): {}", requestId);
                log.info("=====================================");
            } else {
                log.warn("========== DB QUERY RESULT ==========");
                log.warn("No rows returned for identifier: {}", identifier);
                log.warn("=====================================");
            }

            rs.close();
            stmt.close();
            conn.close();
            log.info("DB connection closed");
            return requestId;
        } catch (SQLException e) {
            log.error("DB query failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Call the IVR callback API to simulate the phone verification being accepted.
     * This is the same as Katalon's CardsAndPayemntIVRSKIP.
     */
    @Step("Call IVR callback API to skip verification — requestId: {requestId}")
    public static boolean callIvrCallback(String requestId) {
        ConfigManager config = ConfigManager.getInstance();
        String callbackUrl = config.get("ivr.callback.url", "https://192.168.100.67:7700/v1/ivr/callback/initiate");

        String body = "{"
                + "\"referenceId\":\"" + requestId + "\","
                + "\"consumerResponse\":\"Accepted\","
                + "\"action\":\"01\""
                + "}";

        log.info("Calling IVR callback API: {}", callbackUrl);
        log.info("========== IVR CALLBACK REQUEST ==========");
        log.info("Request body: {}", body);
        log.info("Headers: X-Request-Id={}, x-consumer-app-id=WALLET_IVR_APP, x-consumer-org-id=IVR", requestId);
        log.info("===========================================");

        try {
            // Disable SSL verification for internal SIT endpoints
            RestAssured.useRelaxedHTTPSValidation();

            Response response = RestAssured.given()
                    .header("X-Request-Id", requestId)
                    .header("x-app-version", "1.2.3")
                    .header("x-device-name", "test_device")
                    .header("x-device-platform", "Android")
                    .header("x-latitude", "34.5333")
                    .header("x-longitude", "34.3455")
                    .header("x-session-language", "EN")
                    .header("x-global-transaction-id", "3e42689f678cf725057b3483")
                    .header("x-consumer-app-id", "WALLET_IVR_APP")
                    .header("x-consumer-org-id", "IVR")
                    .header("Content-Type", "application/json")
                    .body(body)
                    .post(callbackUrl);

            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();

            log.info("========== IVR CALLBACK RESPONSE ==========");
            log.info("Response Code: {}", statusCode);
            log.info("Response Body: {}", responseBody);
            log.info("============================================");

            if (statusCode >= 200 && statusCode < 300) {
                log.info("**********Initiate IVR EXECUTION ENDED — SUCCESS**********");
                // Match Katalon: Thread.sleep(20000) after successful IVR callback
                log.info("Waiting 20 seconds after IVR callback (matching Katalon behavior)...");
                try { Thread.sleep(20000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                return true;
            } else {
                log.error("IVR callback failed with status: {}", statusCode);
                return false;
            }
        } catch (Exception e) {
            log.error("IVR callback API call failed: {}", e.getMessage());
            return false;
        }
    }
}
