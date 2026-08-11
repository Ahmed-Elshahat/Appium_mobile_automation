package com.urpay.flows;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.model.InternationalTransferData;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.AddInternationalBeneficiaryPage;
import com.urpay.pages.remittance.InternationalTransferPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * International (MTO) Transfer flow — the shared, operator-agnostic workflow for MoneyGram,
 * Western Union, Tahweel AlRajhi, Transfast and H2H. The flow is parameterised by
 * {@link InternationalTransferData}, so a MoneyGram / WU / Tahweel test only supplies its data.
 *
 * Navigation: deep link {@code urpay://international} (Katalon SmartNavigator
 * navigateToInternationalThroughDeepLink), with a Dashboard → Transfer → International fallback.
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/Perform International Transfer/
 *   (Moneygram Cash Pickup / Bank Deposit / Send To Wallet perform cases + their sub-cases)
 *   Scripts/Remittance/InternationalTran/AddMgBeneficiary/
 *
 * Rules:
 *   - ZERO Thread.sleep() — all waits via WaitUtils.
 *   - NO assertions (returns page objects / booleans for the test to verify).
 *   - NO hardcoded data (reads from InternationalTransferData / ConfigManager).
 */
public class InternationalTransferFlow {

    private static final Logger log = LoggerFactory.getLogger(InternationalTransferFlow.class);

    /**
     * Outcome of {@link #performTransfer}. Distinguishes a missing beneficiary (the test should
     * SKIP) from a genuine failure (the test should FAIL) so "false" is never ambiguous.
     */
    public enum TransferResult { SUCCESS, NO_BENEFICIARY, PROVIDER_UNAVAILABLE, FAILED }

