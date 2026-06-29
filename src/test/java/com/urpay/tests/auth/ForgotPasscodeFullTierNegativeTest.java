package com.urpay.tests.auth;

/**
 * Forgot Passcode — Full tier negative cases (Date-of-Birth validation).
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/FullTier/ForgotPasscodeFullTier-NegativeCases
 */
public class ForgotPasscodeFullTierNegativeTest extends AbstractForgotPasscodeDobNegativeTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeFullNeg";
    }
}
