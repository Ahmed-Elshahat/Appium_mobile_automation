package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Platinum Card Management — extends AbstractCardTest with "platinumCard" config prefix.
 * All test logic inherited from AbstractCardTest.
 * Test Data: sit-cards.properties (platinumCard.*)
 */
@Epic("Payments & Cards")
@Feature("Platinum Card Management")
public class PlatinumCardTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "platinumCard";
    }
}
