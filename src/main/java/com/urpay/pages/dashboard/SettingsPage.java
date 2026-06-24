package com.urpay.pages.dashboard;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * More options → Settings → Logout journey.
 *
 * Migrated from Katalon keyword com.uspace.login.keyword.Login.logOut():
 *   More (nav bar) → Settings (testID-avatar-Setting1-) → Logout
 *   (testID-secondary-logOut-main) → confirm Yes → Landing.
 *
 * Object Repository:
 *   android/Dev/WMV/MoreOptionPage/settingsButton
 *   android/Dev/WMV/MoreOptionPage/SettingsPage/logoutButton
 */
public class SettingsPage extends BasePage {

    private static final By SETTINGS_BTN =
            AppiumBy.accessibilityId("testID-avatar-Setting1-");

    private static final By LOGOUT_BTN =
            AppiumBy.accessibilityId("testID-secondary-logOut-main");

    // Logout confirmation dialog ("Are you sure you want to remove this device" → YES).
    private static final By CONFIRM_LOGOUT = AppiumBy.xpath(
            "//*[@text='Yes' or @text='YES' or @text='Logout' or @text='Log out'"
            + " or @text='Confirm' or @resource-id='android:id/button1']");

    @Step("Open Settings from More options")
    public void openSettings() {
        tap(SETTINGS_BTN);
    }

    @Step("Tap Logout")
    public void tapLogout() {
        tap(LOGOUT_BTN);
    }

    @Step("Confirm logout")
    public void confirmLogout() {
        if (isPresent(CONFIRM_LOGOUT, 8)) {
            WebElement yes = driver.findElement(CONFIRM_LOGOUT);
            Rectangle r = yes.getRect();
            tapAtCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
        }
    }
}
