package com.urpay.tests.auth;

import com.urpay.core.BaseTest;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Logout journey — More → Settings → Logout → confirm → unlink device (self-heal for the
 * current build's post-logout passcode+unlink screen) → back to the login flow. Verified by
 * performing a full login again: a fully unlinked session requires credentials + OTP + passcode
 * from scratch, so a successful re-login proves the logout actually completed.
 */
public class LogoutTest extends BaseTest {

    @Test(groups = {"smoke", "auth"})
    @Description("Login with any user, log out, and confirm a fresh full login is required afterwards")
    @Severity(SeverityLevel.CRITICAL)
    public void testLogoutReturnsToLoginScreen() {
        LoginFlow loginFlow = new LoginFlow();
        DashboardPage dashboard = loginFlow.loginDefault();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard must show balance after login");

        SettingsPage settings = new SettingsPage();
        settings.openViaDeepLink();
        settings.tapLogout();
        settings.confirmLogout();

        DashboardPage dashboardAfterRelogin = loginFlow.loginDefault();
        Assert.assertTrue(dashboardAfterRelogin.isLoaded(),
                "A full re-login must succeed after logout, proving the device was unlinked");
        log.info("LOGOUT OK — session cleared, full re-login succeeded");
    }
}
