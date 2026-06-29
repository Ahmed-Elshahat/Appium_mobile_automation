package com.urpay.tests.auth;

/**
 * Forgot Passcode — Family tier negative cases (DOB validation + new-passcode validation).
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/FamilyTier/ForgotPasscodeFamilyTier-NegativeCases
 */
public class ForgotPasscodeFamilyTierNegativeTest extends AbstractForgotPasscodeNegativeJourneyTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeFamilyNeg";
    }
}
