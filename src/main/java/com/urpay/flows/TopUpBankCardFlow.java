package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.PasscodePage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.TopUpPage;
import com.urpay.pages.payments.TopUpSettingsPage;
import com.urpay.pages.payments.TransactionDetailsPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.SwipeUtils;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Top Up Bank Card flow — orchestrates all top-up via bank card workflows.
 *
 * ZERO Thread.sleep() — all waits via WaitUtils explicit waits.
 * NO assertions — returns page objects/values for tests to verify.
 * NO hardcoded values — all data from ConfigManager.
 *
 * Migrated from Katalon:
 *   Test Suites/PaymentAndCards/TopUpUsingBankCard.ts
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/*
 *
 * Supported Workflows:
 *   - Top up with new bank card (no existing cards)
 *   - Top up with existing saved card
 *   - Wallet balance retrieval (before/after top-up)
 *   - Transaction details verification
 *   - Auto top-up setup (first time)
 *   - Auto top-up limit configuration
 *   - Auto top-up enable/disable toggle
 *   - Delete auto top-up card
 *   - Delete saved top-up card (from profile)
 */
public class TopUpBankCardFlow {

    private static final Logger log = LoggerFactory.getLogger(TopUpBankCardFlow.class);

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final SwipeUtils swipe;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final PasscodePage passcodePage;
    private final DashboardPage dashboardPage;

    // ── Locators for camera permission popup ──────────
    private static final By ALLOW_PERMISSION = AppiumBy.xpath(
            "//*[@text='Allow' or @text='ALLOW' or @text='While using the app']");

    // ── Locator for top-up screen loaded ──────────────
    private static final By TOPUP_SCREEN = AppiumBy.xpath(
            "//*[@content-desc='testID-data-0']"
            + " | //*[@text='Bank Card' or @text='bank card']"
            + " | //*[@content-desc='testID-primary-onSubmit-main']");

    public TopUpBankCardFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.swipe = new SwipeUtils(driver);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.common = new CommonComponentsPage();
        this.passcodePage = new PasscodePage();
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Top Up screen from dashboard")
    public TopUpPage navigateToTopUp() {
        goToDashboard();
        dashboardPage.dismissPopups();
        dashboardPage.clickTopUp();
        waits.waitForVisible(TOPUP_SCREEN, 15);
        log.info("Top Up screen loaded");
        return new TopUpPage();
    }

    @Step("Navigate back to dashboard")
    public void goToDashboard() {
        for (int i = 0; i < 3; i++) {
            if (dashboardPage.isSearchIconVisible(2)) {
                break;
            }
            driver.navigate().back();
        }
    }

    // ══════════════════════════════════════════════════
    //  TOP UP WITH NEW CARD (user has no saved cards)
    // ══════════════════════════════════════════════════

    @Step("Top up with new bank card — full wizard")
    public TopUpPage topUpWithNewCard() {
        ConfigManager c = ConfigManager.getInstance();
        return topUpWithNewCard(
                c.get("topup.cardHolderName"),
                c.get("topup.cardNumber"),
                c.get("topup.cardExpMonth"),
                c.get("topup.cardExpYear"),
                c.get("topup.cardNickName"),
                c.get("topup.newCardAmount"),
                c.get("topup.paymentPassCode"),
                c.get("topup.cvv", "123"),
                c.get("topup.3dsOtp"));
    }

