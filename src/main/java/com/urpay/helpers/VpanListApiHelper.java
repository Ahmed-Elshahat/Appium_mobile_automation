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

        Response response = RegistrationApiHelper.authedRequest(session)
                .get(endpoint);

        log.info("VPAN List Status Code: {}",
                response.getStatusCode());

        // Get response header
        String listVpanToken =
                response.getHeader("X-List-VPAN-Token");

        log.info("X-List-VPAN-Token: {}",
                listVpanToken);

        return listVpanToken;
    }
}

