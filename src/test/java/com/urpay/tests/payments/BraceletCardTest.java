package com.urpay.tests.payments;

import com.urpay.flows.CardsFlow;
import com.urpay.pages.payments.CardsPage;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Mada Bracelet Card Management — extends AbstractCardTest with "braceletCard" config prefix.
 * Same 18-test suite as every other card type, but issuance uses the PHYSICAL national-address
 * form (Mada Bracelet is a wearable), not the digital PIN+OTP+IVR flow — see
 * {@link CardsFlow#issueBraceletCard(String)}.
 * Test Data: sit-cards.properties (braceletCard.*)
 */
@Epic("Payments & Cards")
@Feature("Mada Bracelet Card Management")
public class BraceletCardTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "braceletCard";
    }

    @Override
    protected boolean useRegistration() { return true; }

    @Override
    protected CardsPage issueCard(CardsFlow flow) {
        return flow.issueBraceletCard(getCardPrefix());
    }

    @Override
    protected void postIssuanceActivate(CardsFlow flow) {
        flow.activateBraceletCard(getCardPrefix());
    }
}
