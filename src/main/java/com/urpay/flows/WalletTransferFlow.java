package com.urpay.flows;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.WalletTransferPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Wallet Transfer flow — peer-to-peer money transfer between URPay wallets.
 *
 * Navigation: Dashboard → Transfer tab → Wallet Transfer → beneficiary search.
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/WalletTran/WalletTransfer/
 *   Scripts/Remittance/WalletTran/TransferToUnsavedNumber/
 *   Scripts/Remittance/WalletTran/SearchForWalletBeneficiary/
 *
 * Supported workflows:
 *   - Search for a wallet beneficiary
 *   - Transfer to an unsaved (non-beneficiary) mobile number end-to-end
 *
 * Rules:
 *   - ZERO Thread.sleep() — all waits via WaitUtils.
 *   - NO assertions (returns page objects for the test to verify).
 *   - NO hardcoded data (reads from ConfigManager).
 */
public class WalletTransferFlow {

    private static final Logger log = LoggerFactory.getLogger(WalletTransferFlow.class);

    // ── Navigation ─────────────────────────────────────
    private static final By TRANSFER_NAV =
            AppiumBy.accessibilityId("testID-TRANSFER");
    private static final By WALLET_TRANSFER_BTN =
            AppiumBy.accessibilityId("testID-viewElemenWalletTransfer");
    private static final By SEARCH_INPUT =
            AppiumBy.accessibilityId("testID-Search-Input");

    // ── Optional system / app popups ───────────────────
    /** Android runtime contact-permission dialog "Allow" button. */
    private static final By CONTACT_ALLOW = AppiumBy.xpath(
            "//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button'] | "
            + "//*[@resource-id='com.android.packageinstaller:id/permission_allow_button'] | "
            + "//*[@text='Allow' or @text='ALLOW' or @text='While using the app']");

    /** "Continue" confirmation shown when transferring to a non-registered URPay user. */
    private static final By CONTINUE_BTN = AppiumBy.xpath(
            "//*[@text='Continue' or starts-with(@text,'Continue')]");

    // ── Result markers ─────────────────────────────────
    private static final By INSUFFICIENT_BALANCE = AppiumBy.xpath(
            "//*[contains(@text,'Insufficient') or contains(@label,'Insufficient')]");

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public WalletTransferFlow() {
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

    @Step("Navigate to Wallet Transfer: Dashboard → Transfer → Wallet Transfer")
    public WalletTransferPage navigateToWalletTransfer() {
        returnToDashboard();

        // Open the Transfer landing — prefer the stable accessibility id, fall back to
        // the bottom-nav coordinate tap if the build does not expose testID-TRANSFER.
        if (!quickTap(TRANSFER_NAV)) {
            log.info("testID-TRANSFER not found — using bottom-nav coordinate tap");
            dashboardPage.navigateToTransfer();
        }

        waits.waitForClickable(WALLET_TRANSFER_BTN, 15).click();
        waits.waitForVisible(SEARCH_INPUT, 15);
        log.info("Wallet Transfer beneficiary screen loaded");
        return new WalletTransferPage();
    }

    /** Back out of any sub-screen until the dashboard (wallet balance) is visible. */
    private void returnToDashboard() {
        // testID-master-amount-main (wallet balance) is a dashboard-ONLY marker. The
        // generic search/right-icon also appears on sub-screens and gives false positives,
        // so backing out would stop early and never reach the real dashboard.
        By balance = AppiumBy.accessibilityId("testID-master-amount-main");
        for (int i = 0; i < 6; i++) {
            if (waits.isPresent(balance, 2)) {
                break;
            }
            driver.navigate().back();
        }
        if (!waits.isPresent(balance, 2)) {
            dashboardPage.navigateToHome();
            waits.waitForVisible(balance, 10);
        }
        dashboardPage.dismissPopups();
    }

    // ══════════════════════════════════════════════════
    //  SEARCH BENEFICIARY
    // ══════════════════════════════════════════════════

    @Step("Search wallet beneficiary: {term}")
    public WalletTransferPage searchWalletBeneficiary(String term) {
        WalletTransferPage page = navigateToWalletTransfer();
        grantContactPermission();
        page.tapSearch();
        page.searchBeneficiary(term);
        platformActions.dismissKeyboard();
        log.info("Searched wallet beneficiary: {}", term);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  TRANSFER TO UNSAVED NUMBER (end-to-end)
    // ══════════════════════════════════════════════════

    @Step("Transfer {amount} to unsaved number {recipientMobile}")
    public WalletTransferPage transferToUnsavedNumber(String recipientMobile, String amount, String otp) {
        WalletTransferPage page = navigateToWalletTransfer();
        grantContactPermission();

        // Surface the "Transfer to unsaved number" option by searching the recipient.
        String localRecipient = toLocalFormat(recipientMobile);
        page.tapSearch();
        page.searchBeneficiary(localRecipient);
        platformActions.dismissKeyboard();
        page.tapTransferToUnsavedNumber();

        // Confirmation shown when the recipient is not a registered URPay user.
        acceptUnsavedNumberPopup();

        // Recipient mobile number (local format — leading zero dropped).
        page.enterRecipientNumber(localRecipient);
        platformActions.dismissKeyboard();
        page.tapNext();

        // Amount.
        page.enterAmount(amount);
        platformActions.dismissKeyboard();
        page.tapNext();

        // Purpose / relationship of transfer → Next → Confirm.
        page.selectFirstPurpose();
        page.tapNext();

        waits.waitForVisible(AppiumBy.accessibilityId("testID-primary-confirm-main"), 15);
        page.tapConfirm();

        // Fail fast on insufficient funds rather than timing out later.
        if (waits.isPresent(INSUFFICIENT_BALANCE, 3)) {
            throw new IllegalStateException(
                    "INSUFFICIENT BALANCE — sender wallet (" + recipientMobile
                    + " transfer of " + amount + ") lacks funds. Top up before running.");
        }

        // OTP / verification code → success screen.
        otpPage.enterOtp(otp);
        common.waitForResultAfterOtp(30);
        log.info("Wallet transfer of {} to {} completed", amount, recipientMobile);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    /** Strip a single leading zero so a KSA mobile number is in local (5xxxxxxxx) form. */
    private String toLocalFormat(String mobile) {
        return mobile != null && mobile.startsWith("0") ? mobile.substring(1) : mobile;
    }

    @Step("Grant contact permission if prompted")
    private void grantContactPermission() {
        if (quickTap(CONTACT_ALLOW)) {
            log.info("Granted contact permission");
        }
    }

    @Step("Accept transfer-to-unsaved-number confirmation if shown")
    private void acceptUnsavedNumberPopup() {
        if (quickTap(CONTINUE_BTN)) {
            log.info("Accepted unsaved-number 'Continue' confirmation");
        }
    }

    /**
     * Tap the first matching element only if it is already on screen — never waits.
     * Drops the implicit wait to zero so an absent element returns instantly.
     */
    private boolean quickTap(By locator) {
        long implicit = ConfigManager.getInstance().getInt("timeout", 10);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            List<WebElement> els = driver.findElements(locator);
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                els.get(0).click();
                return true;
            }
        } catch (Exception ignored) {
            // not present / went stale — nothing to tap
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicit));
        }
        return false;
    }
}
