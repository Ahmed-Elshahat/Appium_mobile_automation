package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Signature Card Management — extends AbstractCardTest with "signatureCard" config prefix.
 * All test logic inherited from AbstractCardTest.
 * Test Data: sit-cards.properties (signatureCard.*)
 */
@Epic("Payments & Cards")
@Feature("Signature Card Management")
public class SignatureCardTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "signatureCard";
    }
}
