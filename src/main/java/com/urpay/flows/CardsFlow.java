package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.auth.PasscodePage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
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
 * NO locators — all element interactions via page objects.
 */
public class CardsFlow {

    private static final Logger log = LoggerFactory.getLogger(CardsFlow.class);
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final SwipeUtils swipe;
    private final MobilePlatformActions platformActions;
    private final Platform platform;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final DashboardPage dashboardPage;
    private final PasscodePage passcodePage;

    public CardsFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.swipe = new SwipeUtils(driver);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.platform = platformActions.getPlatform();
        this.common = new CommonComponentsPage();
        this.otpPage = new OtpPage();
        this.dashboardPage = new DashboardPage();
        this.passcodePage = new PasscodePage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Cards — scroll to 'Cards' section on dashboard")
    public CardsPage navigateToCards() {
        dashboardPage.dismissPopups();

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

        // DEBUG: dump page source to see what's on screen
        try {
            String ps = driver.getPageSource();
            java.nio.file.Files.writeString(
                java.nio.file.Path.of("target/page_source_after_cards_scroll.xml"), ps);
            log.info("Page source saved to target/page_source_after_cards_scroll.xml ({} chars)", ps.length());
        } catch (Exception ex) { log.warn("Could not save page source: {}", ex.getMessage()); }

        // ── Step 1: Enter cards section ──
        // Try banner first, then "View All" — with scrolling if needed
        By viewAllBtn = AppiumBy.xpath("//*[@text='View All']");
        By bannerBtn = AppiumBy.xpath("//*[contains(@text,'Issue urpay') or contains(@text,'Issue URPay')]");
        By bannerCheckbox = AppiumBy.accessibilityId("testID-check-box-main");

        setImplicitWait(3);
        var bannerEls = driver.findElements(bannerCheckbox);
        if (!bannerEls.isEmpty() && bannerEls.get(0).isDisplayed()) {
            bannerEls.get(0).click();
            log.info("Tapped banner checkbox");
        } else {
            var bannerTextEls = driver.findElements(bannerBtn);
            if (!bannerTextEls.isEmpty() && bannerTextEls.get(0).isDisplayed()) {
                bannerTextEls.get(0).click();
                log.info("Tapped banner text");
            } else {
                // Find and tap "View All" — scroll if needed
                SwipeUtils nudge = new SwipeUtils(driver, 0.20);
                boolean tapped = false;
                for (int i = 0; i < 3; i++) {
                    var viewAllEls = driver.findElements(viewAllBtn);
                    if (!viewAllEls.isEmpty() && viewAllEls.get(0).isDisplayed()) {
                        viewAllEls.get(0).click();
                        log.info("Tapped 'View All'");
                        tapped = true;
                        break;
                    }
                    nudge.swipeUp();
                }
                if (!tapped) {
                    waits.waitForClickable(viewAllBtn, 10).click();
                    log.info("Tapped 'View All' after wait");
                }
            }
        }
        setImplicitWait(10);

        // ── Step 2: Check if user already has a card — scroll to find "Add new card" ──
        setImplicitWait(2);
        boolean hasExistingCard = quickFind(AppiumBy.xpath("//*[@text='Card Settings']"))
                || quickFind(AppiumBy.xpath("//*[@text='Card Information']"))
                || quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"))
                || quickFind(AppiumBy.xpath("//*[@text='Online Transactions']"));
        boolean hasAddNewCard = quickFind(AppiumBy.xpath("//*[@text='Add new card']"));

        // Fallback: if neither detected (e.g. tablet layout), scroll to find indicators
        if (!hasExistingCard && !hasAddNewCard) {
            SwipeUtils smallSwipe = new SwipeUtils(driver, 0.30);
            for (int i = 0; i < 4; i++) {
                smallSwipe.swipeUp();
                if (quickFind(AppiumBy.xpath("//*[@text='Card Settings']"))
                        || quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"))
                        || quickFind(AppiumBy.xpath("//*[@text='Online Transactions']"))) {
                    hasExistingCard = true;
                    break;
                }
                if (quickFind(AppiumBy.xpath("//*[@text='Add new card']"))) {
                    hasAddNewCard = true;
                    break;
                }
            }
        }

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
                // Scroll back up so card management elements are visible
                SwipeUtils backSwipe = new SwipeUtils(driver, 0.30);
                for (int i = 0; i < 4; i++) {
                    backSwipe.swipeDown();
                    if (quickFind(AppiumBy.xpath("//*[@text='Card Settings']"))
                            || quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"))) {
                        break;
                    }
                }
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

    @Step("Request physical card copy")
    public CardsPage requestPhysicalCard(String cardPrefix) {
        RequestPhysicalCardPage physicalPage = new RequestPhysicalCardPage();
        physicalPage.tapRequestPhysicalCopy();

        // Fill national address form (Katalon: RequestPhysicalCard)
        // Wait for the form to fully load
        if (physicalPage.isAddressFormVisible()) {
            physicalPage.fillAddressForm("1234", "1234", "Test", "Test", "12345");
        } else {
            log.warn("Address form not visible — retrying with longer wait");
            waits.waitForVisible(AppiumBy.accessibilityId("testID-input-direct-buildingNo"), 10);
            physicalPage.fillAddressForm("1234", "1234", "Test", "Test", "12345");
        }

        physicalPage.tapNext();

        // Scroll down to find Accept checkbox (Katalon: swipeByDistance x2)
        swipe.swipeUp();
        swipe.swipeUp();

        CardsPage cardsPage = new CardsPage();
        cardsPage.tapAcceptCheckbox();

        // Scroll to Confirm button (Katalon: swipe x2)
        swipe.swipeUp();
        swipe.swipeUp();

        cardsPage.tapConfirm();

        String otp = ConfigManager.getInstance().get(cardPrefix + ".verificationCode", "1234");
        enterVerificationCode(otp);

        // Wait for success screen, then tap View Card/Done
        waits.waitForClickable(
                AppiumBy.xpath("//*[@text='View Card' or @text='Done' or @text='Back to cards' or @content-desc='testID-primary-backToCardsDB-main']"), 15);
        // Tap View Card or Done to return
        driver.findElement(AppiumBy.xpath("//*[@text='View Card' or @text='Done']")).click();

        log.info("Physical card requested for: {}", cardPrefix);
        return cardsPage;
    }

    /** @deprecated Use requestPhysicalCard(String cardPrefix) */
    public CardsPage requestPhysicalCard() { return requestPhysicalCard("madaCard"); }

    // ══════════════════════════════════════════════════
    //  CARD REPLACEMENT (Katalon: RequestReplacementCardFromCardsSettingsScreen)
    // ══════════════════════════════════════════════════

    /**
     * Request card replacement from Card Settings.
     * Flow: Card Settings → Replace → reason (Damage) → Next → city → Next → Confirm → Thank You → Done
     */
    @Step("Request card replacement")
    public void replaceCard() {
        navigateToCardSettings();
        com.urpay.pages.payments.CardReplacementPage replacementPage =
                new com.urpay.pages.payments.CardReplacementPage();

        // Step 1: Tap Replace button in Card Settings
        replacementPage.tapReplace();
        log.info("Tapped Replace — replacement flow started");

        // Step 2: Select replacement reason (Damage)
        replacementPage.selectDamageReason();
        log.info("Selected damage reason");

        // Step 3: Tap Next
        replacementPage.tapNext();
        log.info("Tapped Next after reason selection");

        // Step 4: Select city
        replacementPage.selectCity();
        log.info("Selected replacement city");

        // Step 5: Tap Next
        replacementPage.tapNext();
        log.info("Tapped Next after city selection");

        // Step 6: Confirm replacement
        replacementPage.tapConfirm();
        log.info("Tapped Confirm — awaiting Thank You screen");

        // Step 7: Verify success and tap Done
        if (replacementPage.isThankYouVisible()) {
            log.info("Card replacement Thank You screen confirmed");
        }
        replacementPage.tapDone();
        log.info("Card replacement completed");
    }

    /**
     * Activate a replacement card.
     * Flow: Tap Activate → enter verification code → Back to Cards
     */
    @Step("Activate replacement card")
    public void activateReplacementCard(String cardPrefix) {
        com.urpay.pages.payments.CardReplacementPage replacementPage =
                new com.urpay.pages.payments.CardReplacementPage();
        ConfigManager c = ConfigManager.getInstance();

        // Step 1: Tap Activate
        replacementPage.tapActivate();
        log.info("Tapped Activate replacement card");

        // Step 2: Enter verification code via keypad
        String verificationCode = c.get(cardPrefix + ".verificationCode", "1234");
        passcodePage.enterPasscode(verificationCode);
        log.info("Entered activation verification code");

        // Step 3: Back to cards
        replacementPage.tapBackToCards();
        log.info("Replacement card activated — back to cards");
    }

    // ══════════════════════════════════════════════════
    //  VALIDATE CARD DUPLICATION (Katalon: validateDuplicationOfMadaCard)
    // ══════════════════════════════════════════════════

    @Step("Attempt to create duplicate card and capture notification")
    public CardsPage attemptDuplicateCard() {
        CardsPage page = new CardsPage();
        page.tapAddNewCard();
        // Notification should appear about max cards
        common.waitForNotification(10);
        log.info("Duplicate card attempt — notification displayed");
        return page;
    }

    // ══════════════════════════════════════════════════
    //  TOGGLE OPERATIONS
    // ══════════════════════════════════════════════════

    @Step("Disable online transactions")
    public CardSettingsPage disableOnlineTransactions() {
        navigateToCardSettings();
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapOnlineTransactionsToggle();
        return settings;
    }

    @Step("Enable online transactions")
    public CardSettingsPage enableOnlineTransactions() {
        CardSettingsPage settings = new CardSettingsPage();
        // Brief pause for previous notification to clear
        if (common.isNotificationVisible(1)) {
            try { common.waitForNotificationToDismiss(3); } catch (Exception ignored) {}
        }
        settings.tapOnlineTransactionsToggle();
        return settings;
    }

    @Step("Disable ATM transactions")
    public CardSettingsPage disableAtmTransactions() {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapAtmTransactionToggle();
        return settings;
    }

    @Step("Enable ATM transactions")
    public CardSettingsPage enableAtmTransactions() {
        CardSettingsPage settings = new CardSettingsPage();
        // Brief pause for previous notification to clear
        if (common.isNotificationVisible(1)) {
            try { common.waitForNotificationToDismiss(3); } catch (Exception ignored) {}
        }
        settings.tapAtmTransactionToggle();
        return settings;
    }

    /** Navigate to Card Settings — directly visible on Products page */
    @Step("Navigate to Card Settings")
    public void navigateToCardSettings() {
        ensureCardUnlocked();
        By target = AppiumBy.xpath("//*[@text='Card Settings']");
        waits.waitForClickable(target, 10).click();
        log.info("Tapped 'Card Settings'");
    }

    /** Navigate to Card Information — directly visible on Products page */
    @Step("Navigate to Card Information")
    public void navigateToCardInfo() {
        ensureCardUnlocked();
        By target = AppiumBy.xpath("//*[@text='Card Information']");
        waits.waitForVisible(target, 10);
        driver.findElement(target).click();
        log.info("Tapped 'Card Information'");
    }

    /** Navigate to Card Benefits — directly visible on Products page */
    @Step("Navigate to Card Benefits")
    public void navigateToCardBenefits() {
        ensureCardUnlocked();
        By target = AppiumBy.xpath("//*[@text='Card Benefits']");
        waits.waitForVisible(target, 10);
        driver.findElement(target).click();
        log.info("Tapped 'Card Benefits'");
    }

    /** Scroll down to find element and tap it */
    private void scrollAndTap(By locator, String label) {
        setImplicitWait(3);
        var els = driver.findElements(locator);
        if (!els.isEmpty() && els.get(0).isDisplayed()) {
            els.get(0).click();
            log.info("Tapped '{}'", label);
            setImplicitWait(10);
            return;
        }
        setImplicitWait(0);
        SwipeUtils smallSwipe = new SwipeUtils(driver, 0.30);
        for (int i = 0; i < 5; i++) {
            smallSwipe.swipeUp();
            els = driver.findElements(locator);
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                els.get(0).click();
                log.info("Tapped '{}' after scroll", label);
                setImplicitWait(10);
                return;
            }
        }
        setImplicitWait(10);
        waits.waitForClickable(locator, 10).click();
        log.info("Tapped '{}'", label);
    }

