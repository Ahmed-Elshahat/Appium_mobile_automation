package com.urpay.pages.remittance;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Wallet Transfer page — peer-to-peer (URPay wallet → URPay wallet) money transfer.
 *
 * Locators extracted from Katalon Object Repository:
 *   Object Repository/android|ios/Remittance/WalletTransfer/
 *   (Android and iOS testIDs are identical for this module.)
 *
 * Screens covered:
 *   - Wallet Transfer entry (from Transfer landing)
 *   - Beneficiary search / "Transfer to unsaved number" option
 *   - Recipient mobile-number entry
 *   - Amount entry
 *   - Purpose / Relationship of transfer (radio list)
 *   - Confirmation
 *   - Done / Thank-you (success) screen
 *
 * Rules enforced:
 *   - All WebElement fields PRIVATE, exposed via @Step action methods only.
 *   - NO assertions — state queries return booleans for the test to assert.
 *   - NO waits/sleeps here — BasePage.tap/type delegate to WaitUtils.
 */
public class WalletTransferPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  ENTRY / BENEFICIARY SEARCH
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-viewElemenWalletTransfer")
    @iOSXCUITFindBy(accessibility = "testID-viewElemenWalletTransfer")
    private WebElement walletTransferButton;

    @AndroidFindBy(accessibility = "testID-Search-Input")
    @iOSXCUITFindBy(accessibility = "testID-Search-Input")
    private WebElement searchBeneficiaryField;

    @AndroidFindBy(accessibility = "testID-contacts-number-0")
    @iOSXCUITFindBy(accessibility = "testID-contacts-number-0")
    private WebElement firstBeneficiaryResult;

    @AndroidFindBy(xpath = "//*[@text='Transfer to unsaved number' "
            + "or starts-with(@text,'Transfer to unsaved')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label BEGINSWITH 'Transfer to unsaved number'")
    private WebElement transferToUnsavedNumberOption;

    // ══════════════════════════════════════════════════
    //  RECIPIENT NUMBER / AMOUNT
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-input-direct-undefined")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-undefined")
    private WebElement recipientNumberField;

    @AndroidFindBy(xpath = "//android.widget.EditText")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeTextField")
    private WebElement amountField;

    // ══════════════════════════════════════════════════
    //  PURPOSE / RELATIONSHIP (radio list)
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-radio-item-0")
    @iOSXCUITFindBy(accessibility = "testID-radio-item-0")
    private WebElement firstPurposeOption;

    // ══════════════════════════════════════════════════
    //  NAVIGATION / CONFIRMATION
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@text='Next']")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Next'")
    private WebElement nextButton;

    @AndroidFindBy(accessibility = "testID-primary-confirm-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-confirm-main")
    private WebElement confirmButton;

    @AndroidFindBy(accessibility = "testID-primary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement doneButton;

    @AndroidFindBy(accessibility = "testID-secondary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-secondary-action-main")
    private WebElement shareTransactionButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS — ENTRY / SEARCH
    // ══════════════════════════════════════════════════

    @Step("Tap 'Wallet Transfer' on the Transfer landing")
    public void tapWalletTransfer() {
        tap(walletTransferButton);
    }

    @Step("Tap beneficiary search field")
    public void tapSearch() {
        tap(searchBeneficiaryField);
    }

    @Step("Search for beneficiary: {term}")
    public void searchBeneficiary(String term) {
        type(searchBeneficiaryField, term);
    }

    @Step("Tap first beneficiary in the results")
    public void tapFirstBeneficiary() {
        tap(firstBeneficiaryResult);
    }

    @Step("Tap 'Transfer to unsaved number'")
    public void tapTransferToUnsavedNumber() {
        tap(transferToUnsavedNumberOption);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — RECIPIENT / AMOUNT
    // ══════════════════════════════════════════════════

    @Step("Enter recipient mobile number: {mobile}")
    public void enterRecipientNumber(String mobile) {
        type(recipientNumberField, mobile);
    }

    @Step("Enter transfer amount: {amount}")
    public void enterAmount(String amount) {
        type(amountField, amount);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — PURPOSE / NAVIGATION / CONFIRM
    // ══════════════════════════════════════════════════

    @Step("Select the first purpose/relationship option")
    public void selectFirstPurpose() {
        tap(firstPurposeOption);
    }

    @Step("Tap Next")
    public void tapNext() {
        tap(nextButton);
    }

    @Step("Tap Confirm")
    public void tapConfirm() {
        tap(confirmButton);
    }

    @Step("Tap Done on the success screen")
    public void tapDone() {
        tap(doneButton);
    }

    @Step("Tap Share Transaction")
    public void tapShareTransaction() {
        tap(shareTransactionButton);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES (no assertions — return booleans)
    // ══════════════════════════════════════════════════

    public boolean isSearchVisible(long timeoutSec) {
        return isDisplayed(searchBeneficiaryField, timeoutSec);
    }

    public boolean isBeneficiaryResultVisible(long timeoutSec) {
        return isDisplayed(firstBeneficiaryResult, timeoutSec);
    }

    public boolean isTransferToUnsavedNumberVisible(long timeoutSec) {
        return isDisplayed(transferToUnsavedNumberOption, timeoutSec);
    }

    public boolean isConfirmVisible(long timeoutSec) {
        return isDisplayed(confirmButton, timeoutSec);
    }

    public boolean isDoneVisible(long timeoutSec) {
        return isDisplayed(doneButton, timeoutSec);
    }
}
