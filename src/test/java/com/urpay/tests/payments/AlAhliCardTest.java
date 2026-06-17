package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Al-Ahli Club Card Management — extends AbstractCardTest with "alahliCard" config prefix.
 * All test logic inherited from AbstractCardTest.
 * Test Data: sit-cards.properties (alahliCard.*)
 */
@Epic("Payments & Cards")
@Feature("Al-Ahli Club Card Management")
public class AlAhliCardTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "alahliCard";
    }
}
