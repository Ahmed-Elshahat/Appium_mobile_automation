package com.urpay.pages.remittance;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Local Transfer page — bank transfer to a local (IBAN) beneficiary.
 *
 * Migrated from Katalon: Scripts/Remittance/LocalTran/ + Object Repository/android/Remittance/LocalTransfer/
 *
 * Screens covered:
 *   - Enter Amount
 *   - Beneficiary selection (search existing / add new)
 *   - Add-new-beneficiary details (IBAN / full name / nickname)
 *   - Purpose of transfer (+ optional fees / note)
 *   - Confirmation
 *   - Success (Thank You)
 *
 * Rules: private elements, @Step actions, NO assertions (booleans for the test).
 */
public class LocalTransferPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  ENTRY / AMOUNT
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@text='Enter Amount']")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Enter Amount'")
    private WebElement enterAmountLabel;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-validateAmount-main' or @text='Next' or @text='NEXT']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-primary-validateAmount-main' OR label == 'Next'")
    private WebElement nextAtAmountButton;

    // ══════════════════════════════════════════════════
    //  BENEFICIARY SELECTION
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-Search-Input")
    @iOSXCUITFindBy(accessibility = "testID-Search-Input")
    private WebElement searchBeneficiaryField;

    /**
     * Account-INDEPENDENT "first saved beneficiary" row. Saved beneficiary rows carry an indexed
     * testID ending in '.0'. Matched with contains() only (UiAutomator2's XPath does not reliably
     * support substring()/string-length()). The legacy exact id and the contacts list are fallbacks.
     */
    private static final By FIRST_BENEFICIARY = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableOpacity.8c2db26f-194e-411f-b12a-dd5ea16d47a1.0']"
            + " | //*[contains(@content-desc,'testID-TouchableOpacity') and contains(@content-desc,'.0')]"
            + " | //*[@content-desc='testID-contacts-number-0']");

    // ══════════════════════════════════════════════════
    //  PURPOSE / FEES / NOTE
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-radio-item-0")
    @iOSXCUITFindBy(accessibility = "testID-radio-item-0")
    private WebElement firstPurposeOption;

    @AndroidFindBy(xpath = "//android.widget.TextView[starts-with(@text,'Next')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label BEGINSWITH 'Next'")
    private WebElement nextAtPurposeButton;

    @AndroidFindBy(accessibility = "testID-radio-item-3")
    @iOSXCUITFindBy(accessibility = "testID-radio-item-3")
    private WebElement anotherPurposeOption;

    @AndroidFindBy(accessibility = "testID-input-direct-notes")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-notes")
    private WebElement noteField;

    // ══════════════════════════════════════════════════
    //  CONFIRMATION / SUCCESS
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-nextStep-main' or @text='Confirm' or @text='CONFIRM']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-primary-nextStep-main' OR label == 'Confirm'")
    private WebElement confirmButton;

    @AndroidFindBy(accessibility = "testID-primary-goDashboard-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-goDashboard-main")
    private WebElement doneButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS — AMOUNT
    // ══════════════════════════════════════════════════

    /** The Enter Amount editable field (Katalon EnterAmountField = //android.widget.EditText). */
    private static final By AMOUNT_FIELD = AppiumBy.xpath("//android.widget.EditText");

    /**
     * Enter the transfer amount. Mirrors Katalon (Keypad.safeSendKeys → tap field, clear, type)
     * by typing into the Enter Amount field. Falls back to a quick-amount chip if the build
     * renders chips (20/50/150/300) instead of an editable field.
     */
    @Step("Enter amount: {amount}")
    public void enterAmount(String amount) {
        if (isPresent(AMOUNT_FIELD, 3)) {
            WebElement field = driver.findElement(AMOUNT_FIELD);
            field.click();
            try {
                field.clear();
            } catch (Exception ignored) {
                // some builds disallow clear on the masked amount field
            }
            field.sendKeys(amount);
            hideKeyboard();
            return;
        }
        // Fallback: quick-amount chip matching the value exactly.
        tap(AppiumBy.xpath("//*[@text='" + amount + "' or @label='" + amount + "']"));
    }

    @Step("Tap Next on the amount screen")
    public void tapNextAtAmount() {
        tap(nextAtAmountButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — BENEFICIARY
    // ══════════════════════════════════════════════════

    @Step("Search beneficiary: {term}")
    public void searchBeneficiary(String term) {
        type(searchBeneficiaryField, term);
    }

    @Step("Select local beneficiary: {name}")
    public void selectBeneficiaryByName(String name) {
        // Prefer the named beneficiary row (Katalon selects the beneficiary by name); fall back to
        // the first saved beneficiary if the name is not visible.
        By named = AppiumBy.xpath(
                "//*[contains(@text,'" + name + "') or contains(@content-desc,'" + name + "')]");
        if (isPresent(named, 5)) {
            tap(named);
            return;
        }
        tap(FIRST_BENEFICIARY);
    }

    @Step("Select the first saved local beneficiary")
    public void selectFirstBeneficiary() {
        tap(FIRST_BENEFICIARY);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — PURPOSE / FEES / NOTE
    // ══════════════════════════════════════════════════

    @Step("Select the first purpose of transfer")
    public void selectFirstPurpose() {
        tap(firstPurposeOption);
    }

    @Step("Tap Next on the purpose screen")
    public void tapNextAtPurpose() {
        tap(nextAtPurposeButton);
    }

    @Step("Toggle 'Include Fees'")
    public void toggleIncludeFees() {
        // The Include Fees toggle is on the review screen, below the fold, and its testID is
        // obfuscated on newer builds — locate the switch by class (there is a single switch on
        // the review screen) and tap it. Scroll it into view first via native UiScrollable.
        scrollIntoViewByClass("android.widget.Switch");
        tap(AppiumBy.xpath(
                "//*[@content-desc='testID-switcher-includeFees'] | //android.widget.Switch"));
    }

    @Step("Select another purpose of transfer")
    public void selectAnotherPurpose() {
        tap(anotherPurposeOption);
    }

    @Step("Enter transfer note: {note}")
    public void enterNote(String note) {
        type(noteField, note);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — CONFIRM
    // ══════════════════════════════════════════════════

    @Step("Tap Confirm")
    public void tapConfirm() {
        tap(confirmButton);
    }

    /**
     * Reveal and tap the Confirm button. On the review screen the Confirm button sits below
     * the fold, so fling the scroll view to its end first (fast and build-independent — avoids
     * a slow UiScrollable search for an obfuscated Confirm testID). The fling never taps.
     */
    @Step("Scroll to and tap Confirm")
    public void scrollToConfirmAndTap() {
        scrollToEnd();
        tap(confirmButton);
    }

    /** Fling the first scrollable to its end to reveal the bottom action button (no tap). */
    private void scrollToEnd() {
        try {
            driver.findElement(io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true)).flingToEnd(4)"));
        } catch (Exception ignored) {
            // not scrollable / already at the end
        }
    }

    /** Scroll until an element of the given Android class is on screen (native UiScrollable). */
    private void scrollIntoViewByClass(String className) {
        try {
            driver.findElement(io.appium.java_client.AppiumBy.androidUIAutomator(
                    "new UiScrollable(new UiSelector().scrollable(true))"
                    + ".scrollIntoView(new UiSelector().className(\"" + className + "\"))"));
        } catch (Exception ignored) {
            // not scrollable or already fully visible
        }
    }

    @Step("Tap Done (go to dashboard) on the success screen")
    public void tapDone() {
        tap(doneButton);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES (no assertions)
    // ══════════════════════════════════════════════════

    public boolean isAmountScreenVisible(long timeoutSec) {
        return isDisplayed(nextAtAmountButton, timeoutSec)
                || isDisplayed(enterAmountLabel, timeoutSec);
    }

    public boolean isBeneficiaryListVisible(long timeoutSec) {
        return isPresent(FIRST_BENEFICIARY, timeoutSec);
    }

    public boolean isBeneficiaryVisible(String name, long timeoutSec) {
        return isPresent(AppiumBy.xpath(
                "//*[contains(@text,'" + name + "') or contains(@content-desc,'" + name + "')]"), timeoutSec)
                || isPresent(FIRST_BENEFICIARY, 2);
    }

    public boolean isConfirmVisible(long timeoutSec) {
        return isDisplayed(confirmButton, timeoutSec);
    }

    public boolean isTransferSuccessful(long timeoutSec) {
        // Wait ONCE for ANY success signal (returns the instant one appears) instead of waiting
        // the full timeout on the "Done" button first — its testID is obfuscated on newer builds,
        // so probing it first wasted the entire timeout before falling back to the "Thank You" text.
        By success = AppiumBy.xpath(
                "//*[contains(@text,'Thank You') or contains(@text,'Thank you') "
                + "or @content-desc='testID-primary-goDashboard-main' "
                + "or contains(@text,'successfully') or contains(@text,'Success') "
                + "or contains(@text,'requested') or contains(@text,'submitted') "
                + "or contains(@text,'received your')]");
        return isPresent(success, timeoutSec);
    }
}
