package com.urpay.pages.settings;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Change Phone Number page.
 *
 * Migrated from Katalon:
 *   Test Cases/Wallet_VAS/ChangePhoneNumber/* + Object Repository/android/WalletVas/ChangePhoneNumber/*
 *
 * Real navigation path (per Katalon ValidateOpeningChangePhoneNumber):
 *   Dashboard → tap profile avatar (user initial badge) → tap "Change mobile number".
 *
 * Locator notes:
 *   - Profile avatar reuses a generic React testID; disambiguated by single-character text.
 *   - Form/header/buttons keep text fallbacks so they survive minor testID churn.
 *   - XPath used where no stable accessibility id exists — flagged for dedicated testIDs.
 */
public class ChangePhoneNumberPage extends BasePage {

    // ── Navigation (Dashboard → avatar → change number) ──
    // The avatar marker (testID-avatar-<user name>-) is not clickable/enabled itself; its
    // direct parent ViewGroup is the touchable wrapper. Target that clickable container so
    // the locator stays user-independent and unambiguous (the old single-char text match
    // also matched the balance decimal point).
    private static final By PROFILE_AVATAR = AppiumBy.xpath(
            "//android.view.ViewGroup[@clickable='true']"
            + "[.//*[starts-with(@content-desc,'testID-avatar-')"
            + " and not(contains(@content-desc,'ShowMy'))]]");

    private static final By CHANGE_PHONE_BTN = AppiumBy.xpath(
            "//*[@text='Change mobile number'"
            + " or @content-desc='testID-Text.c06eec32-711a-4563-863f-c6157b3485bd.3']");

    // ── Change Mobile Number form ────────────────────
    private static final By CHANGE_MOBILE_HEADER = AppiumBy.xpath(
            "//*[@text='Change Mobile Number']");

    private static final By NEW_MOBILE_FIELD =
            AppiumBy.accessibilityId("testID-input-direct-mobile");

    private static final By NEXT_BTN =
            AppiumBy.accessibilityId("testID-primary--main");

    // ── "Number Already In Use" popup ────────────────
    private static final By ALREADY_IN_USE_TITLE = AppiumBy.xpath(
            "//android.widget.TextView[@resource-id='android:id/alertTitle'"
            + " or @text='This Number Already in Use']");

    private static final By ALREADY_IN_USE_DONE_BTN = AppiumBy.xpath(
            "//android.widget.Button[@resource-id='android:id/button1' or @text='DONE']");

    // ── Thank You screen ─────────────────────────────
    private static final By THANK_YOU_ANIMATION =
            AppiumBy.accessibilityId("testID-LottieView.8a85644b-0405-47b9-8795-98e38695dd41");

    private static final By THANK_YOU_TEXT = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5'"
            + " or @text='Thank You!']");

    private static final By DONE_BTN_THANKS = AppiumBy.xpath(
            "//*[@text='Done']");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Tap profile avatar on Dashboard")
    public void tapProfileAvatar() {
        tap(PROFILE_AVATAR);
    }

    @Step("Tap 'Change mobile number'")
    public void tapChangePhoneNumber() {
        // Dismiss keyboard first, then scroll the item into view before tapping.
        platformActions.dismissKeyboard();
        if (!isPresent(CHANGE_PHONE_BTN, 3)) {
            scrollToText("Change mobile number");
        }
        tap(CHANGE_PHONE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  CHANGE PHONE FORM
    // ══════════════════════════════════════════════════

    public boolean isChangePhoneScreenLoaded() {
        return isPresent(CHANGE_MOBILE_HEADER, 30);
    }

    @Step("Enter new mobile number: {number}")
    public void enterNewMobileNumber(String number) {
        type(NEW_MOBILE_FIELD, number);
    }

    @Step("Tap Next")
    public void tapNext() {
        hideKeyboard();
        tap(NEXT_BTN);
    }

    @Step("Enter verification code")
    public void enterVerificationCode(String code) {
        platformActions.enterDigits(code);
    }

    // ══════════════════════════════════════════════════
    //  "NUMBER ALREADY IN USE" HANDLING
    // ══════════════════════════════════════════════════

    public boolean isNumberAlreadyInUseDisplayed() {
        return isPresent(ALREADY_IN_USE_TITLE, 5);
    }

    @Step("Dismiss 'Number Already In Use' popup")
    public void dismissAlreadyInUsePopup() {
        tap(ALREADY_IN_USE_DONE_BTN);
    }

    @Step("Clear and re-enter mobile number: {number}")
    public void clearAndReenterNumber(String number) {
        type(NEW_MOBILE_FIELD, number);
    }

    // ══════════════════════════════════════════════════
    //  THANK YOU SCREEN
    // ══════════════════════════════════════════════════

    public boolean isThankYouScreenDisplayed() {
        return isPresent(THANK_YOU_ANIMATION, 20);
    }

    public String getThankYouText() {
        return getText(THANK_YOU_TEXT);
    }

    @Step("Tap Done on Thank You screen")
    public void tapDone() {
        tap(DONE_BTN_THANKS);
    }
}
