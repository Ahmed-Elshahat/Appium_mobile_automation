package com.urpay.tests.payments;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Platinum Card Management with fresh user registration.
 *
 * Registers a new full-tier NAT user via backend API before each suite run,
 * then issues a Platinum card for that brand-new user. This ensures:
 *   - No pre-existing card (IVR flow always triggers)
 *   - Clean state (no leftover locks/settings from prior runs)
 *   - Full end-to-end coverage: registration → card issuance → card management
 *
 * Requires:
 *   - VPN / network access to SIT backend (192.168.100.71:14301)
 *   - Oracle DB access for IVR skip (192.168.100.101:1521)
 *   - Backend simulator (neoleap-backend-simulator-sit)
 */
@Epic("Payments & Cards")
@Feature("Platinum Card Management (Fresh Registration)")
public class PlatinumCardRegistrationTest extends AbstractCardTest {

    @Override
    protected String getCardPrefix() {
        return "platinumCard";
    }

    @Override
    protected boolean useRegistration() {
        return true;
    }
}
