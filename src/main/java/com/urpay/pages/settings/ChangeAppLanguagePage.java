package com.urpay.pages.settings;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Change App Language page — accessed from More → Settings → Language.
 * Allows switching between English and Arabic.
 *
 * Katalon source: Object Repository/android/WalletVas/Settings/ChangeAppLanguage/
 */
public class ChangeAppLanguagePage extends BasePage {

    // ── Navigation ───────────────────────────────────
    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

    private static final By SETTINGS_BTN =
            AppiumBy.xpath("//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42' and @text='Settings']");

    private static final By APP_LANGUAGE_BTN =
            AppiumBy.accessibilityId("testID-Text.390fe1ce-cf3f-4333-b7c8-f6fbac527adf.1");

    // ── Language page ────────────────────────────────
    private static final By LANGUAGE_HEADER =
            AppiumBy.xpath("//*[@text='Language']");

    private static final By SELECT_ARABIC =
            AppiumBy.xpath("//*[@content-desc='testID-TouchableOpacity.800b9a21-865f-4a24-b035-3ba5bb3826d1.undefined' and ./preceding-sibling::*[contains(@text,'\u0639\u0631\u0628\u064A')]]");

    private static final By SELECT_ENGLISH =
            AppiumBy.xpath("//*[@content-desc='testID-TouchableOpacity.800b9a21-865f-4a24-b035-3ba5bb3826d1.undefined' and ./following-sibling::*[@text='English']]");

    private static final By SAVE_BUTTON =
            AppiumBy.xpath("//*[@class='android.view.ViewGroup' and ./*[@text='Save']]");

    private static final By YES_CONFIRM_BUTTON =
            AppiumBy.id("android:id/button1");

    // ── Verification elements ────────────────────────
    private static final By ARABIC_BALANCE_TEXT =
            AppiumBy.xpath("//*[@text='\u0631\u0635\u064A\u062F\u0643']");

    private static final By ENGLISH_BALANCE_TEXT =
            AppiumBy.xpath("//*[@text='Your Balance']");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Language settings (More → Settings → Language)")
    public void navigateToLanguageSettings() {
        tap(MORE_NAV);
        waitUtils.waitForVisible(SETTINGS_BTN, 15);
        tap(SETTINGS_BTN);
        waitUtils.waitForVisible(APP_LANGUAGE_BTN, 15);
        tap(APP_LANGUAGE_BTN);
        waitUtils.waitForVisible(LANGUAGE_HEADER, 15);
    }

    // ══════════════════════════════════════════════════
    //  LANGUAGE ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Select Arabic language")
    public void selectArabic() {
        tap(SELECT_ARABIC);
    }

    @Step("Select English language")
    public void selectEnglish() {
        tap(SELECT_ENGLISH);
    }

    @Step("Tap Save button")
    public void tapSave() {
        tap(SAVE_BUTTON);
    }

    @Step("Confirm language change (tap Yes)")
    public void confirmChange() {
        waitUtils.waitForVisible(YES_CONFIRM_BUTTON, 10);
        tap(YES_CONFIRM_BUTTON);
    }

    @Step("Change language to Arabic and confirm")
    public void switchToArabic() {
        selectArabic();
        tapSave();
        confirmChange();
    }

    @Step("Change language to English and confirm")
    public void switchToEnglish() {
        selectEnglish();
        tapSave();
        confirmChange();
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES
    // ══════════════════════════════════════════════════

    public boolean isLanguagePageLoaded() {
        return isPresent(LANGUAGE_HEADER, 10);
    }

    public boolean isArabicApplied() {
        // After language change, app reloads (blue loading screen) — needs extra time
        return isPresent(ARABIC_BALANCE_TEXT, 45);
    }

    public boolean isEnglishApplied() {
        // After language change, app reloads (blue loading screen) — needs extra time
        return isPresent(ENGLISH_BALANCE_TEXT, 45);
    }

    public String getLanguageHeader() {
        return getText(LANGUAGE_HEADER);
    }
}
