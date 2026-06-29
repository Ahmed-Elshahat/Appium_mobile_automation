package com.urpay.tests.auth;

/**
 * Forgot Passcode — Visitor tier wrong-passcode negative cases.
 *
 * Migrated from Katalon suite:
 *   ResetPasscode/ForgotPasscode/VisitorTier/ForgotPasscodeVisitorTier-NagativeCases -02
 */
public class ForgotPasscodeVisitorTierLoginNegativeTest extends AbstractForgotPasscodeLoginNegativeTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeVisitorNegPasscode";
    }
}
