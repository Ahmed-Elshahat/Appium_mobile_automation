package com.urpay.pages.payments;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Card Replacement page — request replacement, select reason/city, confirm.
 *
 * Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/cardSettings/RequestReplacmentCard/
 *
 * Flow: Card Settings → Replace → reason dropdown → Damage → Next →
 *       city dropdown → city → Next → Confirm → Thank You → Done
 */
public class CardReplacementPage extends BasePage {

    // ── Replacement flow locators (testIDs from Katalon) ──
    private static final By REPLACE_BTN = AppiumBy.xpath(
            "//*[@text='Replace' and @class='android.widget.TextView']");
    private static final By REASON_DROPDOWN = AppiumBy.accessibilityId("testID-multi-select-value");
    private static final By DAMAGE_REASON = AppiumBy.accessibilityId("testID-search-item-1");
    private static final By NEXT_BTN = AppiumBy.accessibilityId("testID-primary--main");
    private static final By CITY_DROPDOWN = AppiumBy.accessibilityId("testID-multi-select-city");
    private static final By CITY_SELECTION = AppiumBy.accessibilityId("testID-search-item-1");
    private static final By CONFIRM_BTN = AppiumBy.accessibilityId("testID-primary-action-main");
    private static final By THANK_YOU_LABEL = AppiumBy.accessibilityId("testID-total-transfer");
    private static final By DONE_BTN = AppiumBy.accessibilityId("testID-primary-close-main");

    // ── Activation locators ──
    private static final By ACTIVATE_BTN = AppiumBy.accessibilityId("testID-primary-activate-main");
    private static final By BACK_TO_CARDS_BTN = AppiumBy.accessibilityId("testID-primary-backToCards-main");

    // ── Transaction detail locators ──
    private static final By TRANSACTION_SUBTITLE = AppiumBy.accessibilityId("testID-second-secRow-0");
    private static final By TRANSACTION_TYPE_VALUE = AppiumBy.accessibilityId("testID-label-value-main-0");
    private static final By TRANSACTION_REF_NUMBER = AppiumBy.accessibilityId("testID-label-value-main-2");

    // ── Notification ──
    private static final By NOTIFICATION_MESSAGE = AppiumBy.accessibilityId("testID-notification-message");

    // ══════════════════════════════════════════════════
    //  REPLACEMENT FLOW ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Replace button")
    public void tapReplace() { tap(REPLACE_BTN); }

    @Step("Select replacement reason — Damage")
    public void selectDamageReason() {
        tap(REASON_DROPDOWN);
        tap(DAMAGE_REASON);
    }

    @Step("Tap Next")
    public void tapNext() { tap(NEXT_BTN); }

    @Step("Select replacement city")
    public void selectCity() {
        tap(CITY_DROPDOWN);
        tap(CITY_SELECTION);
    }

    @Step("Tap Confirm replacement")
    public void tapConfirm() { tap(CONFIRM_BTN); }

    @Step("Tap Done after replacement")
    public void tapDone() { tap(DONE_BTN); }

    // ══════════════════════════════════════════════════
    //  ACTIVATION ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Activate replacement card")
    public void tapActivate() { tap(ACTIVATE_BTN); }

    @Step("Tap Back to Cards")
    public void tapBackToCards() { tap(BACK_TO_CARDS_BTN); }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public boolean isThankYouVisible() {
        return isPresent(THANK_YOU_LABEL, 15);
    }

    public String getTransactionSubtitle() {
        return getText(TRANSACTION_SUBTITLE);
    }

    public String getTransactionTypeValue() {
        return getText(TRANSACTION_TYPE_VALUE);
    }

    public String getTransactionRefNumber() {
        return getText(TRANSACTION_REF_NUMBER);
    }

    public String readNotificationIfVisible() {
        try {
            org.openqa.selenium.WebElement el = waitUtils.waitForVisible(NOTIFICATION_MESSAGE, 5);
            return el.getText();
        } catch (Exception e) {
            return null;
        }
    }
}
