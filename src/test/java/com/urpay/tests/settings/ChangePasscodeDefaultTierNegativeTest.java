package com.urpay.tests.settings;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Change Passcode — Default Tier NEGATIVE cases.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/DefaultTier/ChangePasscodeDefaultTier-NegativeCases
 *
 * The full linear negative journey lives in {@link AbstractChangePasscodeTierNegativeTest};
 * this class only binds the config-key prefix.
 */
@Epic("Wallet & VAS")
@Feature("Change Passcode — Default Tier (Negative)")
public class ChangePasscodeDefaultTierNegativeTest extends AbstractChangePasscodeTierNegativeTest {

    @Override
    protected String keyPrefix() {
        return "changePasscodeNegative";
    }
}
