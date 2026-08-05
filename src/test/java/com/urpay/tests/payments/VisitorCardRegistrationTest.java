package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Visitor Card Management with fresh user registration.
 *
 * Registers a new full-tier NAT user via backend API before each suite run,
 * then issues a Visitor card (under the "Visa Card" tab) for that brand-new user.
 * Mirrors PlatinumCardRegistrationTest.
 *
 * Requires:
 *   - VPN / network access to SIT backend (192.168.100.71:14301)
 *   - Oracle DB access for IVR skip (192.168.100.101:1521)
 *   - Backend simulator (neoleap-backend-simulator-sit)
 */
@Epic("Payments & Cards")
@Feature("Visitor Card Management (Fresh Registration)")
public class VisitorCardRegistrationTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "visitorCard";
    }

    @Override
    protected boolean useRegistration() {
        return true;
    }
}
