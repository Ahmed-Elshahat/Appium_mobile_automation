package com.urpay.tests.auth;

/**
 * Forgot Passcode — Default tier (IQA user).
 *
 * Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/FamilyTier/DefaultTier/ForgotPasscodeDefaultTier
 */
public class ForgotPasscodeDefaultTierTest extends AbstractForgotPasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscode";
    }
}