    @Step("Top up with new bank card: {cardHolderName}, amount={amount}")
    public TopUpPage topUpWithNewCard(String cardHolderName, String cardNumber,
            String expiryMonth, String expiryYear, String nickname,
            String amount, String passCode, String cvv, String otp) {

        TopUpPage page = navigateToTopUp();

        // Step 1: Select Bank Card method
        page.tapBankCard();
        log.info("Selected Bank Card as top-up method");

        // Step 2: Check for existing cards — determines whether to save the new card
        // If user already has a card saved (from previous run), we skip "Save this card"
        // to avoid duplicate card rejection. The card is still used for this top-up.
        boolean hasExistingCard = page.isExistingCardVisible(3);
        boolean saveCard = !hasExistingCard;
        if (hasExistingCard) {
            log.info("Existing card detected — will add new card WITHOUT saving to avoid duplicate");
        }

        // Step 3: Add new card — handle stuck state from previous failed transactions
        page.tapAddNewCard();
        if (isStuckOnErrorPage()) {
            log.warn("Detected stuck error page — recovering by navigating back");
            driver.navigate().back();
            waits.waitForVisible(TOPUP_SCREEN, 10);
            page.tapBankCard();
            page.tapAddNewCard();
        }

        // Step 4: Handle camera permission if it appears
        dismissCameraPermission();

        // Step 5: Enter details manually (skip camera scanner)
        page.tapEnterDetailsManually();

        // Step 6: Fill card details
        page.enterCardHolderName(cardHolderName);
        platformActions.dismissKeyboard();

        page.enterCardNumber(cardNumber);
        platformActions.dismissKeyboard();

        page.enterExpiry(expiryMonth, expiryYear);
        platformActions.dismissKeyboard();

        // Step 7: Optionally save card with nickname (only if no existing cards)
        swipe.swipeUp();
        if (saveCard) {
            page.tapSaveCardCheckBox();
            page.enterNickName(nickname);
            platformActions.dismissKeyboard();
            log.info("Save card enabled with nickname: {}", nickname);
        } else {
            log.info("Skipping save card — user already has saved cards");
        }

        // Step 8: Scroll to ensure Next button is fully in viewport, then tap
        swipe.swipeUp();
        page.tapPrimaryButton();
        log.info("Card details submitted");

        // Step 9: Wait for amount page to load
        waits.waitForVisible(
                AppiumBy.accessibilityId("testID-primary-validateAmount-main"), 20);
        page.enterAmount(amount);
        platformActions.dismissKeyboard();

        // Step 10: Tap Next to validate amount
        page.tapValidateAmount();
        log.info("Amount {} entered and validated", amount);

        // Step 11: Enter payment passcode
        waits.waitForInvisible(
                AppiumBy.accessibilityId("testID-primary-validateAmount-main"), 10);
        passcodePage.enterPasscode(passCode);
        log.info("Payment passcode entered");

        // Step 12: Enter CVV code (3-digit custom keypad)
        enterCvv(cvv);

        // Step 13: Wait for and enter 3D Secure OTP
        complete3dSecure(page, otp);

        // Step 14: Scroll to and tap Done
        swipe.swipeUp();
        page.tapDone(15);
        log.info("Top-up with new card completed successfully");

        return page;
    }

    // ══════════════════════════════════════════════════
    //  TOP UP WITH EXISTING CARD
    // ══════════════════════════════════════════════════

    @Step("Top up with existing saved card")
    public TopUpPage topUpWithExistingCard() {
        ConfigManager c = ConfigManager.getInstance();
        return topUpWithExistingCard(
                c.get("topup.existingCardAmount"),
                c.get("topup.existingCardPaymentPassCode"),
                c.get("topup.cvv", "123"),
                c.get("topup.3dsOtp"));
    }

    @Step("Top up with existing card: amount={amount}")
    public TopUpPage topUpWithExistingCard(String amount, String passCode, String cvv, String otp) {
        TopUpPage page = new TopUpPage();

        // Navigate to top-up screen if not already there
        if (!page.isBankCardButtonVisible(2) && !page.isExistingCardVisible(2)) {
            page = navigateToTopUp();
            page.tapBankCard();
            log.info("Selected Bank Card as top-up method");
        }

        // Tap Next for existing card
        page.tapNextExistingCard();
        log.info("Using existing saved card");

        // Enter amount — wait for the validate amount button to confirm we're on amount page
        waits.waitForVisible(
                AppiumBy.accessibilityId("testID-primary-validateAmount-main"), 15);
        page.enterAmount(amount);
        platformActions.dismissKeyboard();

        // Tap Next to validate amount
        page.tapValidateAmount();
        log.info("Amount {} entered and validated", amount);

        // Enter payment passcode
        waits.waitForInvisible(
                AppiumBy.accessibilityId("testID-primary-validateAmount-main"), 10);
        passcodePage.enterPasscode(passCode);
        log.info("Payment passcode entered");

        // Enter CVV code
        enterCvv(cvv);

        // Step 7: Wait for and complete 3D Secure
        complete3dSecure(page, otp);

        // Step 7: Tap Done
        page.tapDone(15);
        log.info("Top-up with existing card completed successfully");

        return page;
    }

    // ══════════════════════════════════════════════════
    //  WALLET BALANCE
    // ══════════════════════════════════════════════════

