package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Transaction Details page — type, category, reference, date, fees, VAT.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/TransactionDetailsPageObjects/
 */
public class TransactionDetailsPage extends BasePage {

    // TransactionDetailsPageObjects/typeValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-0")
    private WebElement typeValue;

    // TransactionDetailsPageObjects/CategoryValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-1")
    private WebElement categoryValue;

    // TransactionDetailsPageObjects/ReferenceNumberValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-2")
    private WebElement referenceNumber;

    // TransactionDetailsPageObjects/transactionDateValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-3")
    private WebElement transactionDate;

    // TransactionDetailsPageObjects/cardTypeValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-4")
    private WebElement cardType;

    // TransactionDetailsPageObjects/cardEndedWithValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-5")
    private WebElement cardEndedWith;

    // TransactionDetailsPageObjects/TtransactionfeesValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-6")
    private WebElement transactionFees;

    // TransactionDetailsPageObjects/vatValue.rs
    @AndroidFindBy(accessibility = "testID-label-value-main-7")
    private WebElement vatValue;

    // TransactionDetailsPageObjects/backBtnFromTransactionDetails.rs
    // Header back arrow — accept the item id, the generic in-app back id, or the top-left icon.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-left-icon-item'"
            + " or @content-desc='testID-left-icon-back'"
            + " or @content-desc='testID-left-icon-0'"
            + " or @content-desc='testID-header-back']")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap back from transaction details")
    public void tapBack() {
        tap(backButton);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public String getType() {
        return getText(typeValue);
    }

    public String getCategory() {
        return getText(categoryValue);
    }

    public String getReferenceNumber() {
        return getText(referenceNumber);
    }

    public String getTransactionDate() {
        return getText(transactionDate);
    }

    public String getCardType() {
        return getText(cardType);
    }

    public String getCardEndedWith() {
        return getText(cardEndedWith);
    }

    public String getTransactionFees() {
        return getText(transactionFees);
    }

    public String getVat() {
        return getText(vatValue);
    }

    public boolean isLoaded() {
        return isDisplayed(typeValue, 10);
    }
}
