package com.urpay.tests.settings;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Change Passcode — Default Tier.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/DefaultTier/ChangePasscodeDefaultTier
 *
 * Login user 0511113789 / 1757914922. The full linear journey lives in
 * {@link AbstractChangePasscodeTierTest}; this class only binds the config-key prefix.
 */
@Epic("Wallet & VAS")
@Feature("Change Passcode — Default Tier")
public class ChangePasscodeDefaultTierTest extends AbstractChangePasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "changePasscode";
    }
}
