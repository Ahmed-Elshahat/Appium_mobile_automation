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
    // The footer "Unlink from parent" button (the header shares the same text — take the last match).
    private static final By UNLINK_BTN = AppiumBy.xpath(
            "(//android.widget.TextView[@content-desc='" + REACT_TEXT + "' and @text='" + UNLINK_LABEL
            + "'])[last()]");
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
            return isPresent(UNLINK_BTN, 3);
        }
    }

    @Step("Tap the 'Unlink from parent' button")
    public void tapUnlink() {
        tap(UNLINK_BTN);
    }

    /** True once the unlink-request alert popup has rendered. */
    public boolean isUnlinkAlertShown() {
        return isPresent(POPUP_MESSAGE, 15);
    }

    @Step("Tap Send Request to submit the delink request")
    public void tapSendRequest() {
        tap(SEND_REQUEST_BTN);
    }
}