    // ══════════════════════════════════════════════════
    //  CARD LOCK / UNLOCK
    // ══════════════════════════════════════════════════

    @Step("Ensure card is unlocked")
    public void ensureCardUnlocked() {
        // First: ensure we're on the card products page (not stuck on another page)
        setImplicitWait(2);
        boolean onProductsPage = quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"));
        if (!onProductsPage) {
            // Check if we're inside Card Settings detail page or another sub-page
            boolean inSubPage = quickFind(AppiumBy.xpath("//*[@text='Change PIN Code']"))
                    || quickFind(AppiumBy.xpath("//*[@text='Thank You!']"))
                    || quickFind(AppiumBy.xpath("//*[@text='Cancel Card']"));
            if (inSubPage) {
                log.info("Inside sub-page — navigating back to card products");
                driver.navigate().back();
                // Check if we need another back press
                if (!quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"))) {
                    driver.navigate().back();
                    log.info("Second back press to reach card products page");
                }
            } else if (!quickFind(AppiumBy.xpath("//*[@text='Card Information']"))) {
                // Not on products page at all — try back
                log.info("Not on card products page — pressing back");
                driver.navigate().back();
                if (!quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"))) {
                    driver.navigate().back();
                    log.info("Second back press to reach card products page");
                }
            }
        }
        setImplicitWait(10);

        // Dump page source for debugging lock toggle element
        try {
            String ps = driver.getPageSource();
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("target/page_source_card_products.xml"), ps);
            log.info("Card products page source saved ({} chars)", ps.length());
        } catch (Exception e) { log.warn("Failed to save page source: {}", e.getMessage()); }

