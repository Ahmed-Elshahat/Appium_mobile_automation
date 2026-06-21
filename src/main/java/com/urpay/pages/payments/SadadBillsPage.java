package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Saddad Bills page — covers bill listing, add/edit/delete/filter/pay operations.
 *
 * Locators extracted from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/SadadBills/
 *
 * Screens covered:
 *   - Bills List (All / Due Bills tabs)
 *   - Add New Bill wizard (service type → provider → bill type → number → amount → save)
 *   - Bill Payment (Pay Bill → OTP → confirmation)
 *   - Filter / Sort panel
 *   - Edit / Delete bill
 */
public class SadadBillsPage extends BasePage {

    // ══════════════════════════════════════════════════
    //  BILLS LIST SCREEN
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-tags-menu-0")
    @iOSXCUITFindBy(accessibility = "testID-tags-menu-0")
    private WebElement allBillsTab;

    @AndroidFindBy(accessibility = "testID-tags-menu-1")
    @iOSXCUITFindBy(accessibility = "testID-tags-menu-1")
    private WebElement dueBillsTab;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.Plus']")
    @iOSXCUITFindBy(accessibility = "testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.Plus")
    private WebElement addNewBillButton;

    @AndroidFindBy(accessibility = "testID-View.7c68583b-f554-4e2c-93e1-313ab86e2a68.0")
    @iOSXCUITFindBy(accessibility = "testID-View.7c68583b-f554-4e2c-93e1-313ab86e2a68.0")
    private WebElement firstBillButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.0']")
    @iOSXCUITFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.0")
    private WebElement firstBillMultiSelect;

    @AndroidFindBy(accessibility = "testID-left-icon-back")
    @iOSXCUITFindBy(accessibility = "testID-left-icon-back")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  ADD NEW BILL WIZARD
    // ══════════════════════════════════════════════════

    // Service Type Dropdown (Telecom & Internet, Government, etc.)
    @AndroidFindBy(xpath = "//*[@text='All Services' or @content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42']")
    @iOSXCUITFindBy(accessibility = "testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42")
    private WebElement allServicesDropDown;

    // Provider Dropdown
    @AndroidFindBy(accessibility = "testID-multi-select-provider")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-provider")
    private WebElement providerDropDown;

    // Bill Type Dropdown
    @AndroidFindBy(accessibility = "testID-multi-select-billType")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-billType")
    private WebElement billTypeDropDown;

    // Search items (dynamic from dropdowns)
    @AndroidFindBy(accessibility = "testID-search-item-0")
    @iOSXCUITFindBy(accessibility = "testID-search-item-0")
    private WebElement searchItem0;

    @AndroidFindBy(accessibility = "testID-search-item-1")
    @iOSXCUITFindBy(accessibility = "testID-search-item-1")
    private WebElement searchItem1;

    @AndroidFindBy(accessibility = "testID-search-item-4")
    @iOSXCUITFindBy(accessibility = "testID-search-item-4")
    private WebElement searchItem4;

    // Bill Number Input
    @AndroidFindBy(accessibility = "testID-input-direct-number")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-number")
    private WebElement billNumberField;

    // Bill Amount Input
    @AndroidFindBy(xpath = "//*[@content-desc='testID-TextInput.6ebddf51-6774-41c1-956f-5e9e51bc5847']")
    @iOSXCUITFindBy(accessibility = "testID-TextInput.6ebddf51-6774-41c1-956f-5e9e51bc5847")
    private WebElement billAmountField;

    // Bill Name Input (during save)
    @AndroidFindBy(accessibility = "testID-input-direct-alias")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-alias")
    private WebElement billNameField;

    // Next Button (Bill Amount Screen)
    @AndroidFindBy(accessibility = "testID-primary-onPressNext-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressNext-main")
    private WebElement nextButton;

    // Save Bill Switcher
    @AndroidFindBy(accessibility = "testID-switcher-isSaveFlag")
    @iOSXCUITFindBy(accessibility = "testID-switcher-isSaveFlag")
    private WebElement saveBillSwitcher;

    // Save Bill Confirm Button
    @AndroidFindBy(accessibility = "testID-primary-onConfirmSave-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirmSave-main")
    private WebElement saveBillConfirmButton;

    // Done Button (after payment)
    @AndroidFindBy(accessibility = "testID-primary-onPressDone-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressDone-main")
    private WebElement doneButton;

