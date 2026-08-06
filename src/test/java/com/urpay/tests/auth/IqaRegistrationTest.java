package com.urpay.tests.auth;

import com.urpay.helpers.RegistrationApiHelper.PoiType;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Resident / Iqama (IQA) registration test.
 *
 * Migrated from Katalon: RegisterUrpayUser.toRegisterUrpayUser("IQA")
 * Drives the full in-app registration journey for an Iqama (resident) user.
 *
 * Prerequisites: Tahaqoq + Yakeen simulators seeded in @BeforeClass.
 * Run: mvn test -Dsuite=suites/registration-iqa.xml -Dprofile=sit-wmv
 */
@Epic("Auth")
@Feature("Registration — Resident/Iqama (IQA)")
public class IqaRegistrationTest extends AbstractRegistrationTierTest {

    @Override
    protected PoiType poiType() {
        return PoiType.IQA;
    }
}
