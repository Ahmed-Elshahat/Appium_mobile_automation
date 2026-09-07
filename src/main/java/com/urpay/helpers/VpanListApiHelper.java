package com.urpay.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;

import io.restassured.response.Response;

    public final class VpanListApiHelper {

    private static final Logger log =
            LoggerFactory.getLogger(VpanListApiHelper.class);

    private VpanListApiHelper() {}

    public static String getListVpanToken(
            RegistrationApiHelper.Session session,
            String consumerId) {

        // Ground truth from a REAL successful capture (SoapUI/Katalon log against this same SIT
        // env): the Cards List endpoint is on port 14302 (same gateway/port as Activation
        // Initiate) — NOT 14301. 14301 returned "X-Backside-Transport: FAIL FAIL" (an IBM
        // DataPower gateway-to-backend routing failure) with an empty body/no token.
        String baseUrl = ConfigManager.getInstance()
                .get("cardsList.baseUrl", "https://192.168.100.71:14302/walletapppci/v1/cards");
        String endpoint = baseUrl + "/list?ConsumerId=" + consumerId;

        // Header set matches the real successful capture exactly — NOT the earlier guessed set
        // (X-Client-Secret/Accept/Authorization/Fp-Lat-Long/Lat-Long are absent from the real
        // trace and were removed). X-Encryption-Key is a fixed constant echoed back unchanged.
        log.info("API Request > GET {} (ConsumerId={})", endpoint, consumerId);

        Response response = RegistrationApiHelper.authedRequest(session)
                .header("X-Principle-Id", "78988180-c573-413d-9ec4-c78a0e7b1c92")
                .header("X-Principle-Type", "consumer")
                .header("X-Encryption-Key", ConfigManager.getInstance().get("cardsList.encryptionKey",
                        "KAjy+9lWISrsakpA1Dwx45xMk/IPlGW9qb6/e8OS+U5PTkwkfdoIdhFCezImU/3jkgrorlN3PH+"
                        + "XqofrL0AFqi0L5by2+2mKtpvz/rUEUwGAb+Mhc8EF4KPh9uQHyUjSUWo5LUWCzQRGPQtZI72Y9Q3bC"
                        + "lJd2TDi8Bi22Ac45HQ="))
                .log().all() // dump the FULL outgoing request (all headers) to the run log
                .get(endpoint);

        log.info("VPAN List Status Code: {}",
                response.getStatusCode());
        log.info("VPAN List Response Headers: {}", response.getHeaders());
        log.info("VPAN List Response Body: {}", response.getBody().asString());

        // Get response header
        String listVpanToken =
                response.getHeader("X-List-VPAN-Token");

        log.info("X-List-VPAN-Token: {}",
                listVpanToken);

        if (listVpanToken == null) {
            // No header on the response — dump the body so the actual cause (wrong ConsumerId,
            // missing auth header, gateway error, etc.) is visible instead of guessing again.
            log.warn("Cards List response had no X-List-VPAN-Token header. Status={} Body={}",
                    response.getStatusCode(), response.getBody().asString());
        }

        return listVpanToken;
    }
}



