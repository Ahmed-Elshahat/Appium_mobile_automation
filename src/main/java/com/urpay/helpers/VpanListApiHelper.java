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

        // Ground truth from Katalon (Login.groovy "Cards List" step): the X-List-VPAN-Token
        // comes from the Cards LIST endpoint on port 14301 — NOT the "vpan/list" endpoint on
        // 14302 previously called here, which returned an unrelated/invalid token and made the
        // Activation Initiate API fail with 400 "E200978: Authorization Error."
        String baseUrl = ConfigManager.getInstance()
                .get("cardsList.baseUrl", "https://192.168.100.71:14301/walletapppci/v1/cards");
        String endpoint = baseUrl + "/list?ConsumerId=" + consumerId;

        // The PCI Cards gateway (unlike the plain walletapp auth gateway that authedRequest()
        // targets) needs the same fuller header set Katalon sends for this call — without them
        // it silently returns 200 with an EMPTY body/no X-List-VPAN-Token instead of an error.
        Response response = RegistrationApiHelper.authedRequest(session)
                .header("X-Client-Secret", "64")
                .header("Accept", "application/json, text/plain, */*")
                .header("Authorization", "Bearer undefined")
                .header("X-Fp-Latitude", "24.7111")
                .header("X-Fp-Longitude", "46.6753")
                .header("X-Latitude", "24.7111")
                .header("X-Longitude", "46.6753")
                .header("X-Principle-Id", "78988180-c573-413d-9ec4-c78a0e7b1c92")
                .header("X-Principle-Type", "consumer")
                .get(endpoint);

        log.info("VPAN List Status Code: {}",
                response.getStatusCode());

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

