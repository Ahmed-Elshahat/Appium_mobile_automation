package com.urpay.tests.auth;

/**
 * Forgot Passcode — Default tier wrong-passcode negative cases.
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/DefaultTier/ForgotPasscodeDefaultTier-NagativeCases -02
 */
public class ForgotPasscodeDefaultTierLoginNegativeTest extends AbstractForgotPasscodeLoginNegativeTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeDefaultNegPasscode";
    }
}
