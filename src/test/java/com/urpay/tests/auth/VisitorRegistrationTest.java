package com.urpay.tests.auth;

import com.urpay.helpers.RegistrationApiHelper.PoiType;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Visitor (BOR — Border Number) registration test.
 *
 * Migrated from Katalon: RegisterUrpayUser.toRegisterUrpayUser("BOR")
 * Drives the full in-app registration journey for a Visitor (border number) user.
 *
 * Extra step vs NAT/IQA: the app shows a Date of Birth validation screen after OTP.
 * DOB configured via {@code registration.visitor.dob} (default {@code 1994-01-22}).
 *
 * Prerequisites: Tahaqoq simulator seeded in @BeforeClass (Yakeen skipped for BOR).
 * Run: mvn test -Dsuite=suites/registration-visitor.xml -Dprofile=sit-wmv
 */
@Epic("Auth")
@Feature("Registration — Visitor (BOR)")
public class VisitorRegistrationTest extends AbstractRegistrationTierTest {

    @Override
    protected PoiType poiType() {
        return PoiType.BOR;
    }
}
