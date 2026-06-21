package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Saddad Transaction Details page — verifies transaction history after bill payment.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/SadadBills/TransactionDetails/
 *
 * Used to verify:
 *   - Transaction title and subtitle in history list
 *   - Transaction type, category, reference number
 *   - VAT, fees, amount
 */
public class SadadTransactionDetailsPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  TRANSACTION LIST ITEMS
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@content-desc='testID-main-firstRow-0']")
    @iOSXCUITFindBy(accessibility = "testID-main-firstRow-0")
    private WebElement transactionMainTitle;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-second-secRow-0']")
    @iOSXCUITFindBy(accessibility = "testID-second-secRow-0")
    private WebElement transactionSubTitle;

    // ══════════════════════════════════════════════════
    //  TRANSACTION DETAIL VALUES
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-0']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-0")
    private WebElement transactionTypeValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-2']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-2")
    private WebElement referenceNumberValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-3']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-3")
    private WebElement transactionDateValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-4']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-4")
    private WebElement transactionAmountValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-5']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-5")
    private WebElement billingAccountValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-6']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-6")
    private WebElement billNameValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-7']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-7")
    private WebElement feesValue;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-label-value-main-8']")
    @iOSXCUITFindBy(accessibility = "testID-label-value-main-8")
    private WebElement vatValue;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap first transaction in history")
    public void tapFirstTransaction() {
        tap(transactionMainTitle);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES (no assertions — return values for test)
    // ══════════════════════════════════════════════════

    @Step("Get transaction main title text")
    public String getMainTitle() {
        return getText(transactionMainTitle);
    }

    @Step("Get transaction subtitle text")
    public String getSubTitle() {
        return getText(transactionSubTitle);
    }

    @Step("Get transaction type value")
    public String getTransactionType() {
        return getText(transactionTypeValue);
    }

    @Step("Get reference number value")
    public String getReferenceNumber() {
        return getText(referenceNumberValue);
    }

    @Step("Get transaction date value")
    public String getTransactionDate() {
        return getText(transactionDateValue);
    }

    @Step("Get transaction amount value")
    public String getTransactionAmount() {
        return getText(transactionAmountValue);
    }

    @Step("Get billing account value")
    public String getBillingAccount() {
        return getText(billingAccountValue);
    }

    @Step("Get bill name value")
    public String getBillName() {
        return getText(billNameValue);
    }

    @Step("Get fees value")
    public String getFeesValue() {
        return getText(feesValue);
    }

    @Step("Get VAT value")
    public String getVatValue() {
        return getText(vatValue);
    }

    public boolean isTransactionListLoaded() {
        return isDisplayed(transactionMainTitle, 15);
    }

    public boolean isTransactionDetailLoaded() {
        return isDisplayed(transactionTypeValue, 15);
    }
}
