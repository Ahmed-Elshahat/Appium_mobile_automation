package com.urpay.pages.payments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Card Settings page — online/ATM toggles, card lock, PIN change, cancel card.
 * Uses TEXT-BASED xpath locators — accessibility IDs are dynamic UUIDs.
 */
public class CardSettingsPage extends BasePage {

    // ── Text-based locators (stable across builds) ──
    private static final By CARD_SETTINGS_BTN = AppiumBy.xpath("//*[@text='Card Settings']");
    private static final By ONLINE_TOGGLE = AppiumBy.xpath(
            "(//*[contains(@content-desc,'Switch') or contains(@content-desc,'switcher')])[1]");
    private static final By ATM_TOGGLE = AppiumBy.xpath(
            "(//*[contains(@content-desc,'Switch') or contains(@content-desc,'switcher')])[2]");
    private static final By LOCK_TOGGLE = AppiumBy.xpath(
            "(//*[contains(@content-desc,'Switch') or contains(@content-desc,'switcher')])[1]");
    private static final By CHANGE_PIN_TEXT = AppiumBy.xpath("//*[@text='Change PIN Code' or @text='Change card PIN']");
    // Katalon: ChangeCardPinBtn has text="Change" — must NOT match "Change PIN Code" label
    private static final By CHANGE_PIN_BTN = AppiumBy.xpath(
            "//*[@text='Change' and @class='android.widget.TextView']");
    private static final By CANCEL_CARD_TEXT = AppiumBy.xpath("//*[@text='Cancel Card' or @text='Cancel card']");
    private static final By CANCEL_CARD_BTN = AppiumBy.xpath(
            "//*[@text='Cancel' and @class='android.widget.TextView']");
    private static final By CANCELLATION_DROPDOWN = AppiumBy.xpath(
            "//*[contains(@content-desc,'DownArrow')] | //*[@text='Select reason']");
    private static final By OTHER_REASON = AppiumBy.xpath(
            "//*[@text='Other reasons' or @text='Other'] | //*[contains(@content-desc,'data-picker-item-3')]");
    private static final By CONFIRM_CANCEL_BTN = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-cancel--main')] | " +
            "//*[contains(@content-desc,'testID-primary')] | " +
            "(//*[@text='Cancel Card'])[last()]");
    private static final By NO_THANKS_BTN = AppiumBy.xpath(
            "//*[@text='No thanks'] | //*[contains(@content-desc,'noThankButton')]");

    @AndroidFindBy(accessibility = "testID-primary-callAPI-main")
    private WebElement lockYesButton;

    @AndroidFindBy(accessibility = "testID-secondary-onClose-main")
    private WebElement lockNoButton;

    @AndroidFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ══════════════════════════════════════════════════

    @Step("Tap Card Settings button")
    public void tapCardSettings() { tap(CARD_SETTINGS_BTN); }

    @Step("Toggle Online Transactions")
    public void tapOnlineTransactionsToggle() { tap(ONLINE_TOGGLE); }

    @Step("Toggle ATM Transactions")
    public void tapAtmTransactionToggle() { tap(ATM_TOGGLE); }

    @Step("Tap Lock/Unlock toggle")
    public void tapLockToggle() { tap(LOCK_TOGGLE); }

    @Step("Tap Yes on lock popup")
    public void tapLockYes() { tap(lockYesButton); }

    @Step("Tap No on lock popup")
    public void tapLockNo() { tap(lockNoButton); }

    @Step("Scroll to Change PIN section")
    public void tapChangePinSettings() {
        // No scroll needed — Change PIN is visible on Card Settings page
    }

    @Step("Tap Change Card PIN button")
    public void tapChangePin() { tap(CHANGE_PIN_BTN); }

    @Step("Tap Cancel Card option")
    public void tapCancelCard() {
        swipeUp();
        tap(CANCEL_CARD_BTN);
    }

    @Step("Tap Cancellation Reason dropdown")
    public void tapCancellationDropdown() { tap(CANCELLATION_DROPDOWN); }

    @Step("Select 'Other' cancellation reason")
    public void selectOtherReason() { tap(OTHER_REASON); }

    @Step("Tap Confirm Cancel button")
    public void tapConfirmCancel() { tap(CONFIRM_CANCEL_BTN); }

    @Step("Tap No Thanks button")
    public void tapNoThanks() { tap(NO_THANKS_BTN); }

    // ══════════════════════════════════════════════════

    public String getNotificationMessage() {
        waitUtils.waitForVisible(
                AppiumBy.accessibilityId("testID-notification-message"), 10);
        return getText(notificationMessage);
    }

    /** Read notification text if visible; returns null if not present or auto-dismissed */
    public String readNotificationIfVisible() {
        try {
            org.openqa.selenium.WebElement el = waitUtils.waitForVisible(
                    AppiumBy.accessibilityId("testID-notification-message"), 5);
            return el.getText();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isNotificationVisible() {
        return isPresent(AppiumBy.accessibilityId("testID-notification-message"), 5);
    }
}
