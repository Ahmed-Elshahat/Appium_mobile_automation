package com.urpay.pages.wallet;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Account Details page — handles the wallet Account Details screen
 * including QR code, bank info, IBAN, balance, cashback, hold amounts,
 * and account statement navigation.
 *
 * Katalon source: Object Repository/android/WalletVas/AccountDetails/
 *
 * Locators from Katalon Object Repository:
 *   accountDetailsHeader.rs → testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42 (text="Account Details")
 *   qrCode.rs → testID-QRCodeStyled.b4c3742c-ceb6-4f37-a1b8-829879143599
 *   qrCodeText.rs → testID-Text.e13d0a69-e278-43c3-a7a6-7ca71660e6bb
 *   bankNameText.rs → testID-Text.253f691a-7a13-4d3a-83b1-f1ff4b51a01d
 *   IBANText.rs → testID-Text.6304f374-ab9f-4f92-b8b9-0c1a16868c36
 *   copyIBANNumber.rs → testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.Copy
 *   availableBalance.rs → testID-Text.e4d72876-3314-439d-9c59-82b289f56237
 *   cashbackBefore.rs → xpath with testID-amount-main / testID-master-amount-main
 *   viewAllCashbackDetailsBtn.rs → text "View All"
 *   cashbackAfter.rs → testID-Text.9eaf1112-dbe4-4da3-96df-39a4262d6c53
 *   CashbackScreenHeaderText.rs → testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42
 *   viewAllIncomingHoldAmountsBtn.rs → (testID-secondary--main)[2]
 *   holdBalanceText.rs → testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42
 *   accountStatementBtn.rs → testID-primary-action-main
 *   footerText.rs → testID-Text.6ad66c61-1e84-4003-9292-b71d66bc9aee
 *   shareBtn.rs → testID-right-icon-0
 *   backBtnToDashboardScreen.rs → testID-right-icon-item
 */
public class AccountDetailsPage extends BasePage {

    // ── Account Details Header ────────────────────────

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Account Details']")
    @iOSXCUITFindBy(accessibility = "Account Details")
    private WebElement accountDetailsHeader;

    // ── QR Code Section ───────────────────────────────

    @AndroidFindBy(accessibility = "testID-QRCodeStyled.b4c3742c-ceb6-4f37-a1b8-829879143599")
    @iOSXCUITFindBy(accessibility = "testID-QRCodeStyled.b4c3742c-ceb6-4f37-a1b8-829879143599")
    private WebElement qrCode;

    @AndroidFindBy(accessibility = "testID-Text.e13d0a69-e278-43c3-a7a6-7ca71660e6bb")
    @iOSXCUITFindBy(accessibility = "testID-Text.e13d0a69-e278-43c3-a7a6-7ca71660e6bb")
    private WebElement qrCodeText;

    // ── Bank Info Section ─────────────────────────────

    @AndroidFindBy(accessibility = "testID-Text.253f691a-7a13-4d3a-83b1-f1ff4b51a01d")
    @iOSXCUITFindBy(accessibility = "testID-Text.253f691a-7a13-4d3a-83b1-f1ff4b51a01d")
    private WebElement bankNameText;

    @AndroidFindBy(accessibility = "testID-Text.6304f374-ab9f-4f92-b8b9-0c1a16868c36")
    @iOSXCUITFindBy(accessibility = "testID-Text.6304f374-ab9f-4f92-b8b9-0c1a16868c36")
    private WebElement ibanText;

    @AndroidFindBy(accessibility = "testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.Copy")
    @iOSXCUITFindBy(accessibility = "testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.Copy")
    private WebElement copyIbanButton;

    // ── Balance Section ───────────────────────────────

    @AndroidFindBy(xpath = "(//android.widget.TextView[@content-desc='testID-master-amount-main'])[1]")
    @iOSXCUITFindBy(xpath = "(//XCUIElementTypeStaticText[@name='testID-master-amount-main'])[1]")
    private WebElement availableBalance;

