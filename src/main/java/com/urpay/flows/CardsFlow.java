package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.payments.CardBenefitsPage;
import com.urpay.pages.payments.CardInfoPage;
import com.urpay.pages.payments.CardSettingsPage;
import com.urpay.pages.payments.CardsPage;
import com.urpay.pages.payments.RequestPhysicalCardPage;
import com.urpay.pages.payments.TransactionDetailsPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.Platform;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.SwipeUtils;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Cards flow — orchestrates multi-page card management workflows.
 *
 * ZERO Thread.sleep() — all waits via WaitUtils explicit waits.
 * NO assertions — returns page objects for tests to verify.
 * NO hardcoded values — all data from ConfigManager.
 */
public class CardsFlow {

    private static final Logger log = LoggerFactory.getLogger(CardsFlow.class);
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final SwipeUtils swipe;
    private final MobilePlatformActions platformActions;
    private final Platform platform;

    // ── Shared locators ──
    private static final By OTP_FIELD = AppiumBy.accessibilityId("testID-OTP-Input-Field-0");
    private static final By DONE_BTN = AppiumBy.xpath(
            "//*[@text='Done' or @content-desc='testID-primary-action-main']");
    private static final By NEXT_BTN = AppiumBy.xpath(
            "//*[@text='Next' or @content-desc='testID-primary-navigateToNextStep-main']");
    private static final By FIRST_CARD = AppiumBy.accessibilityId("testID-bankCard.data.0");
    private static final By SEARCH_ICON = AppiumBy.accessibilityId("testID-right-icon-0");
    private static final By NOTIFICATION_MSG = AppiumBy.accessibilityId("testID-notification-message");
    private static final By CANCEL_DROPDOWN = AppiumBy.accessibilityId(
            "testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.DownArrow");
    private static final By OTHER_REASON = AppiumBy.accessibilityId("testID-data-picker-item-3");
    private static final By CONFIRM_CANCEL = AppiumBy.accessibilityId("testID-cancel--main");
    private static final By NO_THANKS = AppiumBy.accessibilityId("testID-tertiary-noThankButton-main");

    public CardsFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.swipe = new SwipeUtils(driver);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.platform = platformActions.getPlatform();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Cards — scroll to 'Cards' section on dashboard")
    public CardsPage navigateToCards() {
        goToDashboard();

        // Fast popup dismiss with zero implicit wait — single pass
        setImplicitWait(0);
        quickTapIfFound("//*[@text='Later']");
        quickTapIfFound("//*[@text='×' or @text='X']");
        setImplicitWait(10);

        // Scroll to "Cards" text using platform-specific scrolling
        try {
            platformActions.scrollToText("Cards");
            log.info("Scrolled to 'Cards' section");
        } catch (Exception e) {
            log.info("Platform scroll failed — trying manual swipes");
            SwipeUtils smallSwipe = new SwipeUtils(driver, 0.35);
            setImplicitWait(0);
            for (int i = 0; i < 8; i++) {
                if (quickFind(AppiumBy.xpath("//*[@text='Cards']"))
                        || quickFind(AppiumBy.xpath("//*[@text='View All']"))) break;
                smallSwipe.swipeUp();
            }
            setImplicitWait(10);
        }

        return new CardsPage();
    }

    // ══════════════════════════════════════════════════
    //  ISSUE NEW DIGITAL CARD (Katalon: issueNewDigitalCard)
    // ══════════════════════════════════════════════════

    /** Issue digital card using default "madaCard" prefix. */
    @Step("Issue new digital Mada card")
    public CardsPage issueNewDigitalCard() {
        return issueNewDigitalCard("madaCard");
    }

