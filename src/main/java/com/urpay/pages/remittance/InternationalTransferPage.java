package com.urpay.pages.remittance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * International (MTO) Transfer page — the screens shared by EVERY money transfer operator
 * (MoneyGram, Western Union, Tahweel AlRajhi, Transfast, H2H). The flow is identical across
 * operators; only the data (provider, delivery option, beneficiary, amount) differs, so this
 * one page object serves them all.
 *
 * Migrated from Katalon:
 *   Object Repository/android/Remittance/International Transfer/**
 *   Scripts/Remittance/InternationalTran/ (Move To Main International Transfer Page,
 *   ToValidateSearchForInternationalContact, Click on First Beneficiary Contact)
 *   Scripts/Remittance/Price Estimator/ (Enter Transfer Amount, Total Amount In SAR)
 *   Scripts/Remittance/InternationalTran/Perform International Transfer/
 *   (Select Specific Service Provider, Confirmation Page, Thank You Page)
 *
 * Screens covered:
 *   - International transfer landing (search + beneficiary list)
 *   - Price estimator (enter amount)
 *   - Select service provider (MoneyGram / WU / ...)
 *   - Purpose of transfer
 *   - Confirmation
 *   - Success (Thank You)
 *
 * Rules: private elements, @Step actions, NO assertions (returns values for the test to assert).
 */
