package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Mada Card Management — extends AbstractCardTest with "madaCard" config prefix.
 * All test logic inherited from AbstractCardTest.
 * Test Data: sit-cards.properties (madaCard.*)
 */
@Epic("Payments & Cards")
@Feature("Mada Card Management")
public class MadaCardTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "madaCard";
    }
}
