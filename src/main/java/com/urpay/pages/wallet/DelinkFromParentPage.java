package com.urpay.pages.wallet;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Delink From Parent — a kid sends an "unlink from parent" request from the profile screen.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/DelinkFromParentBySendRequest/DelinkFromParentByKid
 *   ({@code Wallet_VAS/DelinkFromParentBySendRequest/*}).
 *
 * <p>Locator notes: the footer buttons share the generic React-Native text testID and are
 * disambiguated by their visible {@code text} (Unlink from parent / Send Request / No thanks).
 */
public class DelinkFromParentPage extends BasePage {

    private static final String REACT_TEXT = "testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42";

    // Profile entry icon (Katalon DelinkFromParentByKid/profileIconBtn).
    private static final By PROFILE_ICON = AppiumBy.xpath(
            "//android.view.ViewGroup[@content-desc='testID-View.fd0714e7-bbe2-41cb-8bd7-6f98bdb9c586']"
            + "/android.view.ViewGroup[1]");
    private static final String UNLINK_LABEL = "Unlink from parent";
    // The footer "Unlink from parent" TextView (the header shares the same text — take the last match).
    private static final By UNLINK_TEXT = AppiumBy.xpath(
            "(//android.widget.TextView[@content-desc='" + REACT_TEXT + "' and @text='" + UNLINK_LABEL
            + "'])[last()]");
    // The clickable ancestor of that text — RN wraps the label in a clickable ViewGroup; tapping the
    // bare TextView can be a no-op (or dismiss the profile sheet), so prefer the clickable container.
    private static final By UNLINK_CLICKABLE = AppiumBy.xpath(
            "(//android.widget.TextView[@content-desc='" + REACT_TEXT + "' and @text='" + UNLINK_LABEL
            + "'])[last()]/ancestor::*[@clickable='true'][1]");
    // The alert popup message under the header (Katalon textBelowTheHeaderOfPopup).
    private static final By POPUP_MESSAGE =
            AppiumBy.accessibilityId("testID-total-transfer-message");
    private static final By SEND_REQUEST_BTN = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='" + REACT_TEXT + "' and @text='Send Request']");

    @Step("Open the profile section")
    public void openProfile() {
        tap(PROFILE_ICON);
    }

    @Step("Scroll to the 'Unlink from parent' button")
    public boolean scrollToUnlink() {
        try {
            return scrollToText(UNLINK_LABEL) != null;
        } catch (Exception e) {
            return isPresent(UNLINK_TEXT, 3);
        }
    }

    @Step("Tap the 'Unlink from parent' button")
    public void tapUnlink() {
        // Prefer the clickable ViewGroup wrapper; fall back to the bare text if no clickable ancestor.
        if (isPresent(UNLINK_CLICKABLE, 3)) {
            tap(UNLINK_CLICKABLE);
        } else {
            tap(UNLINK_TEXT);
        }
    }

    /** True once the unlink-request alert popup has rendered (message text OR the Send Request button). */
    public boolean isUnlinkAlertShown() {
        return isPresent(POPUP_MESSAGE, 15) || isPresent(SEND_REQUEST_BTN, 3);
    }

    @Step("Tap Send Request to submit the delink request")
    public void tapSendRequest() {
        tap(SEND_REQUEST_BTN);
    }
}
