package com.urpay.pages.payments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Top Up page — covers the bank card top-up wizard screens:
 *   - Top-up method selection (Bank Card / Apple Pay)
 *   - Add new card / Select existing card
 *   - Card details form (manual entry)
 *   - Amount entry
 *   - 3D Secure OTP (bank WebView)
 *   - Success / Done
 *
 * Katalon source:
 *   Object Repository/{platform}/PaymentAndCards/TopUpUsingBankCard/
 *
 * Migrated from:
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/TopUpUsingBankCardForUserDoesnotHaveCards
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/TopUpUsingExistingCard
 */
public class TopUpPage extends BasePage {

    // ── Top-Up Method Selection ───────────────────────
    @AndroidFindBy(accessibility = "testID-data-0")
    @iOSXCUITFindBy(accessibility = "testID-data-0")
    private WebElement bankCardButton;

    // ── Card Selection ────────────────────────────────
    // Cloud build HASHES the middle segment of testID-<primary|secondary>-<action>-main,
    // so match the exact testID, the visible 'Add new card' text (avoids the solid-blue
    // 'Next' button), OR a structural testID-secondary-…-main net as a last resort.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onAddNewCard-main'"
            + " or @content-desc='testID-secondary-onAddNewCard-main'"
            + " or @text='Add new card' or @text='Add New Card' or @text='Add Card']")
    @iOSXCUITFindBy(xpath = "//*[@name='testID-primary-onAddNewCard-main'"
            + " or @name='testID-secondary-onAddNewCard-main'"
            + " or @label='Add new card' or @label='Add New Card' or @label='Add Card']")
    private WebElement addNewCardButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onSubmit-main'"
            + " or @text='Next'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    @iOSXCUITFindBy(accessibility = "testID-primary-onSubmit-main")
    private WebElement nextExistingCardButton;

    // ── Camera / Manual Entry ─────────────────────────
    // Cloud build hashes the middle segment → match exact testID, the visible
    // 'Enter details manually' link, OR a structural testID-secondary-…-main net.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-navigationAction-main'"
            + " or contains(@text,'details manually') or contains(@text,'Details Manually')"
            + " or (starts-with(@content-desc,'testID-secondary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    @iOSXCUITFindBy(accessibility = "testID-secondary-navigationAction-main")
    private WebElement enterDetailsManuallyButton;

    // ── Card Details Form ─────────────────────────────
    @AndroidFindBy(accessibility = "testID-input-direct-cardHolderName")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-cardHolderName")
    private WebElement cardHolderNameField;

    @AndroidFindBy(accessibility = "testID-input-direct-cardNumber")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-cardNumber")
    private WebElement cardNumberField;

    @AndroidFindBy(accessibility = "testID-input-direct-expiry")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-expiry")
    private WebElement expiryDateField;

    @AndroidFindBy(accessibility = "testID-check-box-main")
    @iOSXCUITFindBy(accessibility = "testID-check-box-main")
    private WebElement saveCardCheckBox;

    @AndroidFindBy(accessibility = "testID-input-direct-cardAlias")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-cardAlias")
    private WebElement nickNameField;

    // ── Amount Entry ──────────────────────────────────
    // Dynamic testID — fallback to EditText for amount input
    private static final By AMOUNT_INPUT = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-TextInput.') and @class='android.widget.EditText']"
            + " | //android.widget.EditText");

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-validateAmount-main'"
            + " or @text='Next'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    @iOSXCUITFindBy(accessibility = "testID-primary-validateAmount-main")
    private WebElement validateAmountButton;

    // ── Generic Primary Action Button (Next / Confirm / Done) ──
    // Cloud build hashes the middle segment of testID-primary-<action>-main →
    // match visible text OR any structural testID-primary-…-main (one per wizard screen).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary--main'"
            + " or @text='Next' or @text='Confirm' or @text='Done' or @text='Add'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')]")
    @iOSXCUITFindBy(accessibility = "testID-primary--main")
    private WebElement primaryButton;

    // ── 3D Secure WebView (bank OTP page) ─────────────
    @AndroidFindBy(xpath = "//android.widget.EditText[@resource-id='otp']")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeTextField[@name='otp']")
    private WebElement secureOtpField;

    @AndroidFindBy(xpath = "//*[@text='submit' or @text='Submit']")
    @iOSXCUITFindBy(xpath = "//*[@label='submit' or @label='Submit']")
    private WebElement submitOtpButton;

    // ── Transactions Tab (post-topup verification) ────
    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'Transactions')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Transactions'")
    private WebElement transactionsTab;

    @AndroidFindBy(accessibility = "testID-main-firstRow-0")
    @iOSXCUITFindBy(accessibility = "testID-main-firstRow-0")
    private WebElement latestTransaction;

    // ══════════════════════════════════════════════════
    //  TOP-UP METHOD SELECTION
    // ══════════════════════════════════════════════════

    @Step("Tap Bank Card button")
    public void tapBankCard() {
        tap(bankCardButton);
    }

    // ══════════════════════════════════════════════════
    //  CARD SELECTION
    // ══════════════════════════════════════════════════

    @Step("Tap Add New Card button")
    public void tapAddNewCard() {
        tap(addNewCardButton);
    }

    @Step("Tap Next for existing card")
    public void tapNextExistingCard() {
        tap(nextExistingCardButton);
    }

    @Step("Tap Enter Details Manually")
    public void tapEnterDetailsManually() {
        tap(enterDetailsManuallyButton);
    }

    // ══════════════════════════════════════════════════
    //  CARD DETAILS FORM
    // ══════════════════════════════════════════════════

    @Step("Enter card holder name: {name}")
    public void enterCardHolderName(String name) {
        type(cardHolderNameField, name);
    }

    @Step("Enter card number: {number}")
    public void enterCardNumber(String number) {
        type(cardNumberField, number);
    }

    @Step("Enter expiry date: {month}/{year}")
    public void enterExpiry(String month, String year) {
        waitUtils.waitForClickable(expiryDateField).click();
        expiryDateField.sendKeys(month);
        expiryDateField.sendKeys(year);
    }

    @Step("Tap Save Card checkbox")
    public void tapSaveCardCheckBox() {
        tap(saveCardCheckBox);
    }

    @Step("Enter card nickname: {nickname}")
    public void enterNickName(String nickname) {
        type(nickNameField, nickname);
    }

    // ══════════════════════════════════════════════════
    //  AMOUNT ENTRY
    // ══════════════════════════════════════════════════

    @Step("Wait for amount entry screen to load")
    public void waitForAmountScreen(long timeoutSec) {
        waitUtils.waitForVisible(AMOUNT_INPUT, timeoutSec);
    }

    @Step("Wait for amount entry screen to dismiss")
    public void waitForAmountScreenDismissed(long timeoutSec) {
        waitUtils.waitForInvisible(AMOUNT_INPUT, timeoutSec);
    }

    @Step("Enter top-up amount: {amount}")
    public void enterAmount(String amount) {
        type(AMOUNT_INPUT, amount);
    }

    @Step("Tap Next to validate amount")
    public void tapValidateAmount() {
        tap(validateAmountButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTION BUTTONS
    // ══════════════════════════════════════════════════

    @Step("Tap primary action button (Next/Confirm/Done)")
    public void tapPrimaryButton() {
        tap(primaryButton);
    }

    @Step("Tap Done button with extended timeout")
    public void tapDone(long timeoutSec) {
        waitUtils.waitForClickable(primaryButton, timeoutSec).click();
    }

    // ══════════════════════════════════════════════════
    //  3D SECURE OTP
    // ══════════════════════════════════════════════════

    @Step("Enter 3D Secure OTP: {otp}")
    public void enter3dsOtp(String otp) {
        waitUtils.waitForClickable(secureOtpField, 30);
        secureOtpField.click();
        secureOtpField.sendKeys(otp);
    }

    @Step("Tap Submit OTP button")
    public void tapSubmitOtp() {
        tap(submitOtpButton);
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Transactions tab")
    public void tapTransactionsTab() {
        tap(transactionsTab);
    }

    @Step("Tap latest transaction")
    public void tapLatestTransaction() {
        tap(latestTransaction);
    }

    public String getLatestTransactionTitle() {
        return getText(latestTransaction);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public boolean isBankCardButtonVisible(long timeoutSec) {
        return isDisplayed(bankCardButton, timeoutSec);
    }

    public boolean isAddNewCardVisible(long timeoutSec) {
        return isDisplayed(addNewCardButton, timeoutSec);
    }

    public boolean isExistingCardVisible(long timeoutSec) {
        return isDisplayed(nextExistingCardButton, timeoutSec);
    }

    public boolean isPrimaryButtonVisible(long timeoutSec) {
        return isDisplayed(primaryButton, timeoutSec);
    }

    public boolean isOtpFieldVisible(long timeoutSec) {
        return isDisplayed(secureOtpField, timeoutSec);
    }
}
