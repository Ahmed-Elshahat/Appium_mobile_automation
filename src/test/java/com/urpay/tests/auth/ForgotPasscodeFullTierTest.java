package com.urpay.tests.auth;

/**
 * Forgot Passcode — Full tier (NAT user).
 *
 * Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/FullTier/ForgotPasscodeFullTier
 */
public class ForgotPasscodeFullTierTest extends AbstractForgotPasscodeTierTest {

    @Override
    protected String keyPrefix() {
        return "forgotPasscodeFull";
    }
}
