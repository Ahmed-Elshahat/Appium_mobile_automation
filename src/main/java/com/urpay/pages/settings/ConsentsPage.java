package com.urpay.pages.settings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Consents page — accessed from More → Settings → Consents.
 * Contains links to Privacy Policy, Terms & Conditions, SMS Promotions.
 *
 * Katalon source: Object Repository/android/WalletVas/PrivacyPolicyPage/
 */
public class ConsentsPage extends BasePage {

    // ── More tab → Settings → Consents navigation ────
    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

    private static final By SETTINGS_BTN =
            AppiumBy.xpath("//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Settings']");

    private static final By CONSENT_BTN =
            AppiumBy.accessibilityId("testID-TouchableOpacity.57c34429-0f55-429b-a34f-abce709d70fb.3");

    // ── Consent page items ───────────────────────────
    private static final By CONSENT_TITLE =
            AppiumBy.xpath("//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Consents']");

    private static final By PRIVACY_BTN =
            AppiumBy.xpath("//*[@text='Privacy Policy' and @content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42']");

    private static final By TERMS_CONDITIONS_BTN =
            AppiumBy.xpath("//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Terms & Conditions']");

    // ── Privacy Policy page ──────────────────────────
    private static final By PRIVACY_TITLE =
            AppiumBy.xpath("//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and ./preceding-sibling::*[@content-desc='testID-left-icon-back']]");

    private static final By TOGGLE_BTN =
            AppiumBy.accessibilityId("testID-switcher-undefined");

    private static final By REJECT_PRIVACY_TEXT =
            AppiumBy.accessibilityId("testID-Text.reject-terms-title");

    private static final By BACK_BUTTON =
            AppiumBy.accessibilityId("testID-right-icon-item");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Consents page (More → Settings → Consents)")
    public void navigateToConsents() {
        tap(MORE_NAV);
        waitUtils.waitForVisible(SETTINGS_BTN, 15);
        tap(SETTINGS_BTN);
        waitUtils.waitForVisible(CONSENT_BTN, 15);
        tap(CONSENT_BTN);
        waitUtils.waitForVisible(CONSENT_TITLE, 15);
    }

    @Step("Tap Privacy Policy button")
    public void tapPrivacyPolicy() {
        tap(PRIVACY_BTN);
    }

    @Step("Tap Terms & Conditions button")
    public void tapTermsAndConditions() {
        tap(TERMS_CONDITIONS_BTN);
    }

    // ══════════════════════════════════════════════════
    //  PRIVACY POLICY ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Get Privacy Policy page title")
    public String getPrivacyTitle() {
        return getText(PRIVACY_TITLE);
    }

    @Step("Tap toggle switch (consent/deconsent)")
    public void tapToggle() {
        tap(TOGGLE_BTN);
    }

    @Step("Get reject confirmation text")
    public String getRejectText() {
        return getText(REJECT_PRIVACY_TEXT);
    }

    @Step("Tap back button")
    public void tapBack() {
        tap(BACK_BUTTON);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES
    // ══════════════════════════════════════════════════

    public boolean isConsentPageLoaded() {
        return isPresent(CONSENT_TITLE, 10);
    }

    public boolean isPrivacyTitleVisible() {
        return isPresent(PRIVACY_TITLE, 10);
    }

    public boolean isRejectDialogVisible() {
        return isPresent(REJECT_PRIVACY_TEXT, 10);
    }
}