    // ══════════════════════════════════════════════════
    //  PAYMENT BUTTONS
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-primary-onPressCustomize-main")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressCustomize-main")
    private WebElement payBillButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onConfirm-main']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement confirmPayButton;

    // ══════════════════════════════════════════════════
    //  FILTER / SORT PANEL
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-right-icon-0")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-0")
    private WebElement filterSortButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-radio-item-0']")
    @iOSXCUITFindBy(accessibility = "testID-radio-item-0")
    private WebElement mostRecentFilter;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-radio-item-1']")
    @iOSXCUITFindBy(accessibility = "testID-radio-item-1")
    private WebElement amountLowFilter;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-radio-item-2']")
    @iOSXCUITFindBy(accessibility = "testID-radio-item-2")
    private WebElement amountHighFilter;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-Text.c06eec32-711a-4563-863f-c6157b3485bd.0']")
    @iOSXCUITFindBy(accessibility = "testID-Text.c06eec32-711a-4563-863f-c6157b3485bd.0")
    private WebElement telecomServiceTypeFilter;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onApplyFilters-main']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onApplyFilters-main")
    private WebElement applyFilterButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-onResetFilters-main']")
    @iOSXCUITFindBy(accessibility = "testID-secondary-onResetFilters-main")
    private WebElement resetFilterButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-Text.6bae8371-55ad-4ce9-8952-99065b147255']")
    @iOSXCUITFindBy(accessibility = "testID-Text.6bae8371-55ad-4ce9-8952-99065b147255")
    private WebElement sortByLabel;

    // ══════════════════════════════════════════════════
    //  EDIT / DELETE
    // ══════════════════════════════════════════════════

    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-onPressEdit-main']")
    @iOSXCUITFindBy(accessibility = "testID-secondary-onPressEdit-main")
    private WebElement editButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onPressEdit-main']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressEdit-main")
    private WebElement applyEditButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-TouchableOpacity.ae91a088-6b7a-4715-89a4-52cabcc15934']")
    @iOSXCUITFindBy(accessibility = "testID-TouchableOpacity.ae91a088-6b7a-4715-89a4-52cabcc15934")
    private WebElement deleteButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onPressDelete-main']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPressDelete-main")
    private WebElement confirmDeleteButton;

    // Edit Bill Name Field
    @AndroidFindBy(xpath = "//*[@content-desc='testID-input-direct-SadadN.EditBil']")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-SadadN.EditBil")
    private WebElement editBillNameField;

    // ══════════════════════════════════════════════════
    //  SEARCH FIELD (in Saddad page)
    // ══════════════════════════════════════════════════

    @AndroidFindBy(accessibility = "testID-Search-Input")
    @iOSXCUITFindBy(accessibility = "testID-Search-Input")
    private WebElement sadadSearchField;

    // ══════════════════════════════════════════════════
    //  ACTIONS — BILLS LIST
    // ══════════════════════════════════════════════════

    @Step("Tap 'All Bills' tab")
    public void tapAllBillsTab() {
        tap(allBillsTab);
    }

    @Step("Tap 'Due Bills' tab")
    public void tapDueBillsTab() {
        tap(dueBillsTab);
    }

    @Step("Tap 'Add New Bill' button")
    public void tapAddNewBill() {
        tap(addNewBillButton);
    }

    @Step("Tap first bill in the list")
    public void tapFirstBill() {
        tap(firstBillButton);
    }

    @Step("Select first bill for multi-payment")
    public void selectFirstBillMulti() {
        tap(firstBillMultiSelect);
    }

    @Step("Tap back button")
    public void tapBack() {
        tap(backButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — ADD NEW BILL WIZARD
    // ══════════════════════════════════════════════════

    @Step("Tap Service Type dropdown (All Services)")
    public void tapServiceTypeDropdown() {
        tap(allServicesDropDown);
    }

    @Step("Select 'Telecom and Internet' service type")
    public void selectTelecomAndInternet() {
        tap(searchItem1);
    }

    @Step("Tap Provider dropdown")
    public void tapProviderDropdown() {
        tap(providerDropDown);
    }

    @Step("Select first provider from search results (index 0)")
    public void selectFirstProvider() {
        tap(searchItem0);
    }

    @Step("Select Mobily 005 provider (search-item-4)")
    public void selectMobilyProvider() {
        tap(searchItem4);
    }

    @Step("Tap Bill Type dropdown")
    public void tapBillTypeDropdown() {
        tap(billTypeDropDown);
    }

    @Step("Select 'Prepaid' bill type (search-item-1)")
    public void selectPrepaid() {
        tap(searchItem1);
    }

    @Step("Select 'Postpaid' bill type (search-item-0)")
    public void selectPostpaid() {
        tap(searchItem0);
    }

    @Step("Select 'Overpaid' bill type (search-item-1)")
    public void selectOverpaid() {
        tap(searchItem1);
    }

    @Step("Enter bill number: {billNumber}")
    public void enterBillNumber(String billNumber) {
        type(billNumberField, billNumber);
    }

    @Step("Enter bill amount: {amount}")
    public void enterBillAmount(String amount) {
        tap(billAmountField);
        type(billAmountField, amount);
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(nextButton);
    }

    @Step("Toggle Save Bill switcher")
    public void toggleSaveBill() {
        tap(saveBillSwitcher);
    }

    @Step("Enter bill name: {name}")
    public void enterBillName(String name) {
        type(billNameField, name);
    }

    @Step("Tap Save Bill Confirm button")
    public void tapSaveBillConfirm() {
        tap(saveBillConfirmButton);
    }

    @Step("Tap Done button")
    public void tapDone() {
        tap(doneButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — PAYMENT
    // ══════════════════════════════════════════════════

    @Step("Tap 'Pay Bill' button")
    public void tapPayBill() {
        tap(payBillButton);
    }

    @Step("Tap 'Confirm Pay' button")
    public void tapConfirmPay() {
        tap(confirmPayButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — FILTER / SORT
    // ══════════════════════════════════════════════════

    @Step("Tap Filter/Sort button")
    public void tapFilterSort() {
        tap(filterSortButton);
    }

    @Step("Select 'Most Recent' filter")
    public void selectMostRecentFilter() {
        tap(mostRecentFilter);
    }

    @Step("Select 'Amount: Low Price' filter")
    public void selectAmountLowFilter() {
        tap(amountLowFilter);
    }

    @Step("Select 'Amount: High Price' filter")
    public void selectAmountHighFilter() {
        tap(amountHighFilter);
    }

    @Step("Select 'Telecom' service type filter")
    public void selectTelecomServiceTypeFilter() {
        tap(telecomServiceTypeFilter);
    }

    @Step("Tap Apply Filter button")
    public void tapApplyFilter() {
        tap(applyFilterButton);
    }

    @Step("Tap Reset Filter button")
    public void tapResetFilter() {
        tap(resetFilterButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — EDIT / DELETE
    // ══════════════════════════════════════════════════

    @Step("Tap Edit button")
    public void tapEdit() {
        tap(editButton);
    }

    @Step("Clear and enter new bill name: {name}")
    public void editBillName(String name) {
        editBillNameField.clear();
        type(editBillNameField, name);
    }

    @Step("Tap Apply Edit button")
    public void tapApplyEdit() {
        tap(applyEditButton);
    }

    @Step("Tap Delete button")
    public void tapDelete() {
        tap(deleteButton);
    }

    @Step("Tap Confirm Delete button")
    public void tapConfirmDelete() {
        tap(confirmDeleteButton);
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS — SEARCH
    // ══════════════════════════════════════════════════

    @Step("Search bills for: {query}")
    public void searchBills(String query) {
        type(sadadSearchField, query);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES (no assertions — return values)
    // ══════════════════════════════════════════════════

    public boolean isBillsListLoaded() {
        return isDisplayed(allBillsTab, 5)
                || isPresent(
                        io.appium.java_client.AppiumBy.xpath(
                                "//*[@text='My bills' or @text='New Bill']"), 5);
    }

    public boolean isAddBillWizardLoaded() {
        return isDisplayed(allServicesDropDown, 10)
                || isDisplayed(providerDropDown, 5);
    }

    public boolean isFilterPanelVisible() {
        return isPresent(
                io.appium.java_client.AppiumBy.accessibilityId("testID-radio-item-0"), 5);
    }

    public boolean isSortByLabelVisible() {
        return isDisplayed(sortByLabel, 5);
    }

    public boolean isFirstBillVisible() {
        return isDisplayed(firstBillButton, 10);
    }

    public boolean isDoneButtonVisible() {
        return isDisplayed(doneButton, 15);
    }
}
