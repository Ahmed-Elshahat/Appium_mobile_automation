package com.urpay.tests.settings;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

/**
 * Change Passcode — Visitor Tier.
 *
 * Migrated from Katalon suite:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/VisitorTier/ChangePasscodeVisitorTier
 *
 * Login user 0576012389 / 4949708020. The full linear journey lives in
 * {@link AbstractChangePasscodeTierTest}; this class only binds the config-key prefix.
 */
@Epic("Wallet & VAS")
@Feature("Change Passcode — Visitor Tier")
public class ChangePasscodeVisitorTierTest extends AbstractChangePasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "changePasscodeVisitor";
    }
}
