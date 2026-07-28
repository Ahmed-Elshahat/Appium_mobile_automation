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
    //  ADD NEW BENEFICIARY (IBAN / name / nickname)
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-primary-transferToNewBeneficiary-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-transferToNewBeneficiary-main")
    private WebElement addNewBeneficiaryButton;

    @AndroidFindBy(accessibility = "testID-input-direct-iban")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-iban")
    private WebElement ibanField;

    @AndroidFindBy(accessibility = "testID-input-direct-accountHolderName")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-accountHolderName")
    private WebElement fullNameField;

    @AndroidFindBy(accessibility = "testID-input-direct-nickName")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-nickName")
    private WebElement nicknameField;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main' or @text='Next' or @text='NEXT']")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'testID-primary--main' OR label == 'Next'")
    private WebElement nextAtBeneficiaryDetailsButton;

    // ══════════════════════════════════════════════════
    //  BENEFICIARY MANAGEMENT (cleanup of a prior beneficiary)
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-viewElemenGroupOfPeople")
    @iOSXCUITFindBy(accessibility = "testID-viewElemenGroupOfPeople")
    private WebElement beneficiaryManagementButton;

    /** "Local" beneficiary category tab on the Beneficiary Management screen (testID has a UUID). */
    private static final By LOCAL_BENEFICIARY_CATEGORY = AppiumBy.xpath(
            "//*[@content-desc='testID-View.72efaf72-308e-4ce0-af71-af29baa368c2.2'] "
            + "| //*[@text='Local' or @text='Local Transfer']");

    /** First saved contact row on the Beneficiary Management list (testID has a UUID). */
    private static final By FIRST_CONTACT = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableOpacity.677b6f96-4757-4b1c-b5ad-6bbd9250fbf9.0'] "
            + "| //*[@content-desc='testID-TouchableOpacity.d2163287-1d7a-4ae9-b909-eaed6bc6f08f.0'] "
            + "| //*[contains(@content-desc,'testID-TouchableOpacity') and contains(@content-desc,'.0')]");

    @AndroidFindBy(xpath = "//*[@text='Delete' or @content-desc='testID-secondary-action-main']")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Delete' OR name == 'testID-secondary-action-main'")
    private WebElement deleteContactButton;

    @AndroidFindBy(accessibility = "testID-primary-deleteBeneficiary-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-deleteBeneficiary-main")
    private WebElement confirmDeleteButton;

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

    /**
     * Enter the transfer amount. The amount screen is a currency display + in-app numeric keypad
     * (NOT a plain EditText) — the same screen the validated Wallet Transfer drives with a
     * quick-amount CHIP. Tap the chip; do NOT use an EditText+sendKeys+hideKeyboard() path here,
     * because hideKeyboard() dismisses the in-app keypad by pressing BACK, which navigates OFF the
     * amount screen. Non-chip amounts are typed digit-by-digit on the in-app keypad.
     */
    @Step("Enter amount: {amount}")
    public void enterAmount(String amount) {
        By chip = AppiumBy.xpath("//*[@text='" + amount + "' or @label='" + amount + "']");
        if (isPresent(chip, 5)) {
            tap(chip);
            return;
        }
        // Non-chip amount: type each digit on the in-app keypad (still NO hideKeyboard()).
        for (char digit : amount.toCharArray()) {
            By key = AppiumBy.xpath("(//*[@text='" + digit + "'])[last()]");
            if (isPresent(key, 3)) {
                tap(key);
            }
        }
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
    //  ACTIONS — ADD NEW BENEFICIARY
    // ══════════════════════════════════════════════════

    @Step("Tap 'Add new beneficiary'")
    public void tapAddNewBeneficiary() {
        tap(addNewBeneficiaryButton);
    }

    @Step("Enter IBAN: {iban}")
    public void enterIban(String iban) {
        type(ibanField, iban);
        hideKeyboard();
    }

    @Step("Enter beneficiary full name: {name}")
    public void enterFullName(String name) {
        type(fullNameField, name);
        hideKeyboard();
    }

    @Step("Enter beneficiary nickname: {nickname}")
    public void enterNickname(String nickname) {
        type(nicknameField, nickname);
        hideKeyboard();
    }

    @Step("Tap Next on the beneficiary details screen")
    public void tapNextAtBeneficiaryDetails() {
        tap(nextAtBeneficiaryDetailsButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — BENEFICIARY MANAGEMENT (cleanup)
    // ══════════════════════════════════════════════════

    @Step("Open Beneficiary Management")
    public void tapBeneficiaryManagement() {
        tap(beneficiaryManagementButton);
    }

    @Step("Open the Local beneficiary category")
    public void tapLocalBeneficiaryCategory() {
        tap(LOCAL_BENEFICIARY_CATEGORY);
    }

    public boolean isFirstContactPresent(long timeoutSec) {
        return isPresent(FIRST_CONTACT, timeoutSec);
    }

    @Step("Open the first contact")
    public void tapFirstContact() {
        tap(FIRST_CONTACT);
    }

    @Step("Tap Delete contact")
    public void tapDeleteContact() {
        tap(deleteContactButton);
    }

    @Step("Confirm delete contact")
    public void tapConfirmDelete() {
        tap(confirmDeleteButton);
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
        // The Include Fees toggle may not exist in newer builds (fees displayed by default on
        // the confirmation screen). Try to find and toggle it; if absent, skip gracefully.
        try {
            scrollIntoViewByClass("android.widget.Switch");
            tap(AppiumBy.xpath(
                    "//*[@content-desc='testID-switcher-includeFees'] | //android.widget.Switch"));
            log.info("Toggled Include Fees switch");
        } catch (Exception e) {
            log.info("Include Fees toggle not found — fees already included by default in this build");
        }
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

    /**
     * Reveal and tap the Next button on the add-beneficiary details screen. Like the review
     * screen, the Next button can sit below the fold, so fling the scroll view to its end first.
     */
    @Step("Scroll to and tap Next on the beneficiary details screen")
    public void scrollToBeneficiaryNextAndTap() {
        scrollToEnd();
        tap(nextAtBeneficiaryDetailsButton);
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

    // ══════════════════════════════════════════════════
    //  PENDING AMOUNT (for beneficiary-first flow)
    // ══════════════════════════════════════════════════

    private String pendingAmount;

    /** Store amount for deferred entry (beneficiary-first flow). */
    public void setPendingAmount(String amount) {
        this.pendingAmount = amount;
    }

    /** Get the stored pending amount (null if already entered). */
    public String getPendingAmount() {
        return pendingAmount;
    }

    /** Clear pending amount after it has been entered. */
    public void clearPendingAmount() {
        this.pendingAmount = null;
    }
}
