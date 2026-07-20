package com.urpay.reporting;

import java.util.concurrent.atomic.AtomicBoolean;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;

/**
 * Attaches every REST Assured API request + response — HTTP method, URL, headers, body and the
 * <b>status code</b> — to the Allure step it runs under, so data-setup / provisioning API calls
 * (registration, passcode reset, family seeding, wallet balance, MTO corridor …) are visible in
 * the report instead of only the console log.
 *
 * <p>Enable once per run from {@code BaseTest.setupSuite()}; the filter is registered globally so
 * no per-call code is needed.
 *
 * <p>⚠ SECURITY: the attached request/response carry credentials (mobile number, national ID, OTP)
 * and bearer tokens. The single-file Allure report is shareable — treat it as sensitive, or add
 * masking before distributing it externally.
 */
public final class ApiReporting {

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);

    private ApiReporting() { }

    /** Register the Allure REST Assured filter globally (idempotent — safe to call repeatedly). */
    public static void enable() {
        if (ENABLED.compareAndSet(false, true)) {
            RestAssured.filters(new AllureRestAssured());
        }
    }
}