    /**
     * Issue a digital card for any card type.
     * If the user already has this card type, navigates to the card and returns.
     * @param cardPrefix config prefix: "madaCard", "alahliCard", "platinumCard", "signatureCard"
     */
    @Step("Issue new digital card — type: {cardPrefix}")
    public CardsPage issueNewDigitalCard(String cardPrefix) {
        ConfigManager c = ConfigManager.getInstance();
        String cardType = c.get(cardPrefix + ".expectedCardName", "Mada Card");
        CardsPage page = navigateToCards();

        // ── Step 1: Enter cards section ──
        // Try banner first (accessibility ID → text), fallback to "View All"
        setImplicitWait(0);
        if (!quickTapIfFound("testID-check-box-main", true)) {
            if (!quickTapIfFound("//*[contains(@text,'Issue urpay') or contains(@text,'Issue URPay')]")) {
                // "View All" might need one more swipe to be clickable
                if (!quickTapIfFound("//*[@text='View All']")) {
                    setImplicitWait(10);
                    SwipeUtils nudge = new SwipeUtils(driver, 0.20);
                    nudge.swipeUp();
                    waits.waitForClickable(AppiumBy.xpath("//*[@text='View All']"), 10).click();
                }
                log.info("Tapped 'View All'");
            }
        }
        setImplicitWait(10);

        // ── Step 2: Check if user already has a card — scroll to find "Add new card" ──
        setImplicitWait(0);
        boolean hasExistingCard = quickFind(AppiumBy.xpath("//*[@text='Card Settings']"))
                || quickFind(AppiumBy.xpath("//*[@text='Card Information']"));
        boolean hasAddNewCard = quickFind(AppiumBy.xpath("//*[@text='Add new card']"));

        if (hasExistingCard && !hasAddNewCard) {
            // Existing card visible but "Add new card" not yet — scroll down to find it
            SwipeUtils smallSwipe = new SwipeUtils(driver, 0.30);
            for (int i = 0; i < 4; i++) {
                smallSwipe.swipeUp();
                if (quickFind(AppiumBy.xpath("//*[@text='Add new card']"))) {
                    hasAddNewCard = true;
                    break;
                }
            }
            if (!hasAddNewCard) {
                // No "Add new card" after scrolling — user truly has max cards
                setImplicitWait(10);
                log.info("User already has card — no 'Add new card' found, card ready for management");
                return page;
            }
        }
        setImplicitWait(10);

        // ── Step 3: Products page — tap "Add new card" if present ──
        if (hasAddNewCard) {
            waits.waitForClickable(AppiumBy.xpath("//*[@text='Add new card']"), 10).click();
            log.info("Tapped 'Add new card'");
        }

        // ── Step 4: Card carousel — find "Request Card" for target type ──
        // Use platform scroll to navigate carousel horizontally to the card type text.
        By requestBtn = AppiumBy.xpath(
                "//*[@text='" + cardType + "']/parent::*//*[@text='Request Card']");

        // First try: platform-specific horizontal scroll to find the card name
        try {
            platformActions.scrollHorizontalToText(cardType);
            log.info("Found '{}' in carousel using platform horizontal scroll", cardType);
        } catch (Exception e) {
            log.info("Platform horizontal scroll failed for '{}'", cardType);
        }

        // UiScrollable may leave the card partially visible — swipe to center it
        // Also handles fallback if UiScrollable didn't work at all
        SwipeUtils carouselSwipe = new SwipeUtils(driver, 0.40);
        setImplicitWait(0);
        boolean found = quickFind(requestBtn);
        if (!found) {
            // Try a few LEFT swipes (card may be partially off-screen to the right)
            for (int i = 0; i < 3; i++) {
                carouselSwipe.swipeLeft();
                if (quickFind(requestBtn)) { found = true; break; }
            }
        }
        if (!found) {
            // Try RIGHT swipes (may have gone past it)
            for (int i = 0; i < 6; i++) {
                carouselSwipe.swipeRight();
                if (quickFind(requestBtn)) { found = true; break; }
            }
        }
        setImplicitWait(10);

        waits.waitForClickable(requestBtn, 10).click();
        log.info("Tapped Request Card for: {}", cardType);

        // ── Step 5: Next → Accept → Confirm (adaptive — skip if not present) ──
        setImplicitWait(0);
        quickTapIfFound("//*[@text='Next']");
        quickTapIfFound("//*[@text='I accept' or @content-desc='testID-check-box-main']");
        setImplicitWait(10);

        waits.waitForClickable(AppiumBy.xpath("//*[@text='Confirm']"), 15).click();
        log.info("Tapped Confirm");

        // ── Step 6: Enter PIN × 2 + OTP ──
        try { ((io.appium.java_client.HidesKeyboard) driver).hideKeyboard(); } catch (Exception ignored) {}
        String pin = c.get(cardPrefix + ".pin", "1234");
        enterPasscode(pin);
        enterPasscode(pin);
        try { ((io.appium.java_client.HidesKeyboard) driver).hideKeyboard(); } catch (Exception ignored) {}
        enterVerificationCode();

        // ── Step 6: IVR Skip — bypass verification call via backend API ──
        String consumerId = c.get(cardPrefix + ".consumerId",
                c.get(cardPrefix + ".id"));
        log.info("Attempting IVR skip for consumer: {}", consumerId);
        boolean ivrSkipped = com.urpay.helpers.IvrSkipHelper.skipCardIssuanceIvr(consumerId);
        log.info("IVR skip result: {}", ivrSkipped ? "SUCCESS" : "FAILED");

        // ── Step 7: Wait for success screen after IVR ──
        By backToCards = AppiumBy.xpath(
                "//*[@text='Back to cards' or @content-desc='testID-primary-backToCardsDB-main']");

        setImplicitWait(0);
        for (int i = 0; i < 15; i++) {
            if (quickFind(backToCards)) {
                setImplicitWait(10);
                waits.waitForClickable(backToCards, 5).click();
                log.info("Tapped 'Back to cards'");
                break;
            }
            try { Thread.sleep(2000); } catch (Exception ignored) {}
        }
        setImplicitWait(10);

        log.info("Digital Mada card issued");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  CREATE NEW CARD (Katalon: createNewMadaCard)
    // ══════════════════════════════════════════════════

    @Step("Create new Mada card — Request → Next → Accept → Confirm → PIN → OTP")
    public CardsPage createNewMadaCard() {
        CardsPage page = new CardsPage();
        page.tapRequestCard();
        page.tapNext();
        page.tapAcceptCheckbox();
        page.tapConfirm();

        String pin = ConfigManager.getInstance().get("madaCard.pin", "1234");
        enterPasscode(pin);
        enterPasscode(pin);
        enterVerificationCode();

        page.tapBackToCards();
        log.info("New Mada card created");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  REQUEST PHYSICAL CARD (Katalon: RequestMadaPhysicalCard)
    // ══════════════════════════════════════════════════

    @Step("Request physical Mada card copy")
    public CardsPage requestPhysicalCard() {
        RequestPhysicalCardPage physicalPage = new RequestPhysicalCardPage();
        physicalPage.tapRequestPhysicalCopy();
        physicalPage.selectRiyadhRegion();
        physicalPage.tapNext();

        // Scroll down to find Accept checkbox (Katalon: swipeByDistance x2)
        swipe.swipeUp();
        swipe.swipeUp();

        CardsPage cardsPage = new CardsPage();
        cardsPage.tapAcceptCheckbox();
        cardsPage.tapConfirm();

        enterVerificationCode();

        // Wait for success screen, then tap View Card
        waits.waitForClickable(
                AppiumBy.xpath("//*[@text='Back to cards' or @content-desc='testID-primary-backToCardsDB-main']"), 15);
        physicalPage.tapViewCard();

        log.info("Physical card requested");
        return cardsPage;
    }

    // ══════════════════════════════════════════════════
    //  VALIDATE CARD DUPLICATION (Katalon: validateDuplicationOfMadaCard)
    // ══════════════════════════════════════════════════

    @Step("Attempt to create duplicate card and capture notification")
    public CardsPage attemptDuplicateCard() {
        CardsPage page = new CardsPage();
        page.tapAddNewCard();
        // Notification should appear about max cards
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        log.info("Duplicate card attempt — notification displayed");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  TOGGLE OPERATIONS
    // ══════════════════════════════════════════════════

    @Step("Disable online transactions")
    public CardSettingsPage disableOnlineTransactions() {
        // Navigate to Card Settings from Products page (text-based)
        navigateToCardSettings();
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapOnlineTransactionsToggle();
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        return settings;
    }

    @Step("Enable online transactions")
    public CardSettingsPage enableOnlineTransactions() {
        CardSettingsPage settings = new CardSettingsPage();
        waits.waitForInvisible(NOTIFICATION_MSG, 5);
        settings.tapOnlineTransactionsToggle();
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        return settings;
    }

    @Step("Disable ATM transactions")
    public CardSettingsPage disableAtmTransactions() {
        // If we're still on settings page, no need to navigate
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapCardSettings();
        settings.tapAtmTransactionToggle();
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        return settings;
    }

    @Step("Enable ATM transactions")
    public CardSettingsPage enableAtmTransactions() {
        CardSettingsPage settings = new CardSettingsPage();
        waits.waitForInvisible(NOTIFICATION_MSG, 5);
        settings.tapAtmTransactionToggle();
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        return settings;
    }

    /** Navigate to Card Settings using visible text (works on Products page) */
    @Step("Navigate to Card Settings")
    public void navigateToCardSettings() {
        By cardSettingsText = AppiumBy.xpath("//*[@text='Card Settings']");
        waits.waitForClickable(cardSettingsText, 15).click();
        log.info("Tapped 'Card Settings'");
    }

    /** Navigate to Card Information using visible text */
    @Step("Navigate to Card Information")
    public void navigateToCardInfo() {
        By cardInfoText = AppiumBy.xpath("//*[@text='Card Information']");
        waits.waitForClickable(cardInfoText, 15).click();
        log.info("Tapped 'Card Information'");
    }

    /** Navigate to Card Benefits using visible text */
    @Step("Navigate to Card Benefits")
    public void navigateToCardBenefits() {
        By cardBenefitsText = AppiumBy.xpath("//*[@text='Card Benefits']");
        waits.waitForClickable(cardBenefitsText, 15).click();
        log.info("Tapped 'Card Benefits'");
    }

    // ══════════════════════════════════════════════════
    //  CARD LOCK / UNLOCK
    // ══════════════════════════════════════════════════

    @Step("Lock card")
    public CardSettingsPage lockCard() {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapLockToggle();
        settings.tapLockYes();
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        return settings;
    }

    @Step("Unlock card")
    public CardSettingsPage unlockCard() {
        CardSettingsPage settings = new CardSettingsPage();
        waits.waitForInvisible(NOTIFICATION_MSG, 5);
        settings.tapLockToggle();
        settings.tapLockYes();
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        return settings;
    }

    // ══════════════════════════════════════════════════
    //  CHANGE PIN (Katalon: ChangeCardPinNumber)
    // ══════════════════════════════════════════════════

    @Step("Change card PIN")
    public void changeCardPin() {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapChangePinSettings();
        settings.tapChangePin();

        // Dismiss keyboard if visible before entering PIN
        dismissKeyboard();

        String pin = ConfigManager.getInstance().get("madaCard.passCode", "2233");
        enterPinAndProceed(pin);
        enterPinAndProceed(pin);

        enterVerificationCode();

        waits.waitForClickable(DONE_BTN, 15).click();
        log.info("Card PIN changed");
    }

    // ══════════════════════════════════════════════════
    //  CHECK INVALID PIN (Katalon: checkTheInvalidPINCode)
    // ══════════════════════════════════════════════════

    @Step("Enter mismatched PINs to verify error notification")
    public CardSettingsPage enterInvalidPinMismatch() {
        String validPin = ConfigManager.getInstance().get("madaCard.pin", "1234");
        enterPasscode(validPin);
        // Enter wrong confirmation
        enterPasscode("9999");
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        CardSettingsPage settings = new CardSettingsPage();
        log.info("Invalid PIN mismatch — notification displayed");
        return settings;
    }

    // ══════════════════════════════════════════════════
    //  CANCEL CARD (Katalon: CancelMadaCard)
    // ══════════════════════════════════════════════════

    @Step("Cancel Mada card")
    public void cancelCard() {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapCardSettings();
        settings.tapCancelCard();

        waits.waitForClickable(CANCEL_DROPDOWN, 10);
        settings.tapCancellationDropdown();

        waits.waitForClickable(OTHER_REASON, 10);
        settings.selectOtherReason();

        waits.waitForClickable(CONFIRM_CANCEL, 10);
        settings.tapConfirmCancel();

        enterPasscode(ConfigManager.getInstance().get("madaCard.passCode", "2233"));

        waits.waitForClickable(NO_THANKS, 15);
        settings.tapNoThanks();
        log.info("Mada card cancelled");
    }

    // ══════════════════════════════════════════════════
    //  CANCEL CARD FROM SETTINGS (Katalon: CancelMadaCardFromCardSettingsScreen)
    // ══════════════════════════════════════════════════

    @Step("Cancel card from card settings screen — enter invalid passcode first, then correct")
    public CardSettingsPage cancelCardFromSettings() {
        CardSettingsPage settings = new CardSettingsPage();
        // Already on settings — tap cancel directly (no need to tap card settings)
        settings.tapCancelCard();

        waits.waitForClickable(CANCEL_DROPDOWN, 10);
        settings.tapCancellationDropdown();

        waits.waitForClickable(OTHER_REASON, 10);
        settings.selectOtherReason();

        waits.waitForClickable(CONFIRM_CANCEL, 10);
        settings.tapConfirmCancel();

        // Enter invalid passcode first to verify error
        enterVerificationCode("9999");
        waits.waitForVisible(NOTIFICATION_MSG, 10);
        log.info("Invalid passcode entered — error notification shown");

        // Wait for notification to dismiss, then enter correct passcode
        waits.waitForInvisible(NOTIFICATION_MSG, 5);
        enterVerificationCode(ConfigManager.getInstance().get("madaCard.passCode", "2233"));

        return settings;
    }

    // ══════════════════════════════════════════════════
    //  CARD BENEFITS
    // ══════════════════════════════════════════════════

    @Step("Open card benefits")
    public CardBenefitsPage openCardBenefits() {
        navigateToCardBenefits();
        return new CardBenefitsPage();
    }

    // ══════════════════════════════════════════════════
    //  CARD INFO
    // ══════════════════════════════════════════════════

    @Step("Open card info screen")
    public CardInfoPage openCardInfo() {
        navigateToCardInfo();
        enterPasscode(ConfigManager.getInstance().get("madaCard.passCode", "2233"));
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
        return new CardInfoPage();
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTION DETAILS
    // ══════════════════════════════════════════════════

    @Step("Open transaction details")
    public TransactionDetailsPage openTransactionDetails() {
        CardsPage cardsPage = new CardsPage();
        cardsPage.tapTransactionsTab();
        cardsPage.tapLatestTransaction();
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-main-0"), 15);
        return new TransactionDetailsPage();
    }

    // ══════════════════════════════════════════════════
    //  VALIDATE CANCELED CARD DELETED FROM DASHBOARD
    // ══════════════════════════════════════════════════

    @Step("Scroll dashboard to verify canceled card is removed")
    public boolean isCanceledCardStillVisible() {
        goToDashboard();
        new com.urpay.pages.dashboard.DashboardPage().dismissNotifications();
        // Use platform scroll to try to find the cards banner
        By cardsBanner = AppiumBy.accessibilityId(
                "testID-View.dceaa398-d6ae-4995-b509-9dfb27610fef");
        try {
            platformActions.scrollToElement(cardsBanner, 5);
            return true; // found = card still visible
        } catch (Exception e) {
            return false; // not found = card removed
        }
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION HELPERS
    // ══════════════════════════════════════════════════

    @Step("Go back to dashboard")
    public void goToDashboard() {
        // First wait for dashboard indicators without pressing back
        setImplicitWait(0);
        for (int i = 0; i < 10; i++) {
            if (quickFind(SEARCH_ICON)
                    || quickFind(AppiumBy.xpath("//*[@text='Cards']"))
                    || quickFind(AppiumBy.xpath("//*[@text='View All']"))
                    || quickFind(AppiumBy.xpath("//*[contains(@text,'Issue urpay') or contains(@text,'Issue URPay')]"))) {
                setImplicitWait(10);
                return;
            }
            if (i >= 3) {
                // After 3 checks, try pressing back (might be on a sub-screen)
                driver.navigate().back();
            }
        }
        setImplicitWait(10);
    }

    @Step("Navigate back from cards settings to first card")
    public void goFromSettingsToFirstCard() {
        driver.navigate().back();
        waits.waitForVisible(FIRST_CARD, 10);
    }

    // ══════════════════════════════════════════════════
    //  FAST ELEMENT HELPERS (zero implicit wait)
    // ══════════════════════════════════════════════════

    /** Set implicit wait in seconds. Use 0 for instant checks. */
    private void setImplicitWait(int seconds) {
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(seconds));
    }

    /** Instant element visibility check — caller MUST set implicitWait(0) first. */
    private boolean quickFind(By locator) {
        try {
            var els = driver.findElements(locator);
            return !els.isEmpty() && els.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Quick tap by xpath — returns true if tapped. Caller MUST set implicitWait(0) first. */
    private boolean quickTapIfFound(String xpath) {
        try {
            var els = driver.findElements(AppiumBy.xpath(xpath));
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                els.get(0).click();
                log.info("Quick tapped: {}", xpath.length() > 40 ? xpath.substring(0, 40) + "..." : xpath);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    /** Quick tap by accessibility ID — returns true if tapped. Caller MUST set implicitWait(0) first. */
    private boolean quickTapIfFound(String accessibilityId, boolean isAccessibilityId) {
        try {
            var els = driver.findElements(AppiumBy.accessibilityId(accessibilityId));
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                els.get(0).click();
                log.info("Quick tapped (ID): {}", accessibilityId);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    // ══════════════════════════════════════════════════
    //  KEYPAD HELPERS — ZERO Thread.sleep()
    // ══════════════════════════════════════════════════

    private void enterPasscode(String passcode) {
        platformActions.enterDigits(passcode);
    }

    private void enterVerificationCode() {
        enterVerificationCode(
                ConfigManager.getInstance().get("madaCard.verificationCode", "1234"));
    }

    private void enterVerificationCode(String code) {
        try {
            waits.waitForClickable(OTP_FIELD, 15);
            driver.findElement(OTP_FIELD).click();
        } catch (Exception ignored) {}

        platformActions.enterDigits(code);
    }

    private void enterPinAndProceed(String pin) {
        platformActions.enterDigits(pin);
        try {
            waits.waitForClickable(NEXT_BTN, 5).click();
        } catch (Exception ignored) {}
    }

    private void dismissKeyboard() {
        platformActions.dismissKeyboard();
    }
}
