package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.pages.auth.OtpPage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Direct Top-up — parent adds money directly to a kid's wallet from the kid profile screen.
 *
 * <p>Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/DirectTopup-Positive/Negative Scenarios and the Family Wallet Direct
 *   Topup / Unhappy suites ({@code Wallet_VAS/FamilyWallet/Direct-Topup/*}).
 *
 * <p>Navigation to the family wallet and opening the kid profile is handled by
 * {@link FamilyWalletPage}; this page owns the Add-Money wizard and the balance / error reads.
 */
public class DirectTopupPage extends BasePage {

    private static final By KID_BALANCE =
            AppiumBy.accessibilityId("testID-master-amount-index_0");
    // The LambdaTest cloud build hashes the <action> segment of testID-primary-<action>-main
    // buttons (prefix/suffix preserved), so each primary button keeps its exact testID for local
    // builds and adds a visible-label + structural (any preserved testID-primary-…-main) fallback
    // for the remote build. Mirrors the proven AutoTopupPage hardening.
    private static final By ADD_MONEY_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-addMoneyAction-main' or @text='Add money' or "
            + "(starts-with(@content-desc,'testID-primary-') and "
            + "substring(@content-desc, string-length(@content-desc) - 4) = '-main')]");
    private static final By ADD_AMOUNT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.99d56835-0082-495d-8e6a-d3f74489f518']");
    private static final By NEXT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-action-main' or @text='Next' or "
            + "(starts-with(@content-desc,'testID-primary-') and "
            + "substring(@content-desc, string-length(@content-desc) - 4) = '-main')]");
    private static final By CONFIRM_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-generateOtp-main' or @text='Confirm' or "
            + "(starts-with(@content-desc,'testID-primary-') and "
            + "substring(@content-desc, string-length(@content-desc) - 4) = '-main')]");
    private static final By THANK_YOU_TEXT =
            AppiumBy.accessibilityId("testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5");
    private static final By DONE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-onSubmit-main' or @text='Done' or "
            + "(starts-with(@content-desc,'testID-primary-') and "
            + "substring(@content-desc, string-length(@content-desc) - 4) = '-main')]");
    private static final By NOTIFICATION_MSG =
            AppiumBy.accessibilityId("testID-notification-message");
    private static final By EXIT_BTN =
            AppiumBy.accessibilityId("testID-right-icon-0");

    @Step("Read the kid wallet balance")
    public String getKidBalance() {
        return getText(KID_BALANCE);
    }

    @Step("Enter add-money amount {amount} and tap Next")
    public void enterAmountAndNext(String amount) {
        tap(ADD_MONEY_BTN);
        type(ADD_AMOUNT_INPUT, amount);
        platformActions.dismissKeyboard();
        tap(NEXT_BTN);
    }

    @Step("Add {amount} to the kid wallet, confirm and enter the OTP")
    public void addMoneyAndConfirm(String amount, String otp) {
        enterAmountAndNext(amount);
        tap(CONFIRM_BTN);
        // Confirm (generateOtp) sends a verification code to the parent; enter it to reach the
        // "Thank You" success screen. Gated so a build that skips the OTP step still proceeds.
        OtpPage otpPage = new OtpPage();
        if (otpPage.isVisible(20)) {
            otpPage.enterOtp(otp);
        }
    }

    /** True once the "Thank You" success screen has rendered. */
    public boolean isThankYouShown() {
        return isPresent(THANK_YOU_TEXT, 30);
    }

    @Step("Tap Done on the success screen")
    public void tapDone() {
        tap(DONE_BTN);
    }

    @Step("Read the notification (error) message")
    public String getNotificationMessage(long timeoutSec) {
        List<WebElement> els = waitUtils.findQuick(NOTIFICATION_MSG, timeoutSec);
        if (els.isEmpty()) {
            log.warn("Notification message did not appear within {}s", timeoutSec);
            return "";
        }
        String text = els.get(0).getAttribute("text");
        if (text == null || text.isEmpty()) {
            text = els.get(0).getText();
        }
        return text == null ? "" : text;
    }

    @Step("Exit the kid profile")
    public void tapExit() {
        tap(EXIT_BTN);
    }
}