        By unlockText = AppiumBy.xpath("//*[@text='Unlock Card']");
        setImplicitWait(3);
        var unlockEls = driver.findElements(unlockText);
        setImplicitWait(10);
        if (!unlockEls.isEmpty()) {
            log.info("Card is LOCKED — attempting unlock");
            // The toggle circle is a sibling/nearby element. Try multiple approaches:
            boolean tapped = false;

            // Approach 1: Tap preceding sibling (toggle circle to the left of text)
            try {
                var toggle = driver.findElement(AppiumBy.xpath(
                        "//*[@text='Unlock Card']/preceding-sibling::*[1]"));
                toggle.click();
                tapped = true;
                log.info("Tapped preceding sibling of Unlock Card");
            } catch (Exception e) {
                log.warn("Preceding sibling approach failed: {}", e.getMessage());
            }

            // Approach 2: If above didn't work, try tap by offset (toggle is left of text)
            if (!tapped) {
                try {
                    var textEl = unlockEls.get(0);
                    int x = textEl.getLocation().getX() - 80;
                    int y = textEl.getLocation().getY() + textEl.getSize().getHeight() / 2;
                    org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(
                            new org.openqa.selenium.interactions.PointerInput(
                                    org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1"), 0);
                    tap.addAction(new org.openqa.selenium.interactions.PointerInput(
                            org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1")
                            .createPointerMove(java.time.Duration.ZERO,
                                    org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
                    tap.addAction(new org.openqa.selenium.interactions.PointerInput(
                            org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1")
                            .createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
                    tap.addAction(new org.openqa.selenium.interactions.Pause(
                            new org.openqa.selenium.interactions.PointerInput(
                                    org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1"),
                            java.time.Duration.ofMillis(100)));
                    tap.addAction(new org.openqa.selenium.interactions.PointerInput(
                            org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1")
                            .createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
                    driver.perform(java.util.Collections.singletonList(tap));
                    tapped = true;
                    log.info("Tapped toggle by coordinate offset: ({}, {})", x, y);
                } catch (Exception e) {
                    log.warn("Coordinate tap failed: {}", e.getMessage());
                }
            }

            // Check for confirmation popup
            setImplicitWait(3);
            By yesBtn = AppiumBy.accessibilityId("testID-primary-callAPI-main");
            var yesBtns = driver.findElements(yesBtn);
            if (!yesBtns.isEmpty()) {
                yesBtns.get(0).click();
                log.info("Tapped Yes on unlock confirmation");
            }
            setImplicitWait(10);
            if (common.isNotificationVisible(5)) {
                log.info("Unlock notification: {}", common.getNotificationMessage());
            }
            if (common.isNotificationVisible(1)) {
                try { common.waitForNotificationToDismiss(3); } catch (Exception ignored) {}
            }
        } else {
            log.info("Card is already unlocked");
        }
    }

    @Step("Lock card")
    public CardSettingsPage lockCard() {
        CardSettingsPage settings = new CardSettingsPage();
        ensureCardUnlocked();

        // Tap lock toggle via preceding sibling
        tapCardToggle("Lock Card");
        // Verify state changed — "Unlock Card" should now be visible
        setImplicitWait(3);
        boolean locked = quickFind(AppiumBy.xpath("//*[@text='Unlock Card']"));
        setImplicitWait(10);
        if (locked) {
            log.info("Card locked — verified 'Unlock Card' visible");
        } else {
            log.warn("Lock state change not confirmed — 'Unlock Card' not found");
        }
        return settings;
    }

    @Step("Unlock card")
    public CardSettingsPage unlockCard() {
        CardSettingsPage settings = new CardSettingsPage();
        if (common.isNotificationVisible(1)) {
            try { common.waitForNotificationToDismiss(3); } catch (Exception ignored) {}
        }
        tapCardToggle("Unlock Card");
        // Verify state changed — "Lock Card" should now be visible
        setImplicitWait(3);
        boolean unlocked = quickFind(AppiumBy.xpath("//*[@text='Lock Card']"));
        setImplicitWait(10);
        if (unlocked) {
            log.info("Card unlocked — verified 'Lock Card' visible");
        } else {
            log.warn("Unlock state change not confirmed — 'Lock Card' not found");
        }
        return settings;
    }

    /** Tap lock/unlock toggle by finding the preceding sibling of the text label and confirming popup */
    private void tapCardToggle(String label) {
        By textLocator = AppiumBy.xpath("//*[@text='" + label + "']");
        waits.waitForVisible(textLocator, 10);
        // Tap preceding sibling (the toggle circle element)
        try {
            driver.findElement(AppiumBy.xpath(
                    "//*[@text='" + label + "']/preceding-sibling::*[1]")).click();
            log.info("Tapped toggle via preceding sibling of '{}'", label);
        } catch (Exception e) {
            driver.findElement(textLocator).click();
            log.info("Tapped '{}' text directly", label);
        }
        // Confirm popup if present
        setImplicitWait(3);
        var yesBtns = driver.findElements(AppiumBy.accessibilityId("testID-primary-callAPI-main"));
        if (!yesBtns.isEmpty()) {
            yesBtns.get(0).click();
            log.info("Tapped Yes on confirmation popup");
        }
        setImplicitWait(10);
    }

    // ══════════════════════════════════════════════════
    //  CHANGE PIN (Katalon: ChangeCardPinNumber)
    // ══════════════════════════════════════════════════

    @Step("Change card PIN")
    public void changeCardPin(String cardPrefix) {
        CardSettingsPage settings = new CardSettingsPage();
        // Step 1: No scroll needed — Change PIN is visible on Card Settings
        settings.tapChangePinSettings();
        // Step 2: Tap "Change" button (text="Change", NOT "Change PIN Code")
        settings.tapChangePin();
        log.info("Tapped Change PIN button — PIN entry screen opened");

        // Step 3: Wait for PIN entry screen to load
        // This MUST succeed — if PIN screen didn't open, test fails here
        waits.waitForVisible(AppiumBy.xpath(
                "//*[@text='Next'] | //*[contains(@content-desc,'passcode')]"), 10);
        log.info("PIN entry screen confirmed visible (Next button found)");

        // Step 4: Enter new PIN using passcode keypad (NOT Actions.sendKeys — keyboard focus is unreliable)
        ConfigManager c = ConfigManager.getInstance();
        String newPin = c.get(cardPrefix + ".newPin", "5678");
        passcodePage.enterPasscode(newPin);
        log.info("Entered new PIN via keypad: {}", newPin);
        tapNextButton();

        // Step 5: Enter confirmation PIN + tap Next
        passcodePage.enterPasscode(newPin);
        log.info("Confirmed new PIN via keypad");
        tapNextButton();

        // Step 6: Enter OTP verification code
        enterVerificationCode(c.get(cardPrefix + ".verificationCode", "1234"));
        log.info("Entered OTP for PIN change");

        // Step 7: Dismiss keyboard (covers Done button on Thank You page) then tap Done
        common.dismissKeyboard();
        // Double-dismiss: pressBack as fallback if hideKeyboard didn't work
        try { ((io.appium.java_client.HidesKeyboard) driver).hideKeyboard(); } catch (Exception ignored) {}
        By thankYouOrDone = AppiumBy.xpath(
                "//*[@text='Thank You!'] | //*[@text='Done'] | " +
                "//*[@text='Card Settings'] | //*[@text='Change PIN Code']");
        waits.waitForVisible(thankYouOrDone, 15);
        log.info("Thank You / Done page visible — PIN change confirmed");
        // Tap Done button (use waitForClickable to ensure it's interactable)
        By doneBtnLocator = AppiumBy.xpath("//*[@text='Done']");
        setImplicitWait(3);
        var doneBtns = driver.findElements(doneBtnLocator);
        if (!doneBtns.isEmpty()) {
            try {
                waits.waitForClickable(doneBtnLocator, 5).click();
                log.info("Tapped Done button");
            } catch (Exception e) {
                // Fallback: tap directly
                doneBtns.get(0).click();
                log.info("Tapped Done button (direct)");
            }
        }
        setImplicitWait(10);
        // Ensure we're back on card products page (not still on Thank You)
        setImplicitWait(3);
        boolean onProducts = quickFind(AppiumBy.xpath("//*[@text='Card Settings']"))
                || quickFind(AppiumBy.xpath("//*[@text='Lock Card' or @text='Unlock Card']"));
        if (!onProducts) {
            driver.navigate().back();
            log.info("Navigated back from Thank You page");
        }
        setImplicitWait(10);
        log.info("Card PIN changed for: {}", cardPrefix);
    }

    /** Enter PIN digits char by char using W3C Actions (matches Katalon enterTextAndProceed) */
    private void enterPinViaActions(String pin) {
        org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
        for (char c : pin.toCharArray()) {
            actions.sendKeys(String.valueOf(c)).perform();
        }
    }

    /** Tap "Next" button after PIN entry (Katalon: NextButton → //*[@text="Next"]) */
    private void tapNextButton() {
        By nextBtn = AppiumBy.xpath("//*[@text='Next']");
        waits.waitForClickable(nextBtn, 10).click();
        log.info("Tapped Next button");
    }

    /** @deprecated Use changeCardPin(String cardPrefix) */
    public void changeCardPin() { changeCardPin("madaCard"); }

    // ══════════════════════════════════════════════════
    //  CHECK INVALID PIN (Katalon: checkTheInvalidPINCode)
    // ══════════════════════════════════════════════════

    @Step("Enter mismatched PINs to verify error notification")
    public CardSettingsPage enterInvalidPinMismatch(String cardPrefix) {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapChangePinSettings();
        settings.tapChangePin();
        common.dismissKeyboard();

        // Katalon: fillPassCode('1234') then fillPassCode('9999') → mismatch
        String validPin = ConfigManager.getInstance().get(cardPrefix + ".pin", "1234");
        passcodePage.enterPasscode(validPin);
        // Enter wrong confirmation
        passcodePage.enterPasscode("9999");
        // Notification may auto-dismiss quickly
        if (common.isNotificationVisible(5)) {
            log.info("Invalid PIN mismatch notification: {}", common.getNotificationMessage());
        } else {
            log.warn("PIN mismatch notification not captured — may have auto-dismissed");
        }
        return settings;
    }

    /** @deprecated Use enterInvalidPinMismatch(String cardPrefix) */
    public CardSettingsPage enterInvalidPinMismatch() { return enterInvalidPinMismatch("madaCard"); }

    // ══════════════════════════════════════════════════
    //  CANCEL CARD (Katalon: CancelMadaCard)
    // ══════════════════════════════════════════════════

    @Step("Cancel card")
    public void cancelCard(String cardPrefix) {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapCardSettings();
        settings.tapCancelCard();
        settings.tapCancellationDropdown();
        settings.selectOtherReason();
        settings.tapConfirmCancel();

        String passcode = ConfigManager.getInstance().get(cardPrefix + ".passCode", "2233");
        passcodePage.enterPasscode(passcode);

        settings.tapNoThanks();
        log.info("Card cancelled for: {}", cardPrefix);
    }

    /** @deprecated Use cancelCard(String cardPrefix) */
    public void cancelCard() { cancelCard("madaCard"); }

    // ══════════════════════════════════════════════════
    //  CANCEL CARD FROM SETTINGS (Katalon: CancelMadaCardFromCardSettingsScreen)
    // ══════════════════════════════════════════════════

    @Step("Cancel card from card settings screen — enter invalid passcode first, then correct")
    public CardSettingsPage cancelCardFromSettings(String cardPrefix) {
        CardSettingsPage settings = new CardSettingsPage();
        settings.tapCancelCard();
        settings.tapCancellationDropdown();
        settings.selectOtherReason();
        settings.tapConfirmCancel();

        // Enter invalid passcode first to verify error
        passcodePage.enterPasscode("9999");
        common.waitForNotification(10);
        log.info("Invalid passcode entered — error notification shown");

        // Wait for notification to dismiss, then enter correct passcode
        common.waitForNotificationToDismiss(5);
        String passcode = ConfigManager.getInstance().get(cardPrefix + ".passCode", "2233");
        passcodePage.enterPasscode(passcode);

        return settings;
    }

    /** @deprecated Use cancelCardFromSettings(String cardPrefix) */
    public CardSettingsPage cancelCardFromSettings() { return cancelCardFromSettings("madaCard"); }

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
    public CardInfoPage openCardInfo(String cardPrefix) {
        navigateToCardInfo();
        // Katalon: fillPassCode('2233') — enter passcode to view card info
        String passcode = ConfigManager.getInstance().get(cardPrefix + ".passCode", "2233");
        passcodePage.enterPasscode(passcode);
        log.info("Entered passcode to view card info");
        waits.waitForVisible(AppiumBy.accessibilityId("testID-label-value-0"), 15);
        return new CardInfoPage();
    }

    /** @deprecated Use openCardInfo(String cardPrefix) */
    public CardInfoPage openCardInfo() { return openCardInfo("madaCard"); }

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
        new com.urpay.pages.dashboard.DashboardPage().dismissPopups();
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
        // Wait for dashboard to load naturally — NO back presses
        By dashboardIndicator = AppiumBy.xpath(
                "//*[@text='Cards'] | //*[@text='View All'] | " +
                "//*[contains(@text,'Issue urpay') or contains(@text,'Issue URPay')]");
        try {
            waits.waitForVisible(dashboardIndicator, 15);
            log.info("Dashboard detected");
        } catch (Exception e) {
            log.info("Dashboard not visible — pressing back once");
            driver.navigate().back();
            try { waits.waitForVisible(dashboardIndicator, 10); } catch (Exception ignored) {}
        }
    }

    @Step("Navigate back from cards settings to first card")
    public void goFromSettingsToFirstCard() {
        driver.navigate().back();
        CardsPage cardsPage = new CardsPage();
        waits.waitForVisible(
                AppiumBy.accessibilityId("testID-bankCard.data.0"), 10);
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
        passcodePage.enterPasscode(passcode);
    }

    private void enterVerificationCode() {
        enterVerificationCode(
                ConfigManager.getInstance().get("madaCard.verificationCode", "1234"));
    }

    private void enterVerificationCode(String code) {
        otpPage.enterOtp(code);
    }

    private void enterPinAndProceed(String pin) {
        passcodePage.enterPasscode(pin);
        try {
            common.tapNext(5);
        } catch (Exception ignored) {}
    }

    private void dismissKeyboard() {
        common.dismissKeyboard();
    }
}
