package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Visitor Card Management (Mada variant) with fresh user registration.
 *
 * Visitor Card also appears under the "Mada Card" tab (a separate instance from the
 * "Visa Card" tab's Visitor Card handled by VisitorCardRegistrationTest). Same card
 * display name ("Visitor Card") but a different tab, hence a distinct config prefix
 * (visitorMadaCard) and cardTab=Mada Card so CardsFlow scopes the sub-carousel search
 * to the correct tab.
 */
@Epic("Payments & Cards")
@Feature("Visitor Card Management - Mada Tab (Fresh Registration)")
public class VisitorMadaCardRegistrationTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "visitorMadaCard";
    }

    @Override
    protected boolean useRegistration() {
        return true;
    }
}
