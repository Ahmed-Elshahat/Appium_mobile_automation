package com.urpay.tests.auth;

/**
 * Forgot Passcode — Family tier.
 *
 * Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/FamilyTier/ForgotPasscodeFamilyTier
 */
public class ForgotPasscodeFamilyTierTest extends AbstractForgotPasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeFamily";
    }
}
