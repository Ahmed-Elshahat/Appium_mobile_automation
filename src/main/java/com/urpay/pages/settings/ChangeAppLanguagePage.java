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
    private static final By APP_LANGUAGE_BTN =
            AppiumBy.accessibilityId("testID-Text.390fe1ce-cf3f-4333-b7c8-f6fbac527adf.1");

    // ── Language page ───────────────────────────────
    // Bilingual: "Language" (EN) or "اللغة" (AR).
    private static final By LANGUAGE_HEADER =
            AppiumBy.xpath("//*[@text='Language' or @text='\u0627\u0644\u0644\u063A\u0629'"
                    + " or @label='Language' or @label='\u0627\u0644\u0644\u063A\u0629'"
                    + " or @name='Language' or @name='\u0627\u0644\u0644\u063A\u0629']");

    private static final By SELECT_ARABIC =
            AppiumBy.xpath("//*[@content-desc='testID-TouchableOpacity.800b9a21-865f-4a24-b035-3ba5bb3826d1.undefined' and ./preceding-sibling::*[contains(@text,'\u0639\u0631\u0628\u064A')]]");

    private static final By SELECT_ENGLISH =
            AppiumBy.xpath("//*[@content-desc='testID-TouchableOpacity.800b9a21-865f-4a24-b035-3ba5bb3826d1.undefined' and ./following-sibling::*[@text='English']]");

    // The Save button is the primary action at the bottom of the Language page. Its label is
    // localized ("Save" in English, "حفظ" in Arabic), so match the language-independent primary
    // button testID first — obfuscated to "testID-primary--main" on the local build and
    // "testID-primary-<action>-main" on the cloud build — then fall back to the bilingual label.
    private static final By SAVE_BUTTON = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main']"
            + " | //*[starts-with(@content-desc,'testID-primary') and substring(@content-desc,string-length(@content-desc)-4)='-main']"
            + " | //android.view.ViewGroup[./*[@text='Save' or @text='\u062D\u0641\u0638']]");

    private static final By YES_CONFIRM_BUTTON =
            AppiumBy.id("android:id/button1");

    // ── Verification elements ────────────────────────
    // Switching the app locale restarts the app and logs the user out to the
    // Arabic passcode / login screen, so the dashboard balance ("\u0631\u0635\u064A\u062F\u0643")
    // is NOT guaranteed to appear. Verify Arabic via any Arabic string that is
    // reliably present after the switch — dashboard balance OR passcode/login screen.
    //  \u0631\u0635\u064A\u062F\u0643        = "Your Balance" (dashboard, if still logged in)
    //  \u0631\u0645\u0632 \u0627\u0644\u0645\u0631\u0648\u0631   = "Passcode"     (login screen)
    //  \u062A\u0633\u062C\u064A\u0644 \u0627\u0644\u062F\u062E\u0648\u0644 = "Login" (login screen header)
    private static final By ARABIC_APPLIED_MARKER =
            AppiumBy.xpath(
                "//*[contains(@text,'\u0631\u0635\u064A\u062F\u0643') "
              + "or contains(@text,'\u0631\u0645\u0632 \u0627\u0644\u0645\u0631\u0648\u0631') "
              + "or contains(@text,'\u062A\u0633\u062C\u064A\u0644 \u0627\u0644\u062F\u062E\u0648\u0644') "
              + "or contains(@label,'\u0631\u0645\u0632 \u0627\u0644\u0645\u0631\u0648\u0631') "
              + "or contains(@label,'\u062A\u0633\u062C\u064A\u0644 \u0627\u0644\u062F\u062E\u0648\u0644') "
              + "or contains(@value,'\u0631\u0645\u0632 \u0627\u0644\u0645\u0631\u0648\u0631')]");

    // Switching back to English also restarts the app to the (now English) passcode/login screen,
    // so "Your Balance" (dashboard) is not guaranteed. Verify via any English string reliably
    // present after the switch — dashboard balance OR passcode/login screen.
    private static final By ENGLISH_APPLIED_MARKER =
            AppiumBy.xpath("//*[@text='Your Balance' or @text='Passcode'"
                    + " or @text='Enter your passcode' or @text='Login'"
                    + " or contains(@text,'Forgot your passcode')"
                    + " or @label='Passcode' or @label='Login' or @value='Passcode']");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Language settings (Settings deep link → Language)")
    public void navigateToLanguageSettings() {
        // Use the Settings deep link instead of the bottom "More" nav. After a language switch the
        // app re-authenticates and can land on a pushed sub-screen (e.g. the Transactions screen)
        // where the bottom nav is not rendered, so tapping MORE_NAV times out. The deep link routes
        // straight to the Settings page from any in-app screen and is language-independent.
        openDeepLink("urpay://MORENAV/Settings");
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
        // After language change, the app restarts (blue loading screen) and lands on the
        // Arabic passcode/login screen — needs extra time for the reload to settle.
        return isPresent(ARABIC_APPLIED_MARKER, 45);
    }

    public boolean isEnglishApplied() {
        // After language change, the app restarts and lands on the English passcode/login screen —
        // needs extra time for the reload to settle.
        return isPresent(ENGLISH_APPLIED_MARKER, 45);
    }

    public String getLanguageHeader() {
        return getText(LANGUAGE_HEADER);
    }
}
