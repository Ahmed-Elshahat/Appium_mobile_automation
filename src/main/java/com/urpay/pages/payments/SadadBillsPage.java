package com.urpay.pages.payments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
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

    // Service Type Dropdown (Katalon AllServicesDropDownList = testID-multi-select-serviceType)
    @AndroidFindBy(accessibility = "testID-multi-select-serviceType")
    @iOSXCUITFindBy(accessibility = "testID-multi-select-serviceType")
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

    // Next Button (Katalon NextButton = testID-primary--main)
    @AndroidFindBy(accessibility = "testID-primary--main")
    @iOSXCUITFindBy(accessibility = "testID-primary--main")
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

    @Step("Select 'Mobily 005' provider from filtered results")
    public void selectMobilyProvider() {
        // After typing "Mobily", select by NAME (robust to result count) — the fixed
        // index item-4 only holds when the filter yields exactly 5 Mobily providers.
        By byName = AppiumBy.xpath(
                "//*[contains(@text,'Mobily 005') or contains(@content-desc,'Mobily 005')]"
                + "/ancestor-or-self::*[@clickable='true'][1]");
        try {
            waitUtils.waitForClickable(byName, 8).click();
            log.info("Selected Mobily 005 by name");
        } catch (Exception e) {
            waitUtils.waitForClickable(AppiumBy.accessibilityId("testID-search-item-4"), 6).click();
            log.info("Selected Mobily 005 via item-4 fallback");
        }
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

    @Step("Tap 'Save & Add Bill' button")
    public void tapSaveBillConfirm() {
        // The button testID varies across app versions:
        //   - testID-primary-onConfirmSave-main (older)
        //   - testID-primary-bSv-main (current)
        //   - fallback: any primary button with "Add Bill" text
        By confirm = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onConfirmSave-main' "
                + "or @content-desc='testID-primary-bSv-main' "
                + "or (contains(@content-desc,'testID-primary') and ancestor-or-self::*[contains(@text,'Add Bill')])]"
                + " | //*[@text='Add Bill' and @clickable='true']");
        try {
            waitUtils.waitForClickable(confirm, 6).click();
        } catch (Exception e) {
            swipeUtils.swipeUp();
            waitUtils.waitForClickable(confirm, 8).click();
        }
        log.info("Tapped 'Save & Add Bill'");
    }

    @Step("Tap 'Pay Later' button")
    public void tapPayLater() {
        By payLater = AppiumBy.xpath(
                "//*[@content-desc='testID-secondary-action-main' or @text='Pay Later']");
        try {
            waitUtils.waitForClickable(payLater, 6).click();
        } catch (Exception e) {
            swipeUtils.swipeUp();
            waitUtils.waitForClickable(payLater, 8).click();
        }
        log.info("Tapped 'Pay Later'");
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
        // testID varies: onPressCustomize-main (older) → hsY-main (current)
        By payBill = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onPressCustomize-main'"
                + " or @content-desc='testID-primary-hsY-main'"
                + " or @text='Pay Bill']");
        waitUtils.waitForClickable(payBill, 10).click();
    }

    @Step("Tap 'Confirm Pay' button")
    public void tapConfirmPay() {
        tap(confirmPayButton);
    }

    @Step("Tap Next on the bill-amount screen")
    public void tapNextBillAmount() {
        // Pay flow's Next (Katalon NextButtonBillAmountScreen) is a DIFFERENT button than
        // the add-bill Next (testID-primary--main).
        tap(AppiumBy.accessibilityId("testID-primary-onPressNext-main"));
    }

    @Step("Tap the final 'Pay Bill' step")
    public void tapPayBillFinalStep() {
        By finalStep = AppiumBy.xpath(
                "//*[@content-desc='testID-View.b8ce712a-4ccc-44c1-9a87-78a58b2036b6']");
        try {
            waitUtils.waitForClickable(finalStep, 6).click();
        } catch (Exception e) {
            swipeUtils.swipeUp();
            waitUtils.waitForClickable(finalStep, 8).click();
        }
        log.info("Tapped final Pay Bill step");
    }

    @Step("Tap 'Done' (scroll into view)")
    public void tapDoneWithScroll() {
        // Result screen: the Done button is below the fold and needs scrolling — Katalon
        // swiped twice before tapping. Retry with a swipe between attempts; match testID OR text.
        By done = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onPressDone-main' or @text='Done']");
        for (int i = 0; i < 4; i++) {
            try {
                waitUtils.waitForClickable(done, 3).click();
                log.info("Tapped Done");
                return;
            } catch (Exception e) {
                swipeUtils.swipeUp();
            }
        }
        waitUtils.waitForClickable(done, 6).click();
        log.info("Tapped Done (final attempt)");
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
        // The Apply button can sit below the keyboard/fold after editing the name —
        // scroll it into view and retry, matching testID OR the "Apply"/"Save" label.
        By apply = AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onPressEdit-main' "
                + "or @text='Apply' or @text='Save']");
        for (int i = 0; i < 3; i++) {
            try {
                waitUtils.waitForClickable(apply, 4).click();
                log.info("Tapped Apply Edit");
                return;
            } catch (Exception e) {
                swipeUtils.swipeUp();
            }
        }
        waitUtils.waitForClickable(apply, 6).click();
        log.info("Tapped Apply Edit (final attempt)");
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

    @Step("Search provider/service list for: {query}")
    public void searchProviderName(String query) {
        // Provider dropdown reuses the generic search input (testID-Search-Input,
        // placeholder "Search by name or provider"). Katalon: setText 'Mobily'.
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

    /** True when on the bills list OR on a bill's detail/pay screen (multi-select opens details). */
    public boolean isBillDetailsOrListVisible() {
        return isBillsListLoaded()
                || isPresent(io.appium.java_client.AppiumBy.xpath(
                        "//*[@text='Pay Bill' or @text='Bill Details' or @text='Bill details']"), 5);
    }

    public boolean isDoneButtonVisible() {
        return isDisplayed(doneButton, 15);
    }
}
