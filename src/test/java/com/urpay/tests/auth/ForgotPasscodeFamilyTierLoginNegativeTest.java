package com.urpay.tests.auth;

/**
 * Forgot Passcode — Family tier wrong-passcode negative cases.
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/FamilyTier/ForgotPasscodeFamilyTier-NegativeCases -02
 */
public class ForgotPasscodeFamilyTierLoginNegativeTest extends AbstractForgotPasscodeLoginNegativeTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeFamilyNegPasscode";
    }
}
