package com.urpay.tests.auth;

/**
 * Forgot Passcode — Visitor tier negative cases (DOB validation + new-passcode validation).
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/VisitorTier/ForgotPasscodeVisitorTier-NagativeCases
 */
public class ForgotPasscodeVisitorTierNegativeTest extends AbstractForgotPasscodeNegativeJourneyTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeVisitorNeg";
    }
}
