package com.urpay.pages.dashboard;

import org.openqa.selenium.By;

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
 * SELF-HEAL (UI change): the app no longer returns straight to the landing screen after the
 * "Are you sure you want to logout?" YES. It now lands back on the remembered-login PASSCODE
 * screen with the header back-arrow replaced by an "Unlink device" icon (testID-left-icon-back,
 * nested content-desc "...Unlink"). Tapping it opens an "Unlink this Device?" bottom sheet
 * (Cancel / Unlink Device); confirming "Unlink Device" is what actually completes the logout and
 * returns to the onboarding/landing flow. confirmLogout() now drives this whole sequence so every
 * existing caller (tapLogout() + confirmLogout()) keeps working unchanged.
 *
 * Object Repository:
 *   android/Dev/WMV/MoreOptionPage/settingsButton
 *   android/Dev/WMV/MoreOptionPage/SettingsPage/logoutButton
 */
public class SettingsPage extends BasePage {

    private static final By SETTINGS_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-avatar-Setting1-']"
            + " | //*[starts-with(@content-desc,'testID-avatar-Setting')]"
            + " | //*[starts-with(@name,'testID-avatar-Setting') or starts-with(@label,'testID-avatar-Setting')]");

    private static final By LOGOUT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-secondary-logOut-main']"
            + " | //*[starts-with(@content-desc,'testID-secondary') and substring(@content-desc,string-length(@content-desc)-4)='-main']"
            + " | //android.view.ViewGroup[@clickable='true' and .//android.widget.TextView[@text='Log out' or @text='Logout']]");

    // Logout confirmation dialog ("Are you sure you want to logout?" → YES). Native Android
    // AlertDialog — target the stable system button id, NOT free text (the dialog TITLE text is
    // also "Logout" and would otherwise match a loose '@text=Logout' condition first).
    private static final By CONFIRM_LOGOUT = AppiumBy.xpath(
            "//*[@resource-id='android:id/button1' or @text='Yes' or @text='YES']");

    // Top-left icon on the post-logout passcode screen that now triggers device unlink.
    private static final By UNLINK_DEVICE_ICON = AppiumBy.accessibilityId("testID-left-icon-back");

    // "Unlink this Device?" bottom-sheet confirm button — obfuscation-tolerant (testID-secondary-
    // <hash>-main), keyed on its visible text since the middle segment is hashed on cloud builds.
    private static final By UNLINK_DEVICE_CONFIRM_BTN = AppiumBy.xpath(
            "//*[@text='Unlink Device']/ancestor::*[@clickable='true'][1]");

    @Step("Open Settings from More options")
    public void openSettings() {
        tap(SETTINGS_BTN);
    }

    @Step("Open Settings directly via deep link")
    public void openViaDeepLink() {
        openDeepLink("urpay://MORENAV/Settings");
    }

    @Step("Tap Logout")
    public void tapLogout() {
        tap(LOGOUT_BTN);
    }

    @Step("Confirm logout")
    public void confirmLogout() {
        if (isPresent(CONFIRM_LOGOUT, 8)) {
            tap(CONFIRM_LOGOUT);
        }
        unlinkDeviceIfPrompted();
    }

    /**
     * Self-heal: after the logout alert the app may land on the remembered-login passcode
     * screen instead of the landing page, requiring an explicit device-unlink to finish
     * logging out. No-op when that screen doesn't appear (older builds / already on landing).
     */
    @Step("Unlink device if prompted (post-logout passcode screen)")
    private void unlinkDeviceIfPrompted() {
        if (isPresent(UNLINK_DEVICE_ICON, 10)) {
            tap(UNLINK_DEVICE_ICON);
            if (isPresent(UNLINK_DEVICE_CONFIRM_BTN, 10)) {
                tap(UNLINK_DEVICE_CONFIRM_BTN);
            }
        }
    }
}
