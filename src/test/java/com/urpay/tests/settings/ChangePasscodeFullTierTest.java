package com.urpay.tests.settings;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Change Passcode — Full Tier.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/FullTier/ChangePasscodeFullTier
 *
 * Login user 0501113589 / 1421604420. The full linear journey lives in
 * {@link AbstractChangePasscodeTierTest}; this class only binds the config-key prefix.
 */
@Epic("Wallet & VAS")
@Feature("Change Passcode — Full Tier")
public class ChangePasscodeFullTierTest extends AbstractChangePasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "changePasscodeFull";
    }
}
