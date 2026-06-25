package com.urpay.tests.settings;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Change Passcode — Family Tier.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/FamilyTier/ChangePasscodeFamilyTier
 *
 * Login user 0520111131 / 1990732818. The full linear journey lives in
 * {@link AbstractChangePasscodeTierTest}; this class only binds the config-key prefix.
 */
@Epic("Wallet & VAS")
@Feature("Change Passcode — Family Tier")
public class ChangePasscodeFamilyTierTest extends AbstractChangePasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "changePasscodeFamily";
    }
}
