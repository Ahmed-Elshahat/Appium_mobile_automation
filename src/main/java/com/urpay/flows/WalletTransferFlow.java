package com.urpay.flows;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.WalletTransactionDetailsPage;
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

    @Step("Navigate to Wallet Transfer (deep link urpay://w2w)")
    public WalletTransferPage navigateToWalletTransfer() {
        // Already on the Wallet Transfer beneficiary screen (e.g. a prior chained test left us
        // here)? Reuse it — re-navigating would be pure waste in a single session.
        if (waits.isVisible(SEARCH_INPUT, 2)) {
            log.info("Already on Wallet Transfer screen — reusing it");
            return new WalletTransferPage();
        }
        // Deep-link straight to Wallet-to-Wallet Transfer. UI back-navigation is fragile from
        // deep stacks (e.g. the transaction-details screen), so mirror Katalon's w2w deep link.
        openViaDeepLink("urpay://w2w");
        waits.waitForVisible(SEARCH_INPUT, 15);
        log.info("Wallet Transfer beneficiary screen loaded (deep link)");
        return new WalletTransferPage();
    }

    /** Back out of any sub-screen until the dashboard (wallet balance) is visible. */
    private void returnToDashboard() {
        // testID-master-amount-main (wallet balance) is a dashboard-ONLY marker. React Native
        // keeps the dashboard mounted BEHIND overlays (e.g. the post-transfer Thank You screen),
        // so a DOM-presence check falsely reports "home" — use VISIBILITY so the back-press loop
        // actually continues until the dashboard is on screen. The 2s budget doubles as settle.
        By balance = AppiumBy.accessibilityId("testID-master-amount-main");
        for (int i = 0; i < 6; i++) {
            if (waits.isVisible(balance, 2)) {
                break;
            }
            driver.navigate().back();
        }
        if (!waits.isVisible(balance, 2)) {
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

    /**
     * Fill the wallet-transfer form up to the Confirm screen — search → unsaved number →
     * recipient → amount → purpose. Stops BEFORE Confirm/OTP (no money moved), so it backs
     * BOTH the full transfer and the recipient name-confirmation validation.
     */
    private WalletTransferPage fillToConfirm(String recipientMobile, String amount) {
        WalletTransferPage page = navigateToWalletTransfer();
        grantContactPermission();

        // Surface the "Transfer to unsaved number" option by searching the recipient.
        String localRecipient = toLocalFormat(recipientMobile);
        page.tapSearch();
        page.searchBeneficiary(localRecipient);
        platformActions.dismissKeyboard();
        page.tapTransferToUnsavedNumber();

        // Confirmation shown when the recipient is not a saved beneficiary.
        acceptUnsavedNumberPopup();

        // Recipient mobile number (local format — leading zero dropped).
        page.enterRecipientNumber(localRecipient);
        platformActions.dismissKeyboard();
        page.tapNext();

        // Amount — the screen auto-focuses a numeric keypad and the underlying field is not
        // a standard EditText, so a quick-amount chip (20/50/100/200) is the reliable input.
        page.isAmountScreenVisible(15);
        page.selectQuickAmount(amount);
        page.tapNext();

        // Purpose / relationship of transfer → Next → Confirm screen.
        page.selectFirstPurpose();
        page.tapNext();

        waits.waitForVisible(AppiumBy.accessibilityId("testID-primary-confirm-main"), 15);
        return page;
    }

    @Step("Transfer {amount} to unsaved number {recipientMobile}")
    public WalletTransferPage transferToUnsavedNumber(String recipientMobile, String amount, String otp) {
        WalletTransferPage page = fillToConfirm(recipientMobile, amount);
        page.tapConfirm();

        // Fail fast on insufficient funds rather than timing out later.
        if (waits.isPresent(INSUFFICIENT_BALANCE, 3)) {
            throw new IllegalStateException(
                    "INSUFFICIENT BALANCE — sender wallet (" + recipientMobile
                    + " transfer of " + amount + ") lacks funds. Top up before running.");
        }

        // OTP / verification code → success screen.
        otpPage.enterOtp(otp);
        platformActions.dismissKeyboard();
        common.waitForResultAfterOtp(30);
        log.info("Wallet transfer of {} to {} completed", amount, recipientMobile);
        return page;
    }

    /**
     * Reach the Confirm screen for an existing URPay user and STOP (no Confirm/OTP — no money).
     * Migrated from ValidateExistingUserPayMobileNameConfirmationPage / ValidateNameConfirmationPage:
     * entering a registered user's number must resolve & show their name on the Confirm screen.
     */
    @Step("Open the transfer Confirm screen for existing user {recipientMobile} (no confirm)")
    public WalletTransferPage openTransferConfirmation(String recipientMobile, String amount) {
        WalletTransferPage page = fillToConfirm(recipientMobile, amount);
        log.info("Reached confirm screen for {} — recipient name confirmation ready", recipientMobile);
        return page;
    }

    // ═══════════════════════════════════════════════════
    //  DIRECT TRANSFER TO A SAVED / ACTIVE BENEFICIARY
    // ═══════════════════════════════════════════════════

    /** Open Wallet Transfer and grant the contact permission so the beneficiary list loads. */
    @Step("Open Wallet Transfer (with contacts)")
    public WalletTransferPage openWalletTransferWithContacts() {
        WalletTransferPage page = navigateToWalletTransfer();
        grantContactPermission();
        return page;
    }

    /**
     * Complete a transfer to the first active/saved beneficiary — select it DIRECTLY (no search,
     * no unsaved-number path) → amount → purpose → Confirm → OTP → success.
     * Migrated from Katalon Scripts/Remittance/WalletTransfer (SelectBenfeficiery).
     */
    @Step("Transfer {amount} directly to the first active beneficiary")
    public WalletTransferPage transferToActiveBeneficiary(WalletTransferPage page, String amount, String otp) {
        page.tapFirstBeneficiary();
        acceptUnsavedNumberPopup();

        page.isAmountScreenVisible(15);
        page.selectQuickAmount(amount);
        page.tapNext();

        page.selectFirstPurpose();
        page.tapNext();

        waits.waitForVisible(AppiumBy.accessibilityId("testID-primary-confirm-main"), 15);
        page.tapConfirm();

        if (waits.isPresent(INSUFFICIENT_BALANCE, 3)) {
            throw new IllegalStateException(
                    "INSUFFICIENT BALANCE — top up the sender wallet before running.");
        }
        otpPage.enterOtp(otp);
        platformActions.dismissKeyboard();
        common.waitForResultAfterOtp(30);
        log.info("Wallet transfer of {} to active beneficiary completed", amount);
        return page;
    }

    // ═══════════════════════════════════════════════════
    //  TRANSACTION HISTORY
    // ═══════════════════════════════════════════════════

    /**
     * Dashboard → Transactions → open the most recent transaction's details.
     * Used to verify a just-completed transfer is recorded in the sender's history.
     */
    @Step("Open the most recent transaction from history")
    public WalletTransactionDetailsPage openLastTransaction() {
        // Deep-link straight to the dashboard. The post-transfer success overlay keeps the soft
        // keyboard up (covering Done) and cannot be left via BACK (that exits the app), so we
        // mirror Katalon's navigateToHomePageDashboardThroughDeepLink instead of UI navigation.
        openDashboardViaDeepLink();
        dashboardPage.dismissPopups();
        dashboardPage.clickTransactions();

        WalletTransactionDetailsPage page = new WalletTransactionDetailsPage();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-main-firstRow-0"), 20);
        page.tapFirstTransaction();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-main-0"), 15);
        log.info("Opened most recent transaction details");
        return page;
    }

    /** Navigate directly to the dashboard via deep link. */
    @Step("Open dashboard via deep link")
    private void openDashboardViaDeepLink() {
        openViaDeepLink("urpay://DashboardHome");
        waits.waitForVisible(AppiumBy.accessibilityId("testID-master-amount-main"), 20);
        log.info("Dashboard opened via deep link");
    }

    /** Fire a urpay:// deep link via the UiAutomator2 mobile:deepLink command. */
    private void openViaDeepLink(String url) {
        String appPackage = ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("package", appPackage);
        ((JavascriptExecutor) driver).executeScript("mobile: deepLink", params);
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
