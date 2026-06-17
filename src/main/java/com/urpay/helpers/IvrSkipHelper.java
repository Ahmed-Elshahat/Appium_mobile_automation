package com.urpay.helpers;

import com.urpay.core.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

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
     * @param consumerId the consumer/party ID (e.g., from GlobalVariable.urpayUser["consumerId"])
     * @return true if IVR was successfully skipped
     */
    @Step("Skip IVR verification for card issuance — consumer: {consumerId}")
    public static boolean skipCardIssuanceIvr(String consumerId) {
        String requestId = getCardIssuanceRequestId(consumerId);
        if (requestId == null || requestId.isEmpty()) {
            log.error("Failed to get card issuance request ID from DB for consumer: {}", consumerId);
            return false;
        }

        log.info("Card issuance request ID: {}", requestId);
        return callIvrCallback(requestId);
    }

    /**
     * Query Oracle DB (EAIR.EAI_MESSAGE_DUMP) for the x-request-id
     * of the most recent CardIssuanceInitiateRq_Rule flow.
     * Searches by consumerId/partyId in the JSON message body.
     */
    @Step("Query DB for card issuance request ID — identifier: {identifier}")
    public static String getCardIssuanceRequestId(String identifier) {
        ConfigManager config = ConfigManager.getInstance();
        String dbUrl = config.get("ivr.db.url", "jdbc:oracle:thin:@//192.168.100.101:1521/ESBAUDIT_DBSIT");
        String dbUser = config.get("ivr.db.user", "ESBQA");
        String dbPassword = config.get("ivr.db.password", "YFnK#9qy2");

        // Search by partyId (consumerId) — also try matching as UserId
        String query = "SELECT JSON_VALUE(md_msg_data, '$.headers.\"x-request-id\"') AS x_request_id "
                + "FROM (SELECT md_msg_data, md_creation_tmstmp FROM EAIR.EAI_MESSAGE_DUMP "
                + "WHERE md_creation_tmstmp >= TRUNC(SYSDATE - INTERVAL '10' MINUTE) "
                + "AND MD_FLOW_ID = 'CardIssuanceInitiateRq_Rule' "
                + "AND (JSON_EXISTS(md_msg_data, '$.body?(@.partyId == \"" + identifier + "\")') "
                + "  OR JSON_EXISTS(md_msg_data, '$.body?(@.UserId == \"" + identifier + "\")')) "
                + "ORDER BY md_creation_tmstmp DESC) WHERE ROWNUM = 1";

        log.info("Querying DB for card issuance request ID...");
        log.debug("Query: {}", query);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                String requestId = rs.getString("x_request_id");
                log.info("Found card issuance request ID: {}", requestId);
                return requestId;
            } else {
                log.warn("No card issuance record found in DB for identifier: {}", identifier);
                return null;
            }
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
        log.debug("Request body: {}", body);

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

            log.info("IVR callback response: status={}, body={}", statusCode, responseBody);

            if (statusCode >= 200 && statusCode < 300) {
                log.info("IVR skip successful");
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
