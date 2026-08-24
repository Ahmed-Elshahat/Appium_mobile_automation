package com.urpay.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.response.Response;

    public final class VpanListApiHelper {

    private static final Logger log =
            LoggerFactory.getLogger(VpanListApiHelper.class);

    private VpanListApiHelper() {}

    public static String getListVpanToken(
            RegistrationApiHelper.Session session,
            String consumerId) {

        String endpoint =
                "https://192.168.100.71:14302/walletapppci/v1/cards/vpan/list?ConsumerId="
                        + consumerId;

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
