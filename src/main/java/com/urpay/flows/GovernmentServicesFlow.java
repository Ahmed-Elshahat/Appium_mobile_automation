package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SearchPage;
import com.urpay.pages.payments.GovernmentServicesPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Government Services (Sadad MOI) flow — orchestrates traffic violation queries and payments.
 *
 * Navigation: Dashboard → Search "Government Services" → Gov Services page
 *
 * Migrated from Katalon: Scripts/PaymntAndCards/GovernmentServices/
 *
 * Supported Workflows:
 *   - Navigate to Government Services
 *   - Query single traffic violation by violator ID
 *   - Query multiple traffic violations by violator ID
 *   - Pay single violation
 *   - Pay multiple violations
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns page objects/values for test to verify)
 *   - NO hardcoded credentials (reads from ConfigManager)
 */
public class GovernmentServicesFlow {

    private static final Logger log = LoggerFactory.getLogger(GovernmentServicesFlow.class);

    /** Locator that matches when Government Services page is loaded */
    private static final By GOV_PAGE_LOADED = AppiumBy.xpath(
            "//*[@content-desc='testID-multi-select-category' or @name='testID-multi-select-category'] | "
                    + "//*[@text='Traffic Violations' or @label='Traffic Violations'] | "
                    + "//*[@text='Government Services' or @label='Government Services']");

    /** Locator for violations list loaded */
    private static final By VIOLATIONS_LOADED =
            AppiumBy.accessibilityId("testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0");

    private static final By INSUFFICIENT_BALANCE = AppiumBy.xpath(
            "//*[contains(@text,'Insufficient Balance') or contains(@label,'Insufficient Balance')]");

    /** Locator for confirm payment screen */
    private static final By CONFIRM_SCREEN =
            AppiumBy.accessibilityId("testID-primary-onConfirm-main");

