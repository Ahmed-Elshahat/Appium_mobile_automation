package com.urpay.tests.wallet;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.NotificationsFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.NotificationsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Validates the Notifications bell icon and the Notifications screen.
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/ValidateNotificationsButton
 *
 * Katalon test cases covered:
 *   1. SetupTestdataforNotificationsButton → handled via notifications.* properties
 *   2. ToValidateLoginForRemoteDevicesOnly → handled via LoginFlow.loginWith()
 *   3. OpenNotificationsandvalidatetheview → testOpenNotificationsAndValidateView()
 */
public class NotificationsTest extends BaseTest {

    private DashboardPage dashboard;
    private NotificationsFlow notificationsFlow;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setupDriver")
    public void loginAndSetup() {
        ConfigManager c = ConfigManager.getInstance();
        String mobile = c.get("notifications.mobileNumber", c.get("urpayUser.mobileNumber"));
        String id = c.get("notifications.id", c.get("urpayUser.id"));
        String otp = c.get("notifications.verificationCode", c.get("urpayUser.verificationCode", "1234"));
        String passcode = c.get("notifications.passCode", c.get("urpayUser.passCode", "2233"));

        dashboard = new LoginFlow().loginWith(mobile, id, otp, passcode);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard must load before Notifications tests");
        notificationsFlow = new NotificationsFlow();
    }

    @Test(groups = {"smoke", "wallet", "notifications"})
    @Description("Open Notifications screen and verify header is displayed with correct text")
    @Severity(SeverityLevel.CRITICAL)
    public void testNotificationsScreenLoads() {
        // Act
        NotificationsPage notifications = notificationsFlow.openNotificationsFrom(dashboard);

        // Assert
        Assert.assertTrue(notifications.isLoaded(), "Notifications screen should be loaded");
        Assert.assertEquals(notifications.getHeaderText(), "Notifications",
                "Header text should be 'Notifications'");
    }

    @Test(groups = {"smoke", "wallet", "notifications"}, dependsOnMethods = "testNotificationsScreenLoads")
    @Description("Verify Transactions tab is displayed with correct label")
    @Severity(SeverityLevel.NORMAL)
    public void testTransactionsTabVisible() {
        // Arrange
        NotificationsPage notifications = notificationsFlow.openNotificationsFrom(dashboard);

        // Assert
        Assert.assertEquals(notifications.getTransactionsTabText(), "Transactions",
                "Transactions tab label should be 'Transactions'");
    }

    @Test(groups = {"smoke", "wallet", "notifications"}, dependsOnMethods = "testNotificationsScreenLoads")
    @Description("Verify first transaction notification is visible with correct header text")
    @Severity(SeverityLevel.NORMAL)
    public void testFirstTransactionNotificationVisible() {
        // Arrange
        NotificationsPage notifications = notificationsFlow.openNotificationsFrom(dashboard);

        // Assert
        Assert.assertTrue(notifications.isFirstTransactionNotificationVisible(),
                "First transaction notification should be visible");
        Assert.assertEquals(notifications.getFirstTransactionNotificationHeader(),
                "New International benefciary",
                "First notification header should match expected text");
    }

    @Test(groups = {"smoke", "wallet", "notifications"}, dependsOnMethods = "testNotificationsScreenLoads")
    @Description("Verify Promotions tab shows 'No Notifications' when empty")
    @Severity(SeverityLevel.NORMAL)
    public void testPromotionsTabNoNotifications() {
        // Arrange
        NotificationsPage notifications = notificationsFlow.openNotificationsFrom(dashboard);

        // Act
        Assert.assertEquals(notifications.getPromotionsTabText(), "Promotions",
                "Promotions tab label should be 'Promotions'");
        notificationsFlow.switchToPromotionsTab(notifications);

        // Assert
        Assert.assertTrue(notifications.isNoNotificationsTextVisible(),
                "'No Notifications' text should be visible on Promotions tab");
        Assert.assertEquals(notifications.getNoNotificationsText(), "No Notifications",
                "Empty state text should be 'No Notifications'");
    }

    @Test(groups = {"smoke", "wallet", "notifications"}, dependsOnMethods = "testNotificationsScreenLoads")
    @Description("Verify back button returns user to Dashboard from Notifications")
    @Severity(SeverityLevel.CRITICAL)
    public void testBackButtonReturnsToDashboard() {
        // Arrange
        NotificationsPage notifications = notificationsFlow.openNotificationsFrom(dashboard);

        // Act
        DashboardPage returnedDashboard = notificationsFlow.navigateBackToDashboard(notifications);

        // Assert
        Assert.assertTrue(returnedDashboard.isLoaded(),
                "Dashboard should be loaded after pressing back from Notifications");
    }
}