public class InternationalTransferPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  LANDING — SEARCH + BENEFICIARY
    // ══════════════════════════════════════════════════

    /** Search input on the international beneficiary landing (Katalon contactSearchEditText). */
    private static final By SEARCH_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-Search-Input-Container']//android.widget.EditText"
            + " | //*[@content-desc='testID-Search-Input']//android.widget.EditText"
            + " | //*[@content-desc='testID-Search-Input']");

    /**
     * First beneficiary contact row. The Katalon testID carries a build-specific UUID
     * (testID-View.163206aa-...0), so match the indexed '.0' row generically with contains()
     * (UiAutomator2's XPath does not reliably support substring()/string-length()), plus the
     * legacy id + the "transfer to this beneficiary" secondary button as fallbacks.
     */
    private static final By FIRST_BENEFICIARY = AppiumBy.xpath(
            "//*[@content-desc='testID-View.163206aa-9f95-4d6b-b934-a55be80eb92c.0']"
            + " | //*[contains(@content-desc,'testID-View') and contains(@content-desc,'.0')]"
            + " | //*[@content-desc='testID-secondary-undefined-main']");

    // ══════════════════════════════════════════════════
    //  PRICE ESTIMATOR — AMOUNT
    // ══════════════════════════════════════════════════

    /** Editable amount field on the price-estimator screen (Katalon totalAmoutEditText). It is an
     *  EditText, but so is the landing search box — exclude any EditText inside a Search container
     *  so we never type the amount into the search field if navigation has not completed yet. */
    private static final By AMOUNT_FIELD = AppiumBy.xpath(
            "//android.widget.EditText[not(ancestor::*[contains(@content-desc,'testID-Search-Input')])]");

    /** Next on the price estimator (Katalon nextButtonAtPriceEstimator = (//*[@text='Next'])[1]). */
    private static final By NEXT_AT_AMOUNT = AppiumBy.xpath("(//*[@text='Next'])[1]");

    /** First "... SAR" value shown on the estimator / provider screens. */
    private static final By TOTAL_AMOUNT_SAR = AppiumBy.xpath("(//*[contains(@text,'SAR')])[1]");

    // ══════════════════════════════════════════════════
    //  SELECT SERVICE PROVIDER
    // ══════════════════════════════════════════════════

    /** A service-provider card (Katalon serviceProviderCards / firstServiceProvider). */
    private static final By PROVIDER_CARD =
            AppiumBy.xpath("//android.view.ViewGroup[@content-desc='testID-data-undefined']");

    /** Next button on the provider AND purpose screens. Tap the "Next" TEXT (fires the RN onPress
     *  reliably) rather than its clickable-ancestor ViewGroup — on some screens (e.g. purpose)
     *  clicking the container ViewGroup does not trigger navigation. [last()] = the frontmost
     *  screen's button if a previous screen is still mounted behind. */
    private static final By NEXT_BUTTON = AppiumBy.xpath(
            "(//android.widget.TextView[@text='Next'])[last()]");

    // ══════════════════════════════════════════════════
    //  PURPOSE OF TRANSFER
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@content-desc='testID-radio-item-0' or @content-desc='testID-search-item-0']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-radio-item-0' OR name == 'testID-search-item-0'")
    private WebElement firstPurposeOption;

    // ══════════════════════════════════════════════════
    //  CONFIRMATION
    // ══════════════════════════════════════════════════

    /** Confirm/Next primary button on the purpose + confirmation screens. */
    @AndroidFindBy(accessibility = "testID-primary-onConfirm-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement confirmButton;

    /** Data container on the confirmation screen (Katalon confirmationDataGroupView). */
    private static final By CONFIRM_DATA_GROUP =
            AppiumBy.xpath("//*[@content-desc='testID-data-undefined']");

    // ══════════════════════════════════════════════════
    //  SUCCESS (THANK YOU)
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-master-amount-main")
    @iOSXCUITFindBy(accessibility = "testID-master-amount-main")
    private WebElement thankYouAmountInteger;

    @AndroidFindBy(accessibility = "testID-fraction-amount-main")
    @iOSXCUITFindBy(accessibility = "testID-fraction-amount-main")
    private WebElement thankYouAmountFraction;

    /** Done on the Thank You page (Katalon doneButton = testID-primary-done-main). */
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-done-main' or @text='Done']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-primary-done-main' OR label == 'Done'")
    private WebElement doneButton;

    /** Reference number on the Thank You page. */
    private static final By REFERENCE_NUMBER = AppiumBy.xpath(
            "//android.view.ViewGroup[@content-desc='testID-amount-main']"
            + "/following-sibling::android.view.ViewGroup//android.widget.TextView");

    /** Rating popup close (appears intermittently after a transfer). */
    private static final By RATING_CLOSE = AppiumBy.xpath(
            "//*[@content-desc='testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.CrossV3']"
            + " | //*[@text='Maybe later' or @text='Not now']");

    // ══════════════════════════════════════════════════
    //  ACTIONS — SEARCH / BENEFICIARY
    // ══════════════════════════════════════════════════

    @Step("Search for international beneficiary: {name}")
    public void searchBeneficiary(String name) {
        tap(SEARCH_INPUT);
        driver.findElement(SEARCH_INPUT).sendKeys(name);
        hideKeyboard();
    }

    /** Clear the beneficiary search filter (best-effort) — used on the not-found diagnostic path. */
    public void clearSearch() {
        try {
            driver.findElement(SEARCH_INPUT).clear();
        } catch (Exception ignored) {
            // field not resolvable / already clear
        }
    }

    public boolean isFirstBeneficiaryVisible(long timeoutSec) {
        return isPresent(FIRST_BENEFICIARY, timeoutSec);
    }

    /**
     * Diagnostic: the visible non-blank TextView labels on the international landing. Used to
     * discover the account's ACTUAL beneficiary names when a configured name is not found
     * (logged by the flow before it gives up), so the config can be corrected without guessing.
     */
    public java.util.List<String> getVisibleBeneficiaryNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        try {
            for (WebElement el : driver.findElements(AppiumBy.className("android.widget.TextView"))) {
                try {
                    String t = el.getText();
                    if (t != null && !t.trim().isEmpty()) {
                        names.add(t.trim());
                    }
                } catch (Exception ignored) {
                    // element went stale — skip
                }
            }
        } catch (Exception ignored) {
            // list not resolvable
        }
        return names;
    }

    /**
     * Select the searched beneficiary. The row exposes an inline "Transfer" action that opens the
     * amount (price-estimator) screen — Katalon taps firstContactViewButtonTransfer =
     * testID-secondary-undefined-main. Tapping the NAME label does NOT navigate.
     *
     * The Transfer button is an RN control whose onPress is sometimes dropped by a plain element
     * tap (the same behaviour as the Next/Confirm buttons — it fired for WU but not Tahweel), so
     * tap the clickable ancestor, verify we actually left the search list, re-tap, and
     * coordinate-tap the button centre as a last resort.
     */
    @Step("Select international beneficiary: {name}")
    public void selectBeneficiaryByName(String name) {
        By transferBtn = AppiumBy.xpath(
                "//android.widget.TextView[@text='Transfer']/ancestor-or-self::*[@clickable='true'][1]"
                + " | //*[@content-desc='testID-secondary-undefined-main']");
        if (!isPresent(transferBtn, 8)) {
            // Fallback: tap the beneficiary row container (name text alone does not navigate).
            tap(FIRST_BENEFICIARY);
            return;
        }
        for (int attempt = 1; attempt <= 3; attempt++) {
            tap(transferBtn);
            // "Navigated" = the amount screen appeared OR the Name Verification popup was raised.
            if (isOnAmountScreen(4) || isNameVerificationShown(1)) {
                return;
            }
            log.info("Transfer tap #{} for '{}' did not navigate — retrying", attempt, name);
        }
        // Last resort: coordinate-tap the Transfer button centre (RN onPress fallback).
        tapCentre(transferBtn);
    }

    /** Coordinate-tap the centre of an element — RN onPress fallback for row action buttons. */
    private void tapCentre(By locator) {
        try {
            org.openqa.selenium.Rectangle r = driver.findElement(locator).getRect();
            tapAtCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
            log.info("Coordinate-tapped the beneficiary Transfer button");
        } catch (Exception e) {
            log.warn("Coordinate tap on the Transfer button failed: {}", e.getMessage());
        }
    }

    /**
     * True if the "Name Verification" bottom-sheet ("Update beneficiary's middle name to complete
     * the transfer") is showing — raised after selecting a beneficiary whose name is incomplete.
     */
    public boolean isNameVerificationShown(long timeoutSec) {
        return isPresent(AppiumBy.xpath(
                "//*[@text='Name Verification' or contains(@text,'middle name')]"), timeoutSec);
    }

    /** True if the price-estimator amount screen (its amount field) is showing. */
    public boolean isOnAmountScreen(long timeoutSec) {
        return isPresent(AMOUNT_FIELD, timeoutSec);
    }

    /**
     * Complete the Name Verification: tap "Update", fill the beneficiary's middle name on the
     * Beneficiary Details screen (testID-input-direct-middleName) and Save (testID-primary--main),
     * so the transfer is no longer gated. After Save the app returns to the beneficiary list; the
     * caller re-initiates the transfer (the popup will not reappear once the name is complete).
     */
    @Step("Complete Name Verification: add middle name '{middleName}' and Save")
    public void completeNameVerification(String middleName) {
        By update = AppiumBy.xpath(
                "//*[@text='Update' or @content-desc='testID-primary-action-main']");
        if (isPresent(update, 5)) {
            tap(update);
        }
        By middleField = AppiumBy.accessibilityId("testID-input-direct-middleName");
        waitUtils.waitForClickable(middleField, 15);
        type(middleField, middleName);
        hideKeyboard();
        By save = AppiumBy.xpath("//*[@content-desc='testID-primary--main' or @text='Save']");
        tap(save);
        log.info("Saved beneficiary middle name '{}'", middleName);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — AMOUNT (PRICE ESTIMATOR)
    // ══════════════════════════════════════════════════

    @Step("Enter transfer amount: {amount}")
    public void enterAmount(String amount) {
        WebElement field = waitUtils.waitForClickable(AMOUNT_FIELD);
        field.click();
        try {
            field.clear();
        } catch (Exception ignored) {
            // masked field may disallow clear
        }
        field.sendKeys(amount);
        hideKeyboard();
    }

    @Step("Tap Next on the amount (price estimator) screen")
    public void tapNextAtAmount() {
        tap(NEXT_AT_AMOUNT);
    }

    /** The first "… SAR" value on screen (estimator/provider), digits+decimal only. */
    public String getTotalAmountInSar() {
        if (!isPresent(TOTAL_AMOUNT_SAR, 10)) {
            return "";
        }
        return getText(TOTAL_AMOUNT_SAR).replaceAll("[^0-9.]", "").trim();
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — SELECT SERVICE PROVIDER
    // ══════════════════════════════════════════════════

    /**
     * Select the MTO service-provider card. On this screen the cards render only the provider
     * LOGO (no "MoneyGram" text) and the clickable card containers carry no stable testID, so the
     * reliable discriminator is the provider's brand MARKER text — MoneyGram (red logo) shows
     * "Easy to track" / "Anywallet". Tap the clickable card that contains the marker; fall back to
     * a card whose text contains the provider name, then the Nth provider card.
     */
    @Step("Select service provider: {providerName} (marker '{marker}', index {index})")
    public void selectServiceProvider(String providerName, String marker, int index) {
        if (marker != null && !marker.isEmpty()) {
            // MoneyGram (red logo) is the AVAILABLE provider card — it has a "Details" button (not
            // the "currently unavailable"/"Refresh" one) AND shows the brand marker (e.g. "Easy to
            // track"); other available providers (e.g. Western Union) don't show that marker. Scope
            // to the available card so a DOWN provider that shares the marker (e.g. Transfast) is
            // not matched, then tap the marker's nearest clickable ancestor to select the card.
            By availableCard = AppiumBy.xpath(
                    "//android.view.ViewGroup[@content-desc='testID-data-undefined']"
                    + "[.//*[@text='Details']]"
                    + "[not(.//*[contains(@text,'unavailable') or @text='Refresh'])]"
                    + "[.//*[@text='" + marker + "']]"
                    + "//*[@text='" + marker + "']/ancestor::*[@clickable='true'][1]");
            if (isPresent(availableCard, 8)) {
                tap(availableCard);
                return;
            }
            // Fallback (single-provider-with-marker routes, e.g. Egypt): nearest clickable
            // ancestor of the marker text.
            By anyMarkerCard = AppiumBy.xpath(
                    "//*[@text='" + marker + "']/ancestor::*[@clickable='true'][1]");
            if (isPresent(anyMarkerCard, 4)) {
                tap(anyMarkerCard);
                return;
            }
        }
        // Fallback: a card whose text contains the provider name.
        By byName = AppiumBy.xpath(
                "//android.view.ViewGroup[@content-desc='testID-data-undefined']"
                + "[.//*[contains(@text,'" + providerName + "')]]");
        if (isPresent(byName, 2)) {
            tap(byName);
            return;
        }
        // Last resort: the Nth provider card (1-based → XPath position()).
        By byIndex = AppiumBy.xpath(
                "(//android.view.ViewGroup[@content-desc='testID-data-undefined'])[" + index + "]");
        if (isPresent(byIndex, 3)) {
            tap(byIndex);
            return;
        }
        tap(PROVIDER_CARD);
    }

    /** Payout-bank dropdown shown on the provider screen for Tahweel AlRajhi (H2H) Cash Pickup. */
    private static final By BANK_DROPDOWN = AppiumBy.xpath(
            "//*[@content-desc='testID-multi-select-bankCode' "
            + "or @content-desc='testID-multi-select-BANKNAME']");

    /** First bank option in the opened bank list. */
    private static final By BANK_FIRST_VALUE =
            AppiumBy.accessibilityId("testID-search-item-0");

    /**
     * Select the first payout bank on the provider screen. Tahweel AlRajhi (H2H) Cash Pickup
     * requires a payout bank before Next; MoneyGram / Western Union have no such dropdown. Migrated
     * from Katalon ToValidateSelectBank (bank dropdown testID-multi-select-bankCode → first value
     * testID-search-item-0).
     */
    @Step("Select the first payout bank (Tahweel AlRajhi / H2H)")
    public void selectFirstBank() {
        if (!isPresent(BANK_DROPDOWN, 10)) {
            log.warn("Tahweel payout-bank dropdown (testID-multi-select-bankCode) not found — "
                    + "skipping bank selection");
            return;
        }
        tap(BANK_DROPDOWN);
        if (isPresent(BANK_FIRST_VALUE, 10)) {
            tap(BANK_FIRST_VALUE);
            log.info("Selected the first payout bank (Tahweel AlRajhi)");
        } else {
            // The bank list never loaded — on a SIT route outage the blocking "Service is currently
            // unavailable. Please try again later." banner is up over the bank screen. Fail fast
            // with the categorized backend issue here instead of grinding on to Next + its 12s
            // advance-wait before failing.
            raiseIfServiceUnavailable();
            log.warn("Bank dropdown opened but no bank option (testID-search-item-0) appeared");
        }
    }

    @Step("Tap Next on the service-provider screen")
    public void tapNextAtServiceProvider() {
        tap(NEXT_BUTTON);
        // The blocking "Service is currently unavailable. Please try again later." banner flashes
        // transiently right after this tap when the provider/route service is down — sample it now.
        raiseIfServiceUnavailable();
        // Deterministic backup (the banner is transient and easily missed): if Next did not advance
        // us to the purpose/confirmation screen, the route's provider service is down → raise the
        // categorized backend issue rather than failing later with a cryptic "Next not clickable".
        By advanced = AppiumBy.xpath(
                "//*[@content-desc='testID-radio-item-0']"
                + " | //*[contains(@text,'Select purpose') or contains(@text,'purpose of transfer')]"
                + " | //*[@text='Delivery Option' or @text='Received amount' or @text='Confirm']");
        if (!isPresent(advanced, 12)) {
            raiseServiceUnavailable();
        }
    }

    /**
     * Throw a categorized backend error if the "Service is currently unavailable" banner is
     * showing. The message contains "currently unavailable" so Allure buckets it under
     * "Backend service unavailable (SIT)" (see categories.json); the listener attaches a
     * screenshot. This is a SIT backend/provider outage, not a test defect.
     */
    private void raiseIfServiceUnavailable() {
        if (isServiceUnavailable(2)) {
            raiseServiceUnavailable();
        }
    }

    /** Throw the categorized "Backend service unavailable (SIT)" error (see categories.json). */
    private void raiseServiceUnavailable() {
        throw new IllegalStateException(
                "Service is currently unavailable. Please try again later. — SIT backend/"
                + "provider outage on the international transfer route (not a test defect).");
    }

    /**
     * True if the BLOCKING backend banner "Service is currently unavailable. Please try again
     * later." is showing. Matched by "try again later" / "Please try again" — the distinctive part
     * that a provider CARD's "This service is currently unavailable" label (always present when one
     * provider is down but MoneyGram is fine) does NOT contain, so a down provider card is not a
     * false positive. This SIT backend/provider outage disables Next; it is not a test defect.
     */
    public boolean isServiceUnavailable(long timeoutSec) {
        return isPresent(AppiumBy.xpath(
                "//*[contains(@text,'try again later') or contains(@text,'Please try again')]"),
                timeoutSec);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — PURPOSE
    // ══════════════════════════════════════════════════

    @Step("Select the first purpose of transfer")
    public void selectFirstPurpose() {
        // If the purpose radio is not present we've already advanced (e.g. to Confirmation) —
        // nothing to select, return so the caller doesn't fail on a stale locator.
        By radio = AppiumBy.accessibilityId("testID-radio-item-0");
        if (!isPresent(radio, 8)) {
            log.info("Purpose radio not present — already advanced, skipping purpose selection");
            return;
        }
        // The selectable element is the TouchableOpacity NESTED inside testID-radio-item-0 —
        // tapping the outer radio ViewGroup does not fire the RN onPress. Tap the inner touchable
        // (fallback: the radio row element).
        By touchable = AppiumBy.xpath(
                "//*[@content-desc='testID-radio-item-0']"
                + "//*[contains(@content-desc,'testID-TouchableOpacity')]");
        if (isPresent(touchable, 5)) {
            tap(touchable);
        } else {
            tap(firstPurposeOption);
        }
        log.info("Selected first purpose of transfer");
    }

    @Step("Tap Next on the purpose screen")
    public void tapNextAtPurpose() {
        // Tapping Next can be a no-op until the purpose selection has committed; retry (re-selecting
        // the purpose) until the confirmation screen appears or the purpose screen is gone.
        By purposeMarker = AppiumBy.xpath("//*[contains(@text,'Select purpose')]");
        for (int attempt = 1; attempt <= 3; attempt++) {
            tap(NEXT_BUTTON);
            // The backend "Service is currently unavailable" banner flashes transiently right after
            // this tap when the provider/route service is down — sample it NOW (it auto-dismisses).
            raiseIfServiceUnavailable();
            if (isConfirmationVisible(6) || !isPresent(purposeMarker, 2)) {
                return;
            }
            // The Next button's RN onPress does not always fire via element taps — coordinate-tap
            // the fixed full-width button at the bottom, then wait GENEROUSLY (the provider quote
            // on the confirmation screen can take several seconds to load).
            tapBottomPrimaryByCoordinates();
            raiseIfServiceUnavailable();
            if (isConfirmationVisible(12) || !isPresent(purposeMarker, 2)) {
                return;
            }
            log.warn("Still on the purpose screen after Next (attempt {}) — re-selecting purpose", attempt);
            selectFirstPurpose();
        }
    }

    /** Coordinate-tap the fixed full-width primary button at the bottom (centre, ~93% down) —
     *  a reliable fallback when an RN button's onPress does not fire via element taps. */
    private void tapBottomPrimaryByCoordinates() {
        try {
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            tapAtCoordinates((int) (size.getWidth() * 0.5), (int) (size.getHeight() * 0.93));
            log.info("Coordinate-tapped the bottom primary button");
        } catch (Exception e) {
            log.warn("Coordinate tap on the bottom button failed: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — CONFIRMATION
    // ══════════════════════════════════════════════════

    public boolean isConfirmationVisible(long timeoutSec) {
        // Distinctive confirmation-screen labels (NOT testID-data-undefined, which also exists on
        // the purpose screen and would false-positive before we've actually advanced).
        return isPresent(AppiumBy.xpath(
                "//*[@text='Delivery Option' or @text='Payout location' or @text='Exchange rate'"
                + " or @text='Received amount' or @text='Confirm']"), timeoutSec);
    }

    /** Diagnostic: capture the confirmation screen page source (used by the no-money validation). */
    public void dumpConfirmationScreen() {
        dumpPageSource("confirmationScreen");
    }

    /** Read a labelled value on the confirmation screen, e.g. "Delivery Option", "Purpose of transfer". */
    public String getConfirmationValue(String label) {
        By value = AppiumBy.xpath(
                "//*[@text='" + label + "']/following-sibling::android.view.ViewGroup//android.widget.TextView");
        if (!isPresent(value, 5)) {
            return "";
        }
        return getText(value).trim();
    }

    /** Reveal (scroll into view) and tap the Confirm button on the confirmation screen. */
    @Step("Scroll to and tap Confirm")
    public void scrollToConfirmAndTap() {
        // The Confirm button's testID varies (onConfirm-main / primary--main) and its RN onPress
        // may not fire via element taps.
        By confirm = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onConfirm-main' "
                + "or @content-desc='testID-primary--main' "
                + "or @content-desc='testID-primary-validate-main' or @text='Confirm']"
                + "/ancestor-or-self::*[@clickable='true'][1]");

        // 1) Bring Confirm INTO the visible viewport with real swipe-up gestures. The button sits
        //    below the fold; a flingToEnd is unreliable on RN and — worse — tapping an element that
        //    is only present in the RN tree while still OFF-SCREEN makes the tap land in the
        //    navigation-bar area and navigates BACK. So scroll it on-screen first, then tap.
        revealConfirmButton(confirm);

        // 2) Tap the on-screen Confirm element; if the RN onPress ignores the element tap and we're
        //    still on the confirmation screen, coordinate-tap the revealed bottom button.
        if (isInViewport(confirm)) {
            tap(confirm);
        }
        if (isConfirmationVisible(3)) {
            tapBottomPrimaryByCoordinates();
        }
    }

    /** Swipe up until the Confirm button is inside the visible viewport (a few attempts max). */
    private void revealConfirmButton(By confirm) {
        for (int attempt = 0; attempt < 5 && !isInViewport(confirm); attempt++) {
            swipeUp();
        }
    }

    /**
     * True only when the element exists AND its centre is within the visible screen. React Native
     * keeps off-screen elements in the tree, and tapping one lands on the navigation bar (→ Back),
     * so this viewport check guards the element tap.
     */
    private boolean isInViewport(By locator) {
        try {
            java.util.List<WebElement> els = driver.findElements(locator);
            if (els.isEmpty()) {
                return false;
            }
            org.openqa.selenium.Rectangle r = els.get(0).getRect();
            int centerY = r.getY() + r.getHeight() / 2;
            int screenH = driver.manage().window().getSize().getHeight();
            return centerY > 0 && centerY < screenH;
        } catch (Exception e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — SUCCESS (THANK YOU)
    // ══════════════════════════════════════════════════

    public boolean isTransferSuccessful(long timeoutSec) {
        By success = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-done-main' or @content-desc='testID-master-amount-main'"
                + " or contains(@text,'Thank') or contains(@text,'successfully')"
                + " or contains(@text,'Success') or contains(@text,'received your')]");
        return isPresent(success, timeoutSec);
    }

    /** Total amount on the Thank You page = integer + fraction parts concatenated (digits/decimal). */
    public String getThankYouTotal() {
        String integer = isDisplayed(thankYouAmountInteger, 8) ? getText(thankYouAmountInteger) : "";
        String fraction = isDisplayed(thankYouAmountFraction, 2) ? getText(thankYouAmountFraction) : "";
        return (integer + fraction).replaceAll("[^0-9.]", "").trim();
    }

    public String getReferenceNumber() {
        if (!isPresent(REFERENCE_NUMBER, 5)) {
            return "";
        }
        return getText(REFERENCE_NUMBER).trim();
    }

    @Step("Tap Done on the Thank You page")
    public void tapDone() {
        scrollToEnd();
        tap(doneButton);
    }

    @Step("Close the rating popup if present")
    public void closeRatingPopupIfPresent() {
        if (isPresent(RATING_CLOSE, 1)) {
            try {
                driver.findElement(RATING_CLOSE).click();
            } catch (Exception ignored) {
                // popup went away on its own
            }
        }
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    /** Fling the first scrollable to its end to reveal a bottom action button (never taps). */
    private void scrollToEnd() {
        try {
            driver.findElement(AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).flingToEnd(4)"));
        } catch (Exception ignored) {
            // not scrollable / already at the end
        }
    }
}
