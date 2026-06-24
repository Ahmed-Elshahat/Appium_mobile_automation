package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Account Statement page — handles the Account Statement screen
 * including date pickers, view statement button, and PDF verification.
 *
 * Katalon source: Object Repository/android/WalletVas/AccountStatement/
 *
 * Locators from Katalon Object Repository:
 *   fromDate.rs → testID-DatePicker.c9764948-a473-4993-bd46-68082a0a2ad2
 *   toDate.rs → testID-DatePicker.bddda331-7924-4207-99b5-2eebfaba7bd3
 *   viewAcctStatementBtn.rs → testID-primary--main
 *   pdfFile.rs → testID-Pdf.b69f9f42-f169-4c3b-80e3-b0c76994e13e
 *   previousMonth.rs → android:id/pickers NumberPicker button
 *   okBtn.rs → android:id/button1
 *   yearText.rs → android:id/date_picker_header_year
 */
public class AccountStatementPage extends BasePage {

    // ── Account Statement Header ──────────────────────
    @AndroidFindBy(xpath = "//*[@text='Account Statement']")
    @iOSXCUITFindBy(accessibility = "Account Statement")
    private WebElement accountStatementHeader;

    // ── From Date Picker ──────────────────────────────
    @AndroidFindBy(accessibility = "testID-DatePicker.c9764948-a473-4993-bd46-68082a0a2ad2")
    @iOSXCUITFindBy(accessibility = "testID-DatePicker.c9764948-a473-4993-bd46-68082a0a2ad2")
    private WebElement fromDatePicker;

    // ── To Date Picker ────────────────────────────────
    @AndroidFindBy(accessibility = "testID-DatePicker.bddda331-7924-4207-99b5-2eebfaba7bd3")
    @iOSXCUITFindBy(accessibility = "testID-DatePicker.bddda331-7924-4207-99b5-2eebfaba7bd3")
    private WebElement toDatePicker;

    // ── Date Dialog - Previous Month Button ───────────
    @AndroidFindBy(xpath = "//*[@resource-id='android:id/pickers']"
            + "/android.widget.NumberPicker[1]/android.widget.Button[1]")
    private WebElement previousMonthButton;

    // ── Date Dialog - OK Button ───────────────────────
    @AndroidFindBy(xpath = "//*[@resource-id='android:id/button1']")
    private WebElement dateDialogOkButton;

    // ── Date Dialog - Year Header ─────────────────────
    @AndroidFindBy(xpath = "//*[@resource-id='android:id/date_picker_header_year']")
    private WebElement yearHeaderText;

    // ── View Account Statement Button ─────────────────
    @AndroidFindBy(accessibility = "testID-primary--main")
    @iOSXCUITFindBy(accessibility = "testID-primary--main")
    private WebElement viewAccountStatementButton;

    // ── PDF Viewer (accessibility ID has dynamic UUID suffix, use UiAutomator partial match) ──
    @AndroidFindBy(uiAutomator = "new UiSelector().descriptionContains(\"testID-Pdf\")")
    @iOSXCUITFindBy(iOSNsPredicate = "name CONTAINS 'testID-Pdf'")
    private WebElement pdfViewer;

    // ── No Data / Empty State ──────────────────────
    @AndroidFindBy(uiAutomator = "new UiSelector().descriptionContains(\"testID-no-data\")")
    private WebElement noDataMessage;

    @AndroidFindBy(uiAutomator = "new UiSelector().textContains(\"No transactions\")")
    private WebElement noTransactionsText;

    // ── Back Button ───────────────────────────────────
    @AndroidFindBy(accessibility = "testID-right-icon-item")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-item")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  PAGE STATE QUERIES
    // ══════════════════════════════════════════════════

    @Step("Wait for Account Statement page to load")
    public void waitUntilLoaded() {
        waitUtils.waitForVisible(accountStatementHeader, 15);
    }

    public boolean isAccountStatementLoaded() {
        return isDisplayed(accountStatementHeader, 15);
    }

    @Step("Get Account Statement header text")
    public String getHeaderText() {
        return getText(accountStatementHeader);
    }

    // ══════════════════════════════════════════════════
    //  DATE PICKERS
    // ══════════════════════════════════════════════════

    @Step("Tap From Date picker")
    public void tapFromDate() {
        tap(fromDatePicker);
        waitUtils.waitForVisible(dateDialogOkButton, 10);
    }

    @Step("Tap To Date picker")
    public void tapToDate() {
        tap(toDatePicker);
        waitUtils.waitForVisible(dateDialogOkButton, 10);
    }

    @Step("Tap Previous Month in date picker dialog")
    public void tapPreviousMonth() {
        tap(previousMonthButton);
    }

    @Step("Tap OK button on date picker dialog")
    public void tapDateDialogOk() {
        tap(dateDialogOkButton);
    }

    @Step("Get year text from date picker header")
    public String getYearText() {
        return getText(yearHeaderText);
    }

    // ══════════════════════════════════════════════════
    //  VIEW STATEMENT
    // ══════════════════════════════════════════════════

    @Step("Tap 'View Account Statement' button")
    public void tapViewAccountStatement() {
        tap(viewAccountStatementButton);
    }

    // ══════════════════════════════════════════════════
    //  PDF VERIFICATION
    // ══════════════════════════════════════════════════

    public boolean isPdfVisible() {
        return isDisplayed(pdfViewer, 10);
    }

    /**
     * Check if a PDF element exists using dynamic partial match.
     * Useful when the accessibility ID contains a dynamic UUID.
     */
    public boolean isPdfVisibleByPartialMatch() {
        List<WebElement> elements = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"testID-Pdf\")"));
        return !elements.isEmpty();
    }

    /**
     * Check if a "no data" or "no transactions" message is visible.
     * This appears when the account has no transactions in the selected range.
     */
    public boolean isNoDataMessageVisible() {
        List<WebElement> noData = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().descriptionContains(\"testID-no-data\")"));
        if (!noData.isEmpty()) return true;

        List<WebElement> noTx = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"No transactions\")"));
        if (!noTx.isEmpty()) return true;

        // Also check for generic empty state or error toast
        List<WebElement> emptyState = driver.findElements(
                AppiumBy.androidUIAutomator("new UiSelector().textContains(\"No data\")"));
        return !emptyState.isEmpty();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Tap back button to return to previous screen")
    public void tapBack() {
        tap(backButton);
    }
}
