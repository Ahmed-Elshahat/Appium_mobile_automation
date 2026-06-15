package com.urpay.tests.auth;

import com.urpay.core.BaseTest;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Login smoke test — uses LoginFlow for reusable authentication.
 * Clean: arrange → act → assert. No locators. No driver plumbing.
 */
public class SmokeTest extends BaseTest {

    @Test(groups = {"smoke", "auth"})
    @Description("Valid login with default user → dashboard displayed")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginShowsDashboard() {
        // Act
        DashboardPage dashboard = new LoginFlow().loginDefault();

        // Assert
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard must show balance after login");
        log.info("LOGIN OK — Dashboard loaded");
    }

    @Test(groups = {"smoke", "auth"}, dependsOnMethods = "testLoginShowsDashboard")
    @Description("Verify balance text is visible on dashboard after login")
    @Severity(SeverityLevel.CRITICAL)
    public void testBalanceVisible() {
        DashboardPage dashboard = new DashboardPage();
        String balance = dashboard.getBalanceText();
        Assert.assertNotNull(balance, "Balance must not be null");
        Assert.assertFalse(balance.isEmpty(), "Balance must not be empty");
        log.info("Balance: {}", balance);
    }
}
