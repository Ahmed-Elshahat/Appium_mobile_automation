package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Government Services page — covers MOI Traffic Violations (Sadad MOI).
 *
 * Locators extracted from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/GovernmentServices/
 *
 * Screens covered:
 *   - Service Category selection (Traffic Violations)
 *   - Service Type selection (Query Violations by the violator ID)
 *   - Violator ID entry
 *   - Violations list (single / multi-select)
 *   - Payment confirmation
 *   - Done / Success screen
 */
public class GovernmentServicesPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  SERVICE SELECTION
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-multi-select-category")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-category")
    private WebElement serviceCategoryDropdown;

    @AndroidFindBy(accessibility = "testID-multi-select-service")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-service")
    private WebElement serviceTypeDropdown;

    @AndroidFindBy(xpath = "//*[@text='Traffic Violations']")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Traffic Violations'")
    private WebElement trafficViolationsOption;

    @AndroidFindBy(xpath = "//*[@text='Query Violations by the violator ID']")
    @iOSXCUITFindBy(iOSNsPredicate = "label == 'Query Violations by the violator ID'")
    private WebElement violationsByViolatorIdOption;

    // ══════════════════════════════════════════════════
    //  VIOLATOR ID ENTRY
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-input-direct-114")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-114")
    private WebElement violatorIdField;

    @AndroidFindBy(accessibility = "testID-primary--main")
    @iOSXCUITFindBy(accessibility = "testID-primary--main")
    private WebElement nextButton;

    // ══════════════════════════════════════════════════
    //  VIOLATIONS LIST
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0")
    @iOSXCUITFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0")
    private WebElement firstViolation;

    @AndroidFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.1")
    @iOSXCUITFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.1")
    private WebElement secondViolation;

    @AndroidFindBy(accessibility = "testID-TouchableOpacity.736923c6-2764-4ecb-b11a-9b815d040889")
    @iOSXCUITFindBy(accessibility = "testID-TouchableOpacity.736923c6-2764-4ecb-b11a-9b815d040889")
    private WebElement selectAllViolationsButton;

    @AndroidFindBy(xpath = "//*[contains(@text,'violations selected')]")
    @iOSXCUITFindBy(iOSNsPredicate = "label CONTAINS 'violations selected'")
    private WebElement paySelectedButton;

    // ══════════════════════════════════════════════════
    //  PAYMENT CONFIRMATION
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onConfirm-main' or @text='Confirm']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement confirmButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onPressDone-main' or @text='Done']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressDone-main")
    private WebElement doneButton;

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-left-icon-back")
    @iOSXCUITFindBy(accessibility = "testID-left-icon-back")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS — SERVICE SELECTION
    // ══════════════════════════════════════════════════

    @Step("Tap Service Category dropdown")
    public void tapServiceCategory() {
        tap(serviceCategoryDropdown);
    }

    @Step("Select 'Traffic Violations' category")
    public void selectTrafficViolations() {
        tap(trafficViolationsOption);
    }

    @Step("Tap Service Type dropdown")
    public void tapServiceType() {
        tap(serviceTypeDropdown);
    }

    @Step("Select 'Query Violations by the violator ID'")
    public void selectViolationsByViolatorId() {
        tap(violationsByViolatorIdOption);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — VIOLATOR ID
    // ══════════════════════════════════════════════════

    @Step("Enter violator ID: {violatorId}")
    public void enterViolatorId(String violatorId) {
        type(violatorIdField, violatorId);
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(nextButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — VIOLATIONS LIST
    // ══════════════════════════════════════════════════

    @Step("Tap first violation in the list")
    public void tapFirstViolation() {
        tap(firstViolation);
    }

    @Step("Tap second violation in the list")
    public void tapSecondViolation() {
        tap(secondViolation);
    }

    @Step("Tap 'Select All' violations button")
    public void tapSelectAll() {
        tap(selectAllViolationsButton);
    }

    @Step("Tap 'Pay Selected' button")
    public void tapPaySelected() {
        tap(paySelectedButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — PAYMENT
    // ══════════════════════════════════════════════════

    @Step("Tap Confirm button")
    public void tapConfirm() {
        tap(confirmButton);
    }

    @Step("Tap Done button")
    public void tapDone() {
        tap(doneButton);
    }

    @Step("Tap back button")
    public void tapBack() {
        tap(backButton);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES (no assertions — returns boolean)
    // ══════════════════════════════════════════════════

    public boolean isServiceCategoryVisible(long timeoutSec) {
        return isDisplayed(serviceCategoryDropdown, timeoutSec);
    }

    public boolean isViolationsListLoaded(long timeoutSec) {
        return isDisplayed(firstViolation, timeoutSec);
    }

    public boolean isConfirmButtonVisible(long timeoutSec) {
        return isDisplayed(confirmButton, timeoutSec);
    }

    public boolean isDoneButtonVisible(long timeoutSec) {
        return isDisplayed(doneButton, timeoutSec);
    }

    public boolean isSecondViolationVisible(long timeoutSec) {
        return isDisplayed(secondViolation, timeoutSec);
    }
}