    @Step("Get current wallet balance from dashboard")
    public String getWalletBalance() {
        waits.waitForVisible(AppiumBy.accessibilityId("testID-master-amount-main"), 10);
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

    @Step("Parse wallet balance as double")
    public double getWalletBalanceAsDouble() {
        String balance = getWalletBalance();
        // Remove currency symbol, commas, and whitespace
        String cleaned = balance.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Could not parse balance '{}' as double, returning 0.0", balance);
            return 0.0;
        }
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTION DETAILS VERIFICATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to transactions and open latest top-up transaction")
    public TransactionDetailsPage viewLatestTransactionDetails() {
        TopUpPage page = new TopUpPage();

        // Tap Transactions tab
        page.tapTransactionsTab();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-main-firstRow-0"), 15);

        // Verify latest transaction title contains "Add money"
        String title = page.getLatestTransactionTitle();
        log.info("Latest transaction title: {}", title);

        // Tap to view details
        page.tapLatestTransaction();

        TransactionDetailsPage detailsPage = new TransactionDetailsPage();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-main-0"), 15);
        log.info("Transaction details page loaded");
        return detailsPage;
    }

    @Step("Get latest transaction title text")
    public String getLatestTransactionTitle() {
        TopUpPage page = new TopUpPage();
        page.tapTransactionsTab();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-main-firstRow-0"), 15);
        return page.getLatestTransactionTitle();
    }

    // ══════════════════════════════════════════════════
    //  AUTO TOP-UP SETUP
    // ══════════════════════════════════════════════════

    @Step("Set up auto top-up for the first time")
    public TopUpSettingsPage setupAutoTopUpForFirstTime() {
        TopUpPage topUpPage = navigateToTopUp();

        // Swipe down to reveal Settings button
        swipe.swipeUp();
        swipe.swipeUp();

        TopUpSettingsPage settingsPage = new TopUpSettingsPage();
        settingsPage.tapTopUpSettings();
        settingsPage.tapAutoTopUpArrow();
        settingsPage.tapSetAutoTopUp();
        settingsPage.tapNextAutoTopUpCreation();

        log.info("Auto top-up initial setup completed — ready for limit configuration");
        return settingsPage;
    }

    @Step("Set auto top-up amount limit: {amount}")
    public TopUpSettingsPage setAutoTopUpAmountLimit(String amount) {
        TopUpSettingsPage page = new TopUpSettingsPage();
        page.tapSetTopUpLimit();
        page.enterLimitAmount(amount);
        platformActions.dismissKeyboard();
        page.tapSave();
        log.info("Auto top-up amount limit set to: {}", amount);
        return page;
    }

    @Step("Set auto top-up lower limit: {amount}")
    public TopUpSettingsPage setAutoTopUpLowerLimit(String amount) {
        TopUpSettingsPage page = new TopUpSettingsPage();
        page.tapSetLowerLimit();
        page.enterLimitAmount(amount);
        platformActions.dismissKeyboard();
        page.tapSave();
        log.info("Auto top-up lower limit set to: {}", amount);
        return page;
    }

    @Step("Confirm auto top-up with 3D Secure verification")
    public TopUpSettingsPage confirmAutoTopUpWith3dSecure() {
        ConfigManager c = ConfigManager.getInstance();
        return confirmAutoTopUpWith3dSecure(
                c.get("topup.paymentPassCode"),
                c.get("topup.cvv", "123"),
                c.get("topup.3dsOtp"));
    }

    @Step("Confirm auto top-up: accept → confirm → passcode → CVV → OTP")
    public TopUpSettingsPage confirmAutoTopUpWith3dSecure(String passCode, String cvv, String otp) {
        TopUpSettingsPage page = new TopUpSettingsPage();

        // Save limits
        page.tapNextSaveLimits();

        // Accept terms
        page.tapAcceptCheckBox();

        // Confirm
        page.tapConfirmAutoTopUp();

        // Enter passcode
        passcodePage.enterPasscode(passCode);
        log.info("Auto top-up passcode entered");

        // Enter CVV if screen appears
        enterCvv(cvv);

        // Enter 3D Secure OTP
        TopUpPage topUpPage = new TopUpPage();
        complete3dSecure(topUpPage, otp);

        // Done
        page.tapDoneAutoTopUp();
        log.info("Auto top-up confirmed with 3D Secure");

        return page;
    }

    // ══════════════════════════════════════════════════
    //  AUTO TOP-UP SETTINGS MANAGEMENT
    // ══════════════════════════════════════════════════

    @Step("Open auto top-up card settings from Add Money screen")
    public TopUpSettingsPage openAutoTopUpCardSettings() {
        TopUpPage topUpPage = navigateToTopUp();
        TopUpSettingsPage settingsPage = new TopUpSettingsPage();
        settingsPage.tapTopUpSettings();
        settingsPage.tapAutoTopUpTab();
        log.info("Auto top-up card settings opened");
        return settingsPage;
    }

    @Step("Toggle auto top-up and return notification message")
    public String toggleAutoTopUp() {
        TopUpSettingsPage page = new TopUpSettingsPage();
        page.tapAutoTopUpToggle();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-notification-message"), 10);
        String message = page.getNotificationMessage();
        log.info("Auto top-up toggled — message: {}", message);
        return message;
    }

    @Step("Delete auto top-up card")
    public TopUpSettingsPage deleteAutoTopUpCard() {
        TopUpSettingsPage page = new TopUpSettingsPage();

        // Tap delete button
        page.tapDeleteAutoTopUp();

        // Verify delete confirmation dialog
        waits.waitForVisible(AppiumBy.xpath(
                "//*[@text='Delete Auto Top-up?' or @text='Delete Auto Top-Up?']"), 10);
        String question = page.getDeleteAutoTopUpQuestion();
        log.info("Delete auto top-up dialog: {}", question);

        // Confirm deletion
        page.tapConfirmDeleteAutoTopUp();

        // Wait for no auto top-up banner
        waits.waitForVisible(AppiumBy.xpath(
                "//*[contains(@text,'No auto') or contains(@text,'no auto')]"), 10);
        log.info("Auto top-up card deleted successfully");

        return page;
    }

    // ══════════════════════════════════════════════════
    //  DELETE SAVED TOP-UP CARD (from Profile)
    // ══════════════════════════════════════════════════

    @Step("Delete saved top-up card from profile settings")
    public String deleteTopUpCard() {
        TopUpSettingsPage page = new TopUpSettingsPage();

        // Navigate to profile → Manage Top Up Cards
        page.tapProfileAvatar();
        page.tapManageTopUpCards();

        // Delete card
        page.tapDeleteTopUpCard();
        page.tapConfirmDeleteCard();

        // Wait for notification
        waits.waitForVisible(AppiumBy.accessibilityId("testID-notification-message"), 10);
        String message = page.getNotificationMessage();
        log.info("Top-up card deleted — message: {}", message);

        // Navigate back to dashboard
        page.tapBack();
        page.tapBack();

        return message;
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Enter CVV code: {cvv}")
    private void enterCvv(String cvv) {
        // CVV screen uses 3 separate input boxes — need to tap first to focus
        By cvvScreen = AppiumBy.xpath(
                "//*[@text='CVV Code' or @text='Enter CVV Code'"
                + " or contains(@text,'3-digit code')]");
        waits.waitForVisible(cvvScreen, 15);

        // CVV boxes use system keyboard input (unlike passcode which uses custom keypad)
        // Find the first EditText/input box → tap to activate keyboard → sendKeys
        By cvvInput = AppiumBy.xpath(
                "//android.widget.EditText"
                + " | //*[contains(@content-desc,'testID-passCode')]"
                + " | //*[contains(@content-desc,'testID-cvv')]"
                + " | //*[contains(@content-desc,'testID-OTP')]");
        try {
            java.util.List<org.openqa.selenium.WebElement> inputs =
                    waits.findQuick(cvvInput, 5);
            if (!inputs.isEmpty()) {
                org.openqa.selenium.WebElement firstInput = inputs.get(0);
                firstInput.click();
                log.info("Tapped CVV input element — sending keys");
                firstInput.sendKeys(cvv);
            } else {
                // No EditText found — the CVV uses custom keypad like passcode
                log.info("No EditText found for CVV — using pressKey digits");
                org.openqa.selenium.Dimension size = driver.manage().window().getSize();
                swipe.tapAtCoordinates(size.getWidth() / 2, (int)(size.getHeight() * 0.45));
                platformActions.enterDigits(cvv);
            }
        } catch (Exception e) {
            // Fallback — try pressKey approach
            log.info("CVV sendKeys failed — falling back to pressKey: {}", e.getMessage());
            platformActions.enterDigits(cvv);
        }
        log.info("CVV code entered: {}", cvv);
    }

    @Step("Complete 3D Secure verification with OTP: {otp}")
    private void complete3dSecure(TopUpPage page, String otp) {
        // Wait for 3D Secure OTP field to load (bank page can be very slow — up to 60s)
        waits.waitForVisible(AppiumBy.xpath(
                "//android.widget.EditText[@resource-id='otp']"
                + " | //android.widget.EditText[contains(@resource-id,'otp')]"), 60);
        page.enter3dsOtp(otp);
        page.tapSubmitOtp();
        log.info("3D Secure OTP submitted");

        // Wait for result screen
        common.waitForResultAfterOtp(30);
    }

    @Step("Dismiss camera permission popup if visible")
    private void dismissCameraPermission() {
        try {
            java.util.List<org.openqa.selenium.WebElement> perms =
                    waits.findQuick(ALLOW_PERMISSION, 3);
            if (!perms.isEmpty()) {
                perms.get(0).click();
                log.info("Camera permission allowed");
            }
        } catch (Exception e) {
            log.debug("No camera permission popup to dismiss");
        }
    }

    /**
     * Detect if the app is stuck on a 3D Secure error page or other error state
     * from a previous incomplete transaction.
     */
    private boolean isStuckOnErrorPage() {
        try {
            By errorPage = AppiumBy.xpath(
                    "//*[contains(@text,'Status 500') or contains(@text,'Internal Server Error')"
                    + " or contains(@text,'Service unavailable') or contains(@text,'error')]"
                    + " | //*[@text='3D secure' or @text='3D Secure']");
            java.util.List<org.openqa.selenium.WebElement> errors =
                    waits.findQuick(errorPage, 3);
            if (!errors.isEmpty()) {
                log.warn("Stuck error page detected: {}", errors.get(0).getText());
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
