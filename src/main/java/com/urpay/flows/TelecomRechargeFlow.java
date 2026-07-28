package com.urpay.flows;

import java.io.ByteArrayInputStream;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SearchPage;
import com.urpay.pages.payments.TelecomRechargePage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

/**
 * Telecom Recharge flow — reusable for Zain, Mobily, STC providers.
 *
 * Navigation: Dashboard → Search "Telecom Recharge" → tap result → Provider page
 * (Katalon uses search-based navigation, not direct Payments tab)
 *
 * Each provider has its own login account (from sit-cards.properties):
 *   Zain:   0520835167 / 2194263444
 *   Mobily: 0520080284 / 2094569585
 *   STC:    0520897136 / 1509065858
 */
public class TelecomRechargeFlow {

    private static final Logger log = LoggerFactory.getLogger(TelecomRechargeFlow.class);
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public TelecomRechargeFlow() {
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

    @Step("Navigate to Telecom Recharge via search")
    public TelecomRechargePage navigateToTelecomRecharge() {
        // Press back until search icon is visible (explicit wait)
        for (int i = 0; i < 3; i++) {
            if (dashboardPage.isSearchIconVisible(2)) {
                break;
            }
            driver.navigate().back();
        }

        SearchPage search = new SearchPage();
        search.searchAndSelect("Telecom Services");

        TelecomRechargePage page = new TelecomRechargePage();
        waits.waitForVisible(
                io.appium.java_client.AppiumBy.accessibilityId("testID-viewElementelecomZAIN2"), 15);
        log.info("Telecom Recharge page loaded");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  ZAIN RECHARGE
    // ══════════════════════════════════════════════════

    @Step("Complete Zain recharge for: {mobileNumber}")
    public TelecomRechargePage rechargeZain(String mobileNumber) {
        TelecomRechargePage page = navigateToTelecomRecharge();
        page.selectZain();
        page.selectFirstCard();         // zainFirstCard (testID-data-0)
        page.selectFirstPackage();      // sawaFirstBackage (SVG package icon)
        page.scrollDown();               // Katalon swipes down ×2 to reach Next button
        page.scrollDown();
        page.tapNextPackages();         // testID-primary-onPress-main
        page.enterMobileNumber(mobileNumber);
        dismissKeyboard();
        page.tapNextMobile();           // testID-View.b8ce712a — confirm mobile
        page.tapConfirm();              // testID-primary-onConfirm-main — confirm transaction
        enterVerificationCode();
        captureAfterOtp("Zain Recharge - Post OTP");
        waitAfterOtp();
        log.info("Zain recharge submitted for: {}", mobileNumber);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  MOBILY RECHARGE
    // ══════════════════════════════════════════════════

    @Step("Complete Mobily recharge for: {mobileNumber}")
    public TelecomRechargePage rechargeMobily(String mobileNumber) {
        TelecomRechargePage page = navigateToTelecomRecharge();
        page.selectMobily();
        page.selectFirstCard();         // mobilyFirstCard (testID-data-0)
        page.selectFirstPackage();      // sawaFirstBackage
        page.scrollDown();
        page.scrollDown();
        page.tapNextPackages();         // testID-primary-onPress-main
        page.enterMobileNumber(mobileNumber);
        dismissKeyboard();
        page.tapNextMobile();           // confirm mobile
        page.tapConfirm();              // confirm transaction
        enterVerificationCode();
        captureAfterOtp("Mobily Recharge - Post OTP");
        waitAfterOtp();
        log.info("Mobily recharge submitted for: {}", mobileNumber);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  STC RECHARGE
    // ══════════════════════════════════════════════════

    @Step("Complete STC Sawa recharge for: {mobileNumber}")
    public TelecomRechargePage rechargeSawa(String mobileNumber) {
        TelecomRechargePage page = navigateToTelecomRecharge();
        page.selectSTC();
        // STC shows services menu: Sawa Recharge / Quicknet / Sawa Packages
        page.selectSawaRecharge();      // tap "Sawa Recharge" option
        page.selectFirstPackage();      // first recharge card
        page.scrollDown();
        page.scrollDown();
        page.tapNextPackages();         // testID-primary-onPress-main
        page.enterMobileNumber(mobileNumber);
        dismissKeyboard();
        page.tapNextMobile();           // confirm mobile number
        waits.waitForVisible(
                io.appium.java_client.AppiumBy.accessibilityId("testID-master-amount-main"), 15);
        page.tapConfirm();              // confirm transaction
        enterVerificationCode();
        captureAfterOtp("STC Sawa Recharge - Post OTP");
        waitAfterOtp();
        log.info("STC Sawa recharge submitted for: {}", mobileNumber);
        return page;
    }

    @Step("Complete STC QuickNet recharge for: {mobileNumber}")
    public TelecomRechargePage rechargeQuickNet(String mobileNumber) {
        TelecomRechargePage page = navigateToTelecomRecharge();
        page.selectSTC();
        page.selectQuickNet();          // quickNetRechargeOption
        page.selectFirstPackage();      // sawaFirstBackage
        page.scrollDown();
        page.scrollDown();
        page.tapNextPackages();         // testID-primary-onPress-main
        page.enterMobileNumber(mobileNumber);
        dismissKeyboard();
        page.tapNextMobile();           // confirm mobile
        page.tapConfirm();              // confirm transaction
        enterVerificationCode();
        captureAfterOtp("STC QuickNet Recharge - Post OTP");
        waitAfterOtp();
        log.info("STC QuickNet recharge submitted for: {}", mobileNumber);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  ORDER HISTORY
    // ══════════════════════════════════════════════════

    @Step("View order history for first order")
    public TelecomRechargePage viewFirstOrderDetails() {
        TelecomRechargePage page = navigateToTelecomRecharge();
        // Scroll down to Last Orders section, then tap first card
        page.scrollDown();
        page.scrollDown();
        page.scrollDown();
        page.tapFirstOrderCard();
        waits.waitForVisible(
                io.appium.java_client.AppiumBy.accessibilityId("testID-label-value-0"), 15);
        return page;
    }

    @Step("Re-order from current page or Telecom Recharge page")
    public TelecomRechargePage reorderFromHistory() {
        TelecomRechargePage page = new TelecomRechargePage();

        // First try: Reorder button might be on current page (after tapBack from order details)
        // Check both the testID variant AND the text "Reorder" (Order History list buttons)
        java.util.List<org.openqa.selenium.WebElement> btns = waits.findQuick(
                io.appium.java_client.AppiumBy.accessibilityId("testID-primary-action-main"), 3);
        if (btns.isEmpty()) {
            btns = waits.findQuick(
                    io.appium.java_client.AppiumBy.xpath(
                            "//*[@text='Reorder' or contains(@text,'Reorder') or contains(@content-desc,'Reorder')]"), 3);
        }
        if (!btns.isEmpty()) {
            page.tapReorder();
        } else {
            // Navigate to Telecom Recharge and scroll to Last Orders
            page = navigateToTelecomRecharge();
            page.scrollDown();
            page.scrollDown();
            page.scrollDown();
            page.scrollDown();
            page.tapReorder();
        }

        waits.waitForVisible(
                io.appium.java_client.AppiumBy.accessibilityId("testID-master-amount-main"), 15);
        return page;
    }

    @Step("Confirm re-order with verification code")
    public void confirmReorder(TelecomRechargePage page) {
        page.tapReorderConfirm();
        enterVerificationCode();
        captureAfterOtp("Re-order - Post OTP");
        waitAfterOtp();
        page.tapDone();
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Enter verification code")
    private void enterVerificationCode() {
        String code = ConfigManager.getInstance().get("urpayUser.verificationCode", "1234");
        otpPage.enterOtp(code);
    }

    /** Wait for result screen after OTP — success (Done button) or error popup */
    private void waitAfterOtp() {
        common.waitForResultAfterOtp(30);
    }

    /** Capture multiple screenshots immediately after OTP to catch transient error popups */
    @Step("Capture screen after OTP: {name}")
    private void captureAfterOtp(String name) {
        try {
            // Take 3 rapid screenshots over 3 seconds to catch transient error banners
            for (int i = 1; i <= 3; i++) {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (screenshot.length > 0) {
                    Allure.addAttachment(name + " (" + i + "s)", "image/png",
                            new ByteArrayInputStream(screenshot), ".png");
                }
            }
            log.info("Post-OTP screenshots captured: {}", name);
        } catch (Exception e) {
            log.warn("Post-OTP screenshot failed: {}", e.getMessage());
        }
    }

    private void dismissKeyboard() {
        common.dismissKeyboard();
    }
}
