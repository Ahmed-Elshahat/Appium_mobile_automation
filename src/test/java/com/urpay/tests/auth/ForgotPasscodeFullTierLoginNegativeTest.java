package com.urpay.tests.auth;

/**
 * Forgot Passcode — Full tier wrong-passcode negative cases.
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/FullTier/ForgotPasscodeFullTier-NegativeCases -02
 */
public class ForgotPasscodeFullTierLoginNegativeTest extends AbstractForgotPasscodeLoginNegativeTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeFullNegPasscode";
    }
}