    // ── Navigation ─────────────────────────────────────
    private static final String INTERNATIONAL_DEEP_LINK = "urpay://international";
    private static final String DASHBOARD_DEEP_LINK = "urpay://DashboardHome";
    private static final By TRANSFER_NAV = AppiumBy.accessibilityId("testID-TRANSFER");
    private static final By INTERNATIONAL_TRANSFER_BTN =
            AppiumBy.xpath("//*[@content-desc='testID-viewElemeninternational1']");
    private static final By SEARCH_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-Search-Input-Container']//android.widget.EditText"
            + " | //*[@content-desc='testID-Search-Input']");
    private static final By DASHBOARD_BALANCE =
            AppiumBy.accessibilityId("testID-master-amount-main");

    // ── Result markers ─────────────────────────────────
    private static final By INSUFFICIENT_BALANCE = AppiumBy.xpath(
            "//*[contains(@text,'Insufficient') or contains(@label,'Insufficient')]");

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;

    public InternationalTransferFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.otpPage = new OtpPage();
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to International Transfer (deep link urpay://international)")
    public InternationalTransferPage navigateToInternationalTransfer() {
        // A prior chained test may already be on the international landing (search visible) → reuse.
        if (waits.isVisible(SEARCH_INPUT, 1)) {
            log.info("Already on International Transfer screen — reusing it");
            return new InternationalTransferPage();
        }
        openViaDeepLink(INTERNATIONAL_DEEP_LINK);
        if (waits.isVisible(SEARCH_INPUT, 10)) {
            log.info("International Transfer screen loaded (deep link)");
            return new InternationalTransferPage();
        }
        // Fallback: Dashboard → Transfer → International Transfer.
        log.info("Deep link did not land on the international screen — falling back to Dashboard nav");
        openViaDeepLink(DASHBOARD_DEEP_LINK);
        waits.waitForVisible(DASHBOARD_BALANCE, 20);
        dashboardPage.dismissPopups();
        waits.waitForClickable(TRANSFER_NAV, 15).click();
        waits.waitForClickable(INTERNATIONAL_TRANSFER_BTN, 15).click();
        waits.waitForVisible(SEARCH_INPUT, 15);
        log.info("International Transfer screen loaded (Dashboard navigation)");
        return new InternationalTransferPage();
    }

    // ══════════════════════════════════════════════════
    //  SELECT BENEFICIARY
    // ══════════════════════════════════════════════════

    /**
     * Search for and select the beneficiary. Returns false if the account has no matching
     * international beneficiary (so the test can SKIP cleanly rather than fail).
     */
    @Step("Search and select international beneficiary: {name}")
    public boolean selectBeneficiary(InternationalTransferPage page, String name) {
        page.searchBeneficiary(name);
        if (!page.isFirstBeneficiaryVisible(12)) {
            // Diagnostic ONLY on failure (scanning every TextView is slow — keep it off the happy
            // path). Clear the filter first so the account's actual beneficiaries are shown.
            page.clearSearch();
            log.warn("No international beneficiary '{}' found. Beneficiaries on the account: {}",
                    name, page.getVisibleBeneficiaryNames());
            return false;
        }
        page.selectBeneficiaryByName(name);

        // A beneficiary with an incomplete name raises "Name Verification" (middle name required)
        // and gates the transfer. Supply the middle name, Save, then re-initiate the transfer.
        if (page.isNameVerificationShown(4)) {
            String middle = ConfigManager.getInstance().get("moneygram.beneficiaryMiddleName", "Ahmed");
            log.info("Beneficiary '{}' needs a middle name — completing Name Verification with '{}'",
                    name, middle);
            page.completeNameVerification(middle);
            if (!page.isOnAmountScreen(3)) {
                // Save returned to the beneficiary list — re-search and re-tap Transfer.
                page.searchBeneficiary(name);
                page.selectBeneficiaryByName(name);
                if (page.isNameVerificationShown(3)) {
                    log.warn("Name Verification still shown for '{}' after the middle-name update", name);
                }
            }
        }
        log.info("Selected international beneficiary '{}'", name);
        return true;
    }

    // ══════════════════════════════════════════════════
    //  PERFORM TRANSFER (full E2E)
    // ══════════════════════════════════════════════════

    /**
     * Perform a full international transfer to an existing beneficiary:
     * navigate → search + select beneficiary → amount → service provider → purpose →
     * confirm → OTP → success (Thank You). Real money — keep the amount small.
     *
     * @return {@link TransferResult#NO_BENEFICIARY} if the account has no such beneficiary
     *         (test should SKIP), {@link TransferResult#SUCCESS} if the Thank You screen shows,
     *         otherwise {@link TransferResult#FAILED}.
     */
    @Step("Perform international transfer: {data}")
    public TransferResult performTransfer(InternationalTransferData data, String otp) {
        InternationalTransferPage page;
        try {
            page = openConfirmation(data);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found among the available provider")) {
                log.warn("Provider unavailable for this corridor: {}", e.getMessage());
                return TransferResult.PROVIDER_UNAVAILABLE;
            }
            throw e;
        }
        if (page == null) {
            return TransferResult.NO_BENEFICIARY;
        }
        return confirmAndFinish(page, otp);
    }

    /**
     * Confirm the transfer FROM the confirmation screen → OTP → success (Thank You). Separated
     * from {@link #openConfirmation} so a test can validate the confirmation details first and
     * then complete the SAME transfer, WITHOUT re-running the whole beneficiary→confirm flow.
     * Real money — keep the amount small.
     *
     * @param page an {@link InternationalTransferPage} already positioned on the confirmation
     *             screen (typically returned by {@link #openConfirmation}).
     * @return {@link TransferResult#SUCCESS} if the Thank You screen shows, else
     *         {@link TransferResult#FAILED}.
     */
    @Step("Confirm the international transfer → OTP → success")
    public TransferResult confirmAndFinish(InternationalTransferPage page, String otp) {
        page.scrollToConfirmAndTap();
        if (waits.isPresent(INSUFFICIENT_BALANCE, 1)) {
            throw new IllegalStateException(
                    "INSUFFICIENT BALANCE — top up the sender account before the international transfer.");
        }

        otpPage.enterOtp(otp);
        platformActions.dismissKeyboard();

        page.closeRatingPopupIfPresent();

        // After the OTP is submitted the backend shows EITHER the Thank You screen or the blocking
        // "Service is currently unavailable. Please try again later." banner (a SIT provider outage
        // on this route, commonly surfaced right after OTP). Detect the banner and raise a
        // categorized backend defect so the result is reported as [BACKEND DEFECT] (and never
        // retried) instead of a misleading "Thank You screen should be visible" assertion that
        // hides the real cause.
        if (page.isServiceUnavailable(6)) {
            throw new com.urpay.utils.BackendErrorException(backendOutageAfterOtpMessage());
        }

        boolean success = page.isTransferSuccessful(30);
        page.closeRatingPopupIfPresent();
        if (!success && page.isServiceUnavailable(2)) {
            // The Thank You screen never appeared and the outage banner surfaced a beat later while
            // we were waiting — attribute it to the backend, not a missing success screen.
            throw new com.urpay.utils.BackendErrorException(backendOutageAfterOtpMessage());
        }
        log.info("International transfer completed (success={})", success);
        return success ? TransferResult.SUCCESS : TransferResult.FAILED;
    }

    /**
     * Drive the transfer up to (but NOT past) the confirmation screen, so a test can assert the
     * confirmation details (delivery option, purpose, amounts) WITHOUT spending real money.
     *
     * @return the page positioned on the confirmation screen, or null if the beneficiary is
     *         not selectable.
     */
    @Step("Open the international transfer confirmation screen: {data}")
    public InternationalTransferPage openConfirmation(InternationalTransferData data) {
        InternationalTransferPage page = navigateToInternationalTransfer();
        if (!selectBeneficiary(page, data.getBeneficiaryName())) {
            return null;
        }
        page.enterAmount(data.getAmountSar());
        page.tapNextAtAmount();
        page.selectServiceProvider(data.getServiceProvider(), data.getServiceProviderMarker(),
                data.getServiceProviderIndex());
        // Tahweel AlRajhi (H2H) Cash Pickup requires selecting a payout bank on the provider screen
        // before Next; other MTOs (MoneyGram / Western Union) have no bank dropdown.
        if (requiresBankSelection(data)) {
            page.selectFirstBank();
        }
        page.tapNextAtServiceProvider();
        checkServiceAvailable(page, data);
        page.selectFirstPurpose();
        page.tapNextAtPurpose();
        checkServiceAvailable(page, data);
        page.isConfirmationVisible(15);
        page.dumpConfirmationScreen();
        log.info("On the international transfer confirmation screen for {}", data);
        return page;
    }

    // ══════════════════════════════════════════════════
    //  ADD INTERNATIONAL BENEFICIARY (+ IVR-skip activation)
    // ══════════════════════════════════════════════════

    /**
     * Add a new international beneficiary end-to-end (UI): country → delivery option → currency
     * → name/nickname → (citizenship) → confirm → name verification → relationship → confirm →
     * OTP. Stops on the IVR "Verification Call" screen — the beneficiary is created PENDING and
     * must be activated in the wallet DB before it can receive a transfer.
     * Migrated from Scripts/Remittance/InternationalTran/AddMgBeneficiary.
     *
     * @return true if the IVR verification-call screen is reached (beneficiary submitted).
     */
    @Step("Add a new international beneficiary: {data}")
    public boolean addBeneficiary(InternationalTransferData data, String otp) {
        navigateToInternationalTransfer();
        AddInternationalBeneficiaryPage page = new AddInternationalBeneficiaryPage();

        page.tapAddNewBeneficiary();
        page.selectCountry(data.getReceiverCountry());
        page.tapNextStep();
        page.selectDeliveryOption(data.getDeliveryOption());
        page.tapNextStep();
        // Currency is an OPTIONAL step: Bank Deposit goes straight to the details screen, while
        // Cash Pickup / Send-to-Wallet show a currency radio first.
        if (!page.isOnDetailsScreen(3)) {
            page.selectFirstCurrency();
            page.tapNextStep();
        }

        // Beneficiary Details screen (type toggle + name + corridor-specific fields live here).
        page.selectBeneficiaryType(data.getBeneficiaryType());
        page.enterFullName(data.getBeneficiaryName());
        page.enterNickname(data.getBeneficiaryNickname());
        page.selectFirstCitizenshipIfPresent();
        // Bank Deposit adds bank/branch/account/city fields here (other options skip these).
        page.fillDeliveryDetails(data.getBankName(), data.getBranch(), data.getAccountNumber(),
                data.getRoutingNumber(), data.getCity(), data.getPurposeOfFunds());
        page.tapNextAtDetails();

        page.confirmNameVerification();
        // Relationship is a separate radio step for "Others" on some corridors, but auto-set
        // ("Self") for "Myself" — which goes straight to the Confirmation screen. Only select it
        // when we're NOT already on Confirmation.
        if (!page.isOnConfirmationScreen(3)) {
            page.selectFirstRelationship();
            page.tapNextStep();
        }

        page.scrollToConfirmAndTap();
        otpPage.enterOtp(otp);
        platformActions.dismissKeyboard();

        boolean submitted = page.isVerificationCallShown(20);
        log.info("New international beneficiary '{}' submitted — IVR pending (reached call screen={})",
                data.getBeneficiaryName(), submitted);
        return submitted;
    }

    /**
     * Edit the first saved beneficiary's name(s) and Save. Migrated from Katalon
     * ToValidateEditAddedInternationalBeneficiary.
     *
     * @return true if the edit screen was reached (Save tapped).
     */
    @Step("Edit the first saved international beneficiary: {data}")
    public boolean editBeneficiary(InternationalTransferData data) {
        navigateToInternationalTransfer();
        AddInternationalBeneficiaryPage page = new AddInternationalBeneficiaryPage();
        page.openFirstBeneficiaryForEdit();
        page.editNames(data.getFirstName(), data.getMiddleName(), data.getLastName(),
                data.getBeneficiaryNickname());
        page.tapSave();
        log.info("Edited the first international beneficiary");
        return true;
    }

    /**
     * Remove the first saved beneficiary (open → Delete → confirm). Migrated from Katalon
     * ToValidateRemoveAddedInternationalBeneficiary.
     *
     * @return true after the delete is confirmed.
     */
    @Step("Remove the first saved international beneficiary")
    public boolean removeBeneficiary() {
        navigateToInternationalTransfer();
        AddInternationalBeneficiaryPage page = new AddInternationalBeneficiaryPage();
        page.openFirstBeneficiaryForEdit();
        page.tapDelete();
        page.confirmDelete();
        log.info("Removed the first international beneficiary");
        return true;
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTION HISTORY
    // ══════════════════════════════════════════════════

    /**
     * Deep-link back to the dashboard, open Transactions and the most recent transaction.
     * The post-success overlay keeps the keyboard up (covering Done) and BACK exits the app,
     * so we deep-link like Katalon (SmartNavigator).
     */
    @Step("Open the most recent transaction from history")
    public com.urpay.pages.remittance.LocalTransactionDetailsPage openLastTransaction() {
        returnToDashboard();
        dashboardPage.clickTransactions();

        com.urpay.pages.remittance.LocalTransactionDetailsPage page =
                new com.urpay.pages.remittance.LocalTransactionDetailsPage();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-main-firstRow-0"), 20);
        page.tapFirstTransaction();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-main-0"), 15);
        log.info("Opened most recent transaction details");
        return page;
    }

    @Step("Return to the dashboard")
    public DashboardPage returnToDashboard() {
        openViaDeepLink(DASHBOARD_DEEP_LINK);
        waits.waitForVisible(DASHBOARD_BALANCE, 20);
        dashboardPage.dismissPopups();
        return dashboardPage;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    /**
     * Fail fast if the backend raised a "Service is currently unavailable. Please try again later."
     * banner — a SIT backend/provider outage for this route that disables Next, NOT a test defect.
     * The failure listener attaches a screenshot; the wording matches the Allure backend category.
     */
    private void checkServiceAvailable(InternationalTransferPage page, InternationalTransferData data) {
        if (page.isServiceUnavailable(3)) {
            throw new IllegalStateException(
                    "Service is currently unavailable — the app shows 'Service is currently "
                    + "unavailable. Please try again later.' for the " + data.getServiceProvider()
                    + " " + data.getDeliveryOption() + " route to " + data.getReceiverCountry()
                    + ". SIT backend/provider outage (not a test defect).");
        }
    }

    /**
     * Message for the post-OTP SIT outage. Carries the "currently unavailable" / "try again later"
     * phrase so the Allure categoriser buckets it under "Backend service unavailable (SIT)" and the
     * {@code RetryAnalyzer} treats it as a deterministic outage (no retry).
     */
    private static String backendOutageAfterOtpMessage() {
        return "Service is currently unavailable. Please try again later. — SIT backend/provider "
                + "outage on the international transfer after OTP submit (not a test defect).";
    }

    /** Tahweel AlRajhi (H2H) is the only MTO that needs a payout-bank selection step. */
    private boolean requiresBankSelection(InternationalTransferData data) {
        String p = data.getServiceProvider() == null ? "" : data.getServiceProvider().toLowerCase();
        return p.contains("tahweel") || p.contains("rajhi") || p.contains("h2h");
    }

    /** Fire a urpay:// deep link via the UiAutomator2 mobile:deepLink command. */
    private void openViaDeepLink(String url) {
        String appPackage = ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("package", appPackage);
        ((JavascriptExecutor) driver).executeScript("mobile: deepLink", params);
    }
}
