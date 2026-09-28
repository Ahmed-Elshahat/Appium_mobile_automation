package com.urpay.pages.settings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/** Update Name — Profile → Update Name → Update (read-only name re-synced from government records, once per account). */
public class UpdateNamePage extends BasePage {

    private static final By PROFILE_AVATAR = AppiumBy.xpath(
            "//android.view.ViewGroup[@clickable='true']"
            + "[.//*[starts-with(@content-desc,'testID-avatar-')"
            + " and not(contains(@content-desc,'ShowMy'))]]");

    // Profile details row .1 (rows: 0 Personal information, 1 Update Name, 2 National address, 3 Change mobile number).
    private static final By UPDATE_NAME_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.1'"
            + " or @text='Update Name' or @text='Update name'"
            + " or @content-desc='testID-Text.c06eec32-711a-4563-863f-c6157b3485bd.1']");

    private static final By UPDATE_NAME_SCREEN = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.445a98db-944b-4653-b88d-250d1f4d6244'"
            + " or @content-desc='testID-Text.7a3ba59f-c4c9-4b4e-94f8-63762c6ef47d'"
            + " or contains(@text,'update your name only once')]");

    private static final By NAME_FIELD = AppiumBy.accessibilityId("testID-input-direct-undefined");

    private static final By UPDATE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main' or @text='Update']");

    private static final By NOTIFICATION_MSG = AppiumBy.xpath(
            "//*[@content-desc='testID-notification-message']");

    private static final By RESULT_TEXT = AppiumBy.xpath(
            "//*[contains(@text,'successfully') or contains(@text,'Thank You')"
            + " or contains(@text,'Error Occurred') or contains(@text,'try again')]");

    private static final By PROFILE_NAME = AppiumBy.accessibilityId(
            "testID-TextV2.d4c6ca5b-83ac-4b58-b6a5-9fd1a998b7c7");

    private static final By BACK_BTN = AppiumBy.accessibilityId("testID-left-icon-back");

    @Step("Tap profile avatar on Dashboard")
    public void tapProfileAvatar() {
        tap(PROFILE_AVATAR);
    }

    public String getProfileName() {
        return getText(PROFILE_NAME);
    }

    @Step("Tap Update Name")
    public void tapUpdateName() {
        if (!isPresent(UPDATE_NAME_BTN, 10)) {
            scrollToText("Update Name");
        }
        tap(UPDATE_NAME_BTN);
    }

    public boolean isUpdateNameScreenDisplayed(long timeoutSec) {
        return isPresent(UPDATE_NAME_SCREEN, timeoutSec);
    }

    public String getDisplayedName() {
        return getText(NAME_FIELD);
    }

    /** Raw-taps Update (no per-tap banner poll) and reads the transient result; "" if none shown. */
    @Step("Tap Update and read the result message")
    public String tapUpdateAndReadResult(long timeoutSec) {
        waitUtils.waitForClickable(UPDATE_BTN).click();
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String msg = firstText(NOTIFICATION_MSG);
            if (msg.isEmpty()) {
                msg = firstText(RESULT_TEXT);
            }
            if (!msg.isEmpty()) {
                log.info("Update Name result: {}", msg);
                return msg;
            }
        }
        log.info("Update Name: no result message within {}s", timeoutSec);
        return "";
    }

    @Step("Back out of Update Name / Profile to Dashboard")
    public void backToDashboard() {
        for (int i = 0; i < 2 && isPresent(BACK_BTN, 3); i++) {
            tap(BACK_BTN);
        }
    }

    private String firstText(By locator) {
        try {
            for (WebElement el : waitUtils.findQuick(locator, 1)) {
                String t = el.getText();
                if (t != null && !t.isBlank()) {
                    return t.trim();
                }
            }
        } catch (Exception ignored) {
            // toast unmounted mid-read — poll again
        }
        return "";
    }
}
