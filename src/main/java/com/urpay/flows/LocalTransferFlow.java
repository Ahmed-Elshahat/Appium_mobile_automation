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
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.LocalTransactionDetailsPage;
import com.urpay.pages.remittance.LocalTransferPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Local Transfer flow — bank (IBAN) transfer to a local beneficiary.
 *
 * Navigation: deep link urpay://SendMoneyToLocalProcess → Local Transfer (amount screen),
 * with a Dashboard → Transfer → Local Transfer fallback.
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/LocalTran/LocalTransfer/
 *   Scripts/Remittance/LocalTran/LocalTransferWithFees/
 *   Scripts/Remittance/LocalTran/LocalTransferEditTransaction/
 *   Scripts/Remittance/LocalTran/ValidateSenderLocalTransactionHistory/
 *
 * Strategy: transfer to a PRE-EXISTING ACTIVE local beneficiary (the account already has one —
 * "Noura Faisal"). Adding a NEW beneficiary needs backend IVR activation and is out of scope.
 *
 * Rules:
 *   - ZERO Thread.sleep() — all waits via WaitUtils.
 *   - NO assertions (returns page objects for the test to verify).
 *   - NO hardcoded data (reads from ConfigManager).
 */
public class LocalTransferFlow {

    private static final Logger log = LoggerFactory.getLogger(LocalTransferFlow.class);

    // ── Navigation ─────────────────────────────────────
    private static final String LOCAL_TRANSFER_DEEP_LINK = "urpay://SendMoneyToLocalProcess";
    private static final By TRANSFER_NAV =
            AppiumBy.accessibilityId("testID-TRANSFER");
    private static final By LOCAL_TRANSFER_BTN =
            AppiumBy.accessibilityId("testID-viewElemenLocalTransfer");
    private static final By ENTER_AMOUNT_LABEL =
            AppiumBy.xpath("//*[@text='Enter Amount']");
    /** Beneficiary list screen — new app flow shows this BEFORE amount entry */
    private static final By BENEFICIARY_LIST =
            AppiumBy.xpath("//*[@text='Local Transfer' and @class='android.widget.TextView']/ancestor::*/following-sibling::*//android.widget.EditText[contains(@text,'Search')]" +
                    " | //*[@text='Search by name or number']");
    private static final By DASHBOARD_BALANCE =
            AppiumBy.accessibilityId("testID-master-amount-main");

    // ── Optional system / app popups ───────────────────
    /** Android runtime contact-permission dialog "Allow" button. */
    private static final By CONTACT_ALLOW = AppiumBy.xpath(
            "//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button'] | "
            + "//*[@resource-id='com.android.packageinstaller:id/permission_allow_button'] | "
            + "//*[@text='Allow' or @text='ALLOW' or @text='While using the app']");

    /** Post-transfer rating popup close button. */
    private static final By CLOSE_RATING = AppiumBy.xpath(
            "//*[@content-desc='testID-close-icon' or @content-desc='testID-rating-close' "
            + "or @text='Maybe later' or @text='Not now']");

