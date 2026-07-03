package com.urpay.pages.wallet;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

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
    private static final By ADD_MONEY_BTN =
            AppiumBy.accessibilityId("testID-primary-addMoneyAction-main");
    private static final By ADD_AMOUNT_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-TextInput.99d56835-0082-495d-8e6a-d3f74489f518']");
    private static final By NEXT_BTN =
            AppiumBy.accessibilityId("testID-primary-action-main");
    private static final By CONFIRM_BTN =
            AppiumBy.accessibilityId("testID-primary-generateOtp-main");
    private static final By THANK_YOU_TEXT =
            AppiumBy.accessibilityId("testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5");
    private static final By DONE_BTN =
            AppiumBy.accessibilityId("testID-primary-onSubmit-main");
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

    @Step("Add {amount} to the kid wallet and confirm")
    public void addMoneyAndConfirm(String amount) {
        enterAmountAndNext(amount);
        tap(CONFIRM_BTN);
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