    /** Locator for done/success screen */
    private static final By DONE_SCREEN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onPressDone-main' or @name='testID-primary-onPressDone-main'] | "
                    + "//*[@text='Done' or @label='Done'] | "
                    + "//*[contains(@text,'Success') or contains(@label,'Success')]");

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public GovernmentServicesFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.common = new CommonComponentsPage();
        this.otpPage = new OtpPage();
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Government Services via search")
    public GovernmentServicesPage navigateToGovernmentServices() {
        // Navigate back to dashboard — try back presses first
        for (int i = 0; i < 5; i++) {
            if (dashboardPage.isSearchIconVisible(2)) {
                break;
            }
            driver.navigate().back();
        }

        // If still not on dashboard, tap Home nav bar as fallback
        if (!dashboardPage.isSearchIconVisible(2)) {
            dashboardPage.navigateToHome();
            waits.waitForVisible(
                    AppiumBy.accessibilityId("testID-right-icon-0"), 10);
        }

        // Dismiss banners before search
        dashboardPage.dismissPopups();
        dismissMaintenanceBanner();

        SearchPage search = new SearchPage();
        String serviceName = ConfigManager.getInstance().get("gov.serviceName", "Government Services");

        try {
            search.searchAndSelect(serviceName);
        } catch (Exception e) {
            log.warn("Search blocked by banner, dismissing and retrying");
            dashboardPage.dismissPopups();
            dismissMaintenanceBanner();
            search.searchAndSelect(serviceName);
        }

        GovernmentServicesPage page = new GovernmentServicesPage();
        waits.waitForVisible(GOV_PAGE_LOADED, 20);
        log.info("Government Services page loaded");
        return page;
    }

    /** Dismiss maintenance/consent banners that overlay the dashboard */
    private void dismissMaintenanceBanner() {
        try {
            var bannerClose = waits.findQuick(
                    AppiumBy.xpath("//*[@text='\u00d7' or @text='\u2715' or @text='\u2717' "
                            + "or @text='×' or @text='✕']"), 2);
            if (!bannerClose.isEmpty() && bannerClose.get(0).isDisplayed()) {
                bannerClose.get(0).click();
                log.info("Dismissed maintenance banner");
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════
    //  QUERY VIOLATIONS
    // ══════════════════════════════════════════════════

    @Step("Select Traffic Violations category and service type")
    private GovernmentServicesPage selectTrafficViolationsService(GovernmentServicesPage page) {
        page.tapServiceCategory();
        page.selectTrafficViolations();

        // Wait for service type dropdown to be ready after category selection
        waits.waitForClickable(
                AppiumBy.accessibilityId("testID-multi-select-service"), 10);
        page.tapServiceType();
        page.selectViolationsByViolatorId();

        return page;
    }

    @Step("Enter violator ID and query violations: {violatorId}")
    private GovernmentServicesPage queryViolations(GovernmentServicesPage page, String violatorId) {
        page.enterViolatorId(violatorId);
        platformActions.dismissKeyboard();
        page.tapNext();
        waits.waitForVisible(VIOLATIONS_LOADED, 20);
        log.info("Violations loaded for violator ID: {}", violatorId);
        return page;
    }

    @Step("Query single traffic violation for violator: {violatorId}")
    public GovernmentServicesPage querySingleViolation(String violatorId) {
        GovernmentServicesPage page = navigateToGovernmentServices();
        selectTrafficViolationsService(page);
        queryViolations(page, violatorId);
        page.tapFirstViolation();
        log.info("Single violation selected for violator: {}", violatorId);
        return page;
    }

    @Step("Query multiple traffic violations for violator: {violatorId}")
    public GovernmentServicesPage queryMultipleViolations(String violatorId) {
        GovernmentServicesPage page = navigateToGovernmentServices();
        selectTrafficViolationsService(page);
        queryViolations(page, violatorId);
        page.tapFirstViolation();
        page.tapSecondViolation();
        log.info("Multiple violations selected for violator: {}", violatorId);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  PAY VIOLATIONS
    // ══════════════════════════════════════════════════

    @Step("Pay single traffic violation for violator: {violatorId}")
    public GovernmentServicesPage paySingleViolation(String violatorId) {
        GovernmentServicesPage page = querySingleViolation(violatorId);
        waits.waitForVisible(
                AppiumBy.xpath("//*[contains(@text,'violation') and contains(@text,'selected')]"), 10);
        page.tapPaySelected();
        return completePayment(page);
    }

    @Step("Pay multiple traffic violations for violator: {violatorId}")
    public GovernmentServicesPage payMultipleViolations(String violatorId) {
        GovernmentServicesPage page = queryMultipleViolations(violatorId);
        waits.waitForVisible(
                AppiumBy.xpath("//*[contains(@text,'violation') and contains(@text,'selected')]"), 10);
        page.tapPaySelected();
        return completePayment(page);
    }

    @Step("Complete violation payment: confirm → OTP → done")
    private GovernmentServicesPage completePayment(GovernmentServicesPage page) {
        waits.waitForVisible(CONFIRM_SCREEN, 15);
        page.tapConfirm();

        // Fail fast if wallet has insufficient balance
        if (waits.isPresent(INSUFFICIENT_BALANCE, 3)) {
            throw new RuntimeException(
                    "INSUFFICIENT BALANCE — wallet does not have enough funds. "
                    + "Top up the wallet before running payment tests. "
                    + "Katalon setup script used API to set balance to 9999.");
        }

        enterVerificationCode();
        common.waitForResultAfterOtp(30);

        log.info("Violation payment completed successfully");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  WALLET BALANCE
    // ══════════════════════════════════════════════════

    @Step("Get current wallet balance from dashboard")
    public String getWalletBalance() {
        waits.waitForVisible(
                AppiumBy.accessibilityId("testID-master-amount-main"), 10);
        String integer = driver.findElement(
                AppiumBy.accessibilityId("testID-master-amount-main")).getText();
        String fraction = "";
        try {
            fraction = driver.findElement(
                    AppiumBy.accessibilityId("testID-fraction-amount-main")).getText();
        } catch (Exception ignored) {
            // fraction may not be present
        }
        String balance = integer + fraction;
        log.info("Wallet balance: {}", balance);
        return balance;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Enter verification code")
    private void enterVerificationCode() {
        String code = ConfigManager.getInstance().get("gov.verificationCode", "1234");
        otpPage.enterOtp(code);
    }
}
