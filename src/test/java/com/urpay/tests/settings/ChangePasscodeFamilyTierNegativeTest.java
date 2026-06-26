package com.urpay.tests.settings;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Change Passcode — Family Tier NEGATIVE cases.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/FamilyTier/ChangePasscodeFamilyTier-NegativeCases
 *
 * NOTE: the Katalon FamilyTier-NegativeCases suite references the same
 * {@code ChangePasscodeForDefaultUser/setUpTestData -NegativeCases} setup as every other negative
 * suite, so it runs the Default negative user. The config prefix below is populated with those same
 * creds. The linear negative journey lives in {@link AbstractChangePasscodeTierNegativeTest}.
 */
@Epic("Wallet & VAS")
@Feature("Change Passcode — Family Tier (Negative)")
public class ChangePasscodeFamilyTierNegativeTest extends AbstractChangePasscodeTierNegativeTest {

    @Override
    protected String keyPrefix() {
        return "changePasscodeFamilyNegative";
    }
}
