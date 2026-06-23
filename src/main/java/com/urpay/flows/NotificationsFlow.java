package com.urpay.flows;

import com.urpay.core.DriverFactory;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.NotificationsPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifications flow — opens Notifications from Dashboard and navigates tabs.
 *
 * Migrated from Katalon:
 *   Test Cases/Wallet_VAS/NotificationsButton/OpenNotificationsandvalidatetheview
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns page objects / values for tests to verify)
 *   - NO hardcoded credentials
 */
public class NotificationsFlow {

    private static final Logger log = LoggerFactory.getLogger(NotificationsFlow.class);

    private final AppiumDriver driver;
    private final WaitUtils waits;

    public NotificationsFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
    }

    /**
     * Open Notifications from Dashboard and return the page object.
     *
     * @param dashboard the current DashboardPage
     * @return NotificationsPage ready for assertions
     */
    @Step("Open Notifications from Dashboard")
    public NotificationsPage openNotificationsFrom(DashboardPage dashboard) {
        NotificationsPage notificationsPage = new NotificationsPage();
        notificationsPage.openNotifications();
        log.info("Opened Notifications screen");
        return notificationsPage;
    }

    /**
     * Navigate to Promotions tab from the Notifications screen.
     *
     * @param notificationsPage the current NotificationsPage
     * @return same NotificationsPage (now showing Promotions tab)
     */
    @Step("Switch to Promotions tab")
    public NotificationsPage switchToPromotionsTab(NotificationsPage notificationsPage) {
        notificationsPage.tapPromotionsTab();
        log.info("Switched to Promotions tab");
        return notificationsPage;
    }

    /**
     * Navigate back from Notifications to Dashboard.
     *
     * @param notificationsPage the current NotificationsPage
     * @return DashboardPage after navigating back
     */
    @Step("Navigate back from Notifications to Dashboard")
    public DashboardPage navigateBackToDashboard(NotificationsPage notificationsPage) {
        notificationsPage.tapBack();
        log.info("Navigated back to Dashboard from Notifications");
        return new DashboardPage();
    }
}