    // ── Cashback Section ──────────────────────────────

    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text, 'Total cashback')]"
            + "/following-sibling::android.view.ViewGroup[@content-desc='testID-amount-main']"
            + "//android.widget.TextView[@content-desc='testID-master-amount-main']")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[contains(@name, 'Total cashback')]"
            + "/following-sibling::XCUIElementTypeOther[@name='testID-amount-main']"
            + "//XCUIElementTypeStaticText[@name='testID-master-amount-main']")
    private WebElement cashbackAmountBefore;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='View All']")
    @iOSXCUITFindBy(accessibility = "View All")
    private WebElement viewAllCashbackButton;

    @AndroidFindBy(accessibility = "testID-Text.9eaf1112-dbe4-4da3-96df-39a4262d6c53")
    @iOSXCUITFindBy(accessibility = "testID-Text.9eaf1112-dbe4-4da3-96df-39a4262d6c53")
    private WebElement cashbackAmountAfter;

    // ── Hold Balance Section ──────────────────────────

    @AndroidFindBy(xpath = "(//*[@content-desc='testID-secondary--main'])[2]")
    @iOSXCUITFindBy(xpath = "(//XCUIElementTypeOther[@name='testID-secondary--main'])[2]")
    private WebElement viewAllHoldAmountsButton;

    // ── Account Statement ─────────────────────────────

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Account Statement']"
            + "/ancestor::android.view.ViewGroup[@content-desc='testID-primary-action-main']")
    @iOSXCUITFindBy(xpath = "//XCUIElementTypeStaticText[@name='Account Statement']"
            + "/ancestor::XCUIElementTypeOther[@name='testID-primary-action-main']")
    private WebElement accountStatementButton;

    // ── Footer ────────────────────────────────────────

    @AndroidFindBy(accessibility = "testID-Text.6ad66c61-1e84-4003-9292-b71d66bc9aee")
    @iOSXCUITFindBy(accessibility = "testID-Text.6ad66c61-1e84-4003-9292-b71d66bc9aee")
    private WebElement footerText;

    // ── Navigation Buttons ────────────────────────────

    @AndroidFindBy(accessibility = "testID-right-icon-0")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-0")
    private WebElement shareButton;

    @AndroidFindBy(accessibility = "testID-right-icon-item")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-item")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  PAGE STATE QUERIES (no assertions — tests decide)
    // ══════════════════════════════════════════════════

    public boolean isAccountDetailsLoaded() {
        return isDisplayed(accountDetailsHeader, 15);
    }

    public boolean isQrCodeVisible() {
        return isDisplayed(qrCode, 10);
    }

    public boolean isShareButtonVisible() {
        return isDisplayed(shareButton, 10);
    }

    // ══════════════════════════════════════════════════
    //  QR CODE SECTION
    // ══════════════════════════════════════════════════

    @Step("Get QR code text description")
    public String getQrCodeText() {
        return getText(qrCodeText);
    }

    // ══════════════════════════════════════════════════
    //  BANK INFO SECTION
    // ══════════════════════════════════════════════════

    @Step("Get bank name text")
    public String getBankName() {
        return getText(bankNameText);
    }

    @Step("Get IBAN text")
    public String getIbanText() {
        return getText(ibanText);
    }

    @Step("Tap Copy IBAN button")
    public void tapCopyIban() {
        tap(copyIbanButton);
    }

    // ══════════════════════════════════════════════════
    //  BALANCE SECTION
    // ══════════════════════════════════════════════════

    @Step("Get available balance text")
    public String getAvailableBalance() {
        return getText(availableBalance);
    }

    // ══════════════════════════════════════════════════
    //  CASHBACK SECTION
    // ══════════════════════════════════════════════════

    @Step("Get cashback amount from Account Details page")
    public String getCashbackAmountBefore() {
        return getText(cashbackAmountBefore);
    }

    @Step("Tap 'View All' Cashback Details button")
    public void tapViewAllCashbackDetails() {
        tap(viewAllCashbackButton);
        // Wait for Cashback Details screen to load
        waitUtils.waitForVisible(
                AppiumBy.accessibilityId("testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42"), 10);
    }

    @Step("Get cashback amount from Cashback Details screen")
    public String getCashbackAmountAfter() {
        return getText(cashbackAmountAfter);
    }

    @Step("Get Cashback screen header text")
    public String getCashbackScreenHeader() {
        return getText(AppiumBy.accessibilityId("testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42"));
    }

    // ══════════════════════════════════════════════════
    //  HOLD BALANCE SECTION
    // ══════════════════════════════════════════════════

    @Step("Tap 'View All Incoming Hold Amounts' button")
    public void tapViewAllHoldAmounts() {
        tap(viewAllHoldAmountsButton);
        // Wait for Hold Balance screen to load
        waitUtils.waitForVisible(
                AppiumBy.accessibilityId("testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42"), 10);
    }

    @Step("Get Hold Balance screen header text")
    public String getHoldBalanceHeader() {
        return getText(AppiumBy.accessibilityId("testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42"));
    }

    // ══════════════════════════════════════════════════
    //  ACCOUNT STATEMENT
    // ══════════════════════════════════════════════════

    @Step("Scroll to and tap Account Statement button")
    public void tapAccountStatement() {
        swipeUp();
        tap(accountStatementButton);
        // Wait for Account Statement screen to load
        waitUtils.waitForVisible(
                AppiumBy.accessibilityId("testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42"), 10);
    }

    @Step("Get Account Statement screen header text")
    public String getAccountStatementHeader() {
        return getText(AppiumBy.accessibilityId("testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42"));
    }

    // ══════════════════════════════════════════════════
    //  FOOTER
    // ══════════════════════════════════════════════════

    @Step("Scroll down and get footer text")
    public String getFooterText() {
        swipeUp();
        return getText(footerText);
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Tap back button to return to previous screen")
    public void tapBack() {
        tap(backButton);
    }

    @Step("Tap share button")
    public void tapShare() {
        tap(shareButton);
    }
}