    // ── Result markers ─────────────────────────────────
    private static final By INSUFFICIENT_BALANCE = AppiumBy.xpath(
            "//*[contains(@text,'Insufficient') or contains(@label,'Insufficient')]");

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public LocalTransferFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.otpPage = new OtpPage();
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Local Transfer (deep link urpay://SendMoneyToLocalProcess)")
    public LocalTransferPage navigateToLocalTransfer() {
        // Already on the amount screen (e.g. a prior chained test left us here)? Reuse it.
        if (waits.isVisible(ENTER_AMOUNT_LABEL, 1)) {
            log.info("Already on Local Transfer amount screen — reusing it");
            return new LocalTransferPage();
        }
        // Already on the beneficiary list (new flow)?
        if (waits.isVisible(BENEFICIARY_LIST, 1)) {
            log.info("Already on Local Transfer beneficiary screen — reusing it");
            return new LocalTransferPage();
        }
        // Deep-link straight to Local Transfer.
        openViaDeepLink(LOCAL_TRANSFER_DEEP_LINK);
        if (waits.isVisible(ENTER_AMOUNT_LABEL, 5)) {
            log.info("Local Transfer amount screen loaded (deep link)");
            return new LocalTransferPage();
        }
        if (waits.isVisible(BENEFICIARY_LIST, 5)) {
            log.info("Local Transfer beneficiary screen loaded (deep link, new flow)");
            return new LocalTransferPage();
        }
        // Fallback: Dashboard → Transfer → Local Transfer.
        log.info("Deep link did not land on expected screen — falling back to Dashboard navigation");
        openViaDeepLink("urpay://DashboardHome");
        waits.waitForVisible(DASHBOARD_BALANCE, 20);
        dashboardPage.dismissPopups();
        waits.waitForClickable(TRANSFER_NAV, 15).click();
        waits.waitForClickable(LOCAL_TRANSFER_BTN, 15).click();
        // Accept EITHER screen (amount-first or beneficiary-first)
        By eitherScreen = AppiumBy.xpath(
                "//*[@text='Enter Amount'] | //*[@text='Search by name or number']");
        waits.waitForVisible(eitherScreen, 15);
        log.info("Local Transfer screen loaded (Dashboard navigation)");
        return new LocalTransferPage();
    }

    // ══════════════════════════════════════════════════
    //  STEP 1 — AMOUNT → BENEFICIARY SCREEN
    // ══════════════════════════════════════════════════

    /**
     * Navigate to Local Transfer and handle amount entry.
     * Supports both flows:
     *   OLD: Enter Amount → Next → Beneficiary list
     *   NEW: Beneficiary list first → select → Enter Amount
     */
    @Step("Open Local Transfer and enter amount {amount}")
    public LocalTransferPage openLocalTransferAmount(String amount) {
        LocalTransferPage page = navigateToLocalTransfer();
        if (waits.isVisible(ENTER_AMOUNT_LABEL, 3)) {
            // OLD flow: amount screen shown first → enter and proceed
            page.enterAmount(amount);
            page.tapNextAtAmount();
            grantContactPermission();
            log.info("Entered local transfer amount {} — on beneficiary screen (amount-first flow)", amount);
        } else {
            // NEW flow: beneficiary list shown first → store amount for after beneficiary selection
            grantContactPermission();
            page.setPendingAmount(amount);
            log.info("On beneficiary screen (beneficiary-first flow) — amount {} will be entered after selection", amount);
        }
        return page;
    }

    // ══════════════════════════════════════════════════
    //  STEP 2 — SELECT BENEFICIARY
    // ══════════════════════════════════════════════════

    /**
     * Select the local beneficiary by name (Katalon selects the saved beneficiary by name), with
     * the first saved beneficiary as a fallback. Returns false if the account has no saved local
     * beneficiary, so the test can SKIP cleanly.
     *
     * Handles both flows:
     *   OLD: Already on beneficiary screen after amount → just select
     *   NEW: On beneficiary screen first → select → enter pending amount on next screen
     */
    @Step("Select local beneficiary: {name}")
    public boolean selectBeneficiary(LocalTransferPage page, String name) {
        if (!page.isBeneficiaryVisible(name, 15)) {
            log.warn("No saved local beneficiary '{}' in the list", name);
            return false;
        }
        page.selectBeneficiaryByName(name);
        log.info("Selected local beneficiary '{}'", name);

        // NEW flow: after selecting beneficiary, the amount screen appears → enter pending amount
        String pendingAmount = page.getPendingAmount();
        if (pendingAmount != null) {
            waits.waitForVisible(ENTER_AMOUNT_LABEL, 15);
            page.enterAmount(pendingAmount);
            page.tapNextAtAmount();
            page.clearPendingAmount();
            log.info("Entered pending amount {} after beneficiary selection (beneficiary-first flow)", pendingAmount);
        }
        return true;
    }

    // ══════════════════════════════════════════════════
    //  STEP 3 — COMPLETE TRANSFER (purpose → confirm → OTP)
    // ══════════════════════════════════════════════════

    /**
     * Basic local transfer: select the purpose → Next → Confirm → OTP → success.
     * Migrated from Scripts/Remittance/LocalTran/LocalTransfer.
     *
     * @return true if the success (Thank You) screen is shown after the transfer.
     */
    @Step("Complete the local transfer (purpose → confirm → OTP)")
    public boolean completeTransfer(LocalTransferPage page, String otp) {
        page.selectFirstPurpose();
        page.tapNextAtPurpose();
        boolean ok = confirmAndVerify(page, otp);
        log.info("Local transfer completed (success={})", ok);
        return ok;
    }

    /**
     * Local transfer with fees included: select purpose → Next → toggle 'Include Fees' on the
     * review screen → Confirm → OTP.
     * Migrated from Scripts/Remittance/LocalTran/LocalTransferWithFees.
     */
    @Step("Complete the local transfer WITH fees (include fees → confirm → OTP)")
    public boolean completeTransferWithFees(LocalTransferPage page, String otp) {
        page.selectFirstPurpose();
        page.tapNextAtPurpose();
        page.toggleIncludeFees();
        boolean ok = confirmAndVerify(page, otp);
        log.info("Local transfer WITH fees completed (success={})", ok);
        return ok;
    }

    /**
     * Local transfer with a non-default purpose + note: on the purpose screen select another
     * purpose (Travel) and add a note → Next → Confirm → OTP. The current build edits the
     * purpose directly on the purpose screen (no separate Edit-pot step).
     * Migrated from Scripts/Remittance/LocalTran/LocalTransferEditTransaction.
     */
    @Step("Complete the local transfer with a non-default purpose & note '{note}'")
    public boolean completeTransferWithEditedPurpose(LocalTransferPage page, String note, String otp) {
        page.selectAnotherPurpose();
        page.enterNote(note);
        platformActions.dismissKeyboard();
        page.tapNextAtPurpose();
        boolean ok = confirmAndVerify(page, otp);
        log.info("Local transfer with edited purpose & note completed (success={})", ok);
        return ok;
    }

    /**
     * Shared tail: reveal & tap Confirm, guard insufficient funds, enter OTP, then capture the
     * success state IMMEDIATELY (the success screen can auto-dismiss / the app can re-lock).
     */
    private boolean confirmAndVerify(LocalTransferPage page, String otp) {
        page.scrollToConfirmAndTap();

        if (waits.isPresent(INSUFFICIENT_BALANCE, 1)) {
            throw new IllegalStateException(
                    "INSUFFICIENT BALANCE — top up the sender account before running local transfer.");
        }
        otpPage.enterOtp(otp);
        platformActions.dismissKeyboard();
        // Close any rating popup that is already up, then wait DIRECTLY for the success screen
        // (Done / Thank You) — it returns as soon as success appears, so there is no fixed 30s
        // result wait that previously timed out on builds whose result locator differed.
        quickTap(CLOSE_RATING);
        boolean success = page.isTransferSuccessful(25);
        quickTap(CLOSE_RATING);
        return success;
    }

    // ══════════════════════════════════════════════════
    //  ADD NEW LOCAL BENEFICIARY (+ IVR-skip activation)
    // ══════════════════════════════════════════════════

    /**
     * Best-effort cleanup: delete any existing local beneficiary with the given nickname so a
     * fresh one can be added (mirrors Katalon AddLocalBeneficiary deleting the prior "zaza").
     * Wrapped so a missing beneficiary / a differing build never fails the test.
     */
    @Step("Delete any existing local beneficiary nicknamed '{nickname}' (best-effort)")
    public void deleteExistingLocalBeneficiary(String nickname) {
        try {
            openViaDeepLink("urpay://DashboardHome");
            waits.waitForVisible(DASHBOARD_BALANCE, 20);
            dashboardPage.dismissPopups();
            waits.waitForClickable(TRANSFER_NAV, 10).click();

            LocalTransferPage page = new LocalTransferPage();
            page.tapBeneficiaryManagement();
            grantContactPermission();
            page.tapLocalBeneficiaryCategory();
            page.searchBeneficiary(nickname);
            platformActions.dismissKeyboard();

            if (page.isFirstContactPresent(5)) {
                page.tapFirstContact();
                page.tapDeleteContact();
                page.tapConfirmDelete();
                log.info("Deleted existing local beneficiary '{}'", nickname);
            } else {
                log.info("No existing local beneficiary '{}' found to delete", nickname);
            }
        } catch (Exception e) {
            log.warn("Beneficiary cleanup for '{}' skipped: {}", nickname, e.getMessage());
        }
    }

    /**
     * Add a new local beneficiary end-to-end (UI): amount → Next → Add new beneficiary →
     * IBAN / full name / nickname → Next → Confirm → OTP. Stops on the IVR-pending screen —
     * the beneficiary is created with STATUS=PENDING and must then be activated in the DB
     * ({@code BeneficiaryActivationHelper.activate}) to bypass the IVR phone verification.
     * Migrated from Scripts/Remittance/LocalTran/AddLocalBeneficiary.
     */
    @Step("Add a new local beneficiary: {fullName} (IBAN {iban})")
    public LocalTransferPage addLocalBeneficiary(String amount, String iban, String fullName,
            String nickname, String otp) {
        LocalTransferPage page = navigateToLocalTransfer();
        page.enterAmount(amount);
        page.tapNextAtAmount();
        grantContactPermission();

        page.tapAddNewBeneficiary();
        page.enterIban(iban);
        page.enterFullName(fullName);
        page.enterNickname(nickname);
        page.scrollToBeneficiaryNextAndTap();

        page.scrollToConfirmAndTap();
        if (waits.isPresent(INSUFFICIENT_BALANCE, 1)) {
            throw new IllegalStateException(
                    "INSUFFICIENT BALANCE — top up the sender account before adding a beneficiary.");
        }
        otpPage.enterOtp(otp);
        platformActions.dismissKeyboard();
        log.info("Submitted new local beneficiary '{}' — pending IVR; activate via DB", fullName);
        return page;
    }

    /**
     * Transfer to the freshly added (and DB-activated) local beneficiary, selecting it by name.
     * Migrated from Scripts/Remittance/LocalTran/LocalTransferNewTransfer.
     *
     * @return true if the success screen is shown; false if the beneficiary is not selectable
     *         (e.g. activation did not run / the beneficiary is still PENDING).
     */
    @Step("Transfer {amount} to the newly added local beneficiary {fullName}")
    public boolean transferToNewBeneficiary(String amount, String fullName, String otp) {
        LocalTransferPage page = openLocalTransferAmount(amount);
        if (!selectBeneficiary(page, fullName)) {
            log.warn("New beneficiary '{}' is not selectable — not active yet?", fullName);
            return false;
        }
        return completeTransfer(page, otp);
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTION HISTORY
    // ══════════════════════════════════════════════════

    /**
     * Dashboard → Transactions → open the most recent transaction's details.
     * Migrated from ValidateSenderLocalTransactionHistory (navigateToHomePageDashboardThroughDeepLink
     * → TransactionButton → first transaction).
     */
    @Step("Open the most recent transaction from history")
    public LocalTransactionDetailsPage openLastTransaction() {
        // The post-transfer success overlay keeps the soft keyboard up (covering Done) and cannot
        // be left via BACK (that exits the app), so deep-link to the dashboard like Katalon does.
        openViaDeepLink("urpay://DashboardHome");
        waits.waitForVisible(DASHBOARD_BALANCE, 20);
        dashboardPage.dismissPopups();
        dashboardPage.clickTransactions();

        LocalTransactionDetailsPage page = new LocalTransactionDetailsPage();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-main-firstRow-0"), 20);
        page.tapFirstTransaction();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-main-0"), 15);
        log.info("Opened most recent transaction details");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    /** Fire a urpay:// deep link via the UiAutomator2 mobile:deepLink command. */
    private void openViaDeepLink(String url) {
        String appPackage = ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("package", appPackage);
        ((JavascriptExecutor) driver).executeScript("mobile: deepLink", params);
    }

    @Step("Grant contact permission if prompted")
    private void grantContactPermission() {
        if (quickTap(CONTACT_ALLOW)) {
            log.info("Granted contact permission");
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
