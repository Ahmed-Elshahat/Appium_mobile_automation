package com.urpay.tests.auth;

/**
 * Forgot Passcode — Default tier negative cases (DOB validation + new-passcode validation).
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/DefaultTier/ForgotPasscodeDefaultTier-NagativeCases
 */
public class ForgotPasscodeDefaultTierNegativeTest extends AbstractForgotPasscodeNegativeJourneyTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeDefaultNeg";
    }
}
