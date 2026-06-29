package com.urpay.tests.auth;

/**
 * Forgot Passcode — Visitor tier (BOR user).
 *
 * Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/VisitorTier/ForgotPasscodeVisitorTier
 */
public class ForgotPasscodeVisitorTierTest extends AbstractForgotPasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeVisitor";
    }
}
