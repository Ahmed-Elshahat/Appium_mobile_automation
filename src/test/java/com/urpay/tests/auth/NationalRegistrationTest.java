package com.urpay.tests.auth;

import com.urpay.helpers.RegistrationApiHelper.PoiType;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * National (NAT / Saudi ID) registration test.
 *
 * Migrated from Katalon: RegisterUrpayUser.toRegisterUrpayUser("NAT")
 * Drives the full in-app registration journey for a Saudi National ID user.
 *
 * Prerequisites: Tahaqoq + Yakeen simulators seeded in @BeforeClass.
 * Run: mvn test -Dsuite=suites/registration-national.xml -Dprofile=sit-wmv
 */
@Epic("Auth")
@Feature("Registration — National (NAT)")
public class NationalRegistrationTest extends AbstractRegistrationTierTest {

    @Override
    protected PoiType poiType() {
        return PoiType.NAT;
    }
}
