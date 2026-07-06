package com.urpay.pages.auth;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.utils.DatePickerHandler;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Forgot Passcode page — the passcode-reset journey reached from the login passcode screen.
 *
 * Migrated from Katalon:
 *   Test Suites/.../WMVSuites/ResetPasscode/ForgotPasscode/&lt;Tier&gt;Tier/ForgotPasscode&lt;Tier&gt;Tier
 *   Object Repository/android/WalletVas/ResetPasscode/ForgotPasscode/*
 *
 * Screens covered (in journey order):
 *   Passcode screen → tap "Forgot your passcode?" → User Verification (Date of Birth) →
 *   Enter New Passcode → Confirm New Passcode → success toast → Dashboard.
 *
 * Locator notes:
 *   - Several texts share the generic React testID {@code testID-ReactText.c8f08fb6-…}
 *     ("Forgot your passcode?", "Login", "Next") and are disambiguated by their visible @text.
 *   - The Date-Of-Birth field opens the NATIVE Android date spinner — driven by
 *     {@link DatePickerHandler} (not by app testIDs).
 *   - The new/confirm passcode keypad is a custom on-screen keypad: each digit is a
 *     {@code testID-ReactText.888d97d4-…} node whose @text is the digit. Entering 4 digits
 *     auto-advances (no Next button), so the passcode is keyed by tapping the digit buttons.
 *   - The success toast and date-error alerts use the shared {@code testID-notification-message}
 *     view (same as ChangePasscode / SystemPopupHandler.getMessageFromNotificationPopup).
 */
public class ForgotPasscodePage extends BasePage {

    // ── Passcode screen ──
    private static final By LANGUAGE_TEXT = AppiumBy.xpath("//*[@text='Language']");

    private static final By PASSCODE_TEXT = AppiumBy.xpath(
            "//*[@content-desc='testID-Text.ae1bcb9a-913f-45ca-853b-a44d7495d52c' or @text='Passcode']");

    private static final By LOGIN_TEXT = AppiumBy.xpath("//*[@text='Login']");

    // The "Forgot your passcode?" link that opens the reset journey.
    private static final By FORGOT_PASSCODE_LINK = AppiumBy.xpath(
            "//*[@content-desc='testID-general.passwordReset' or @text='Forgot your passcode?']");

    // The "Forgot your passcode?" value text (verified after the drag-down back gesture).
    private static final By FORGOT_PASSCODE_VALUE = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Forgot your passcode?']");

    // Clickable passcode container on the login passcode screen (shared RN passcode component).
    // Its hidden TextInput is NOT auto-focused, so the DIGIT key events are dropped unless we tap it.
    private static final By PASSCODE_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-passCode.screen' and @clickable='true']");

    // ── User Verification screen ──
    private static final By USER_VERIFICATION_TITLE =
            AppiumBy.accessibilityId("testID-Text.userVerificationTitle");

    private static final By USER_VERIFICATION_SUBTITLE =
            AppiumBy.accessibilityId("testID-Text.securityQuestionsPrompt");

    private static final By TOGGLE_BUTTON_TEXT =
            AppiumBy.accessibilityId("testID-Text.hijriSwitchLabel");

    private static final By TOGGLE_BUTTON =
            AppiumBy.accessibilityId("testID-Switch.d1583aa8-cf20-49ca-b2b1-f0a3dfccaa24.undefined");

    private static final By NEXT_BUTTON_TEXT = AppiumBy.xpath(
            "//android.widget.TextView[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Next']");

    private static final By DATE_OF_BIRTH_LABEL = AppiumBy.xpath(
            "(//*[@content-desc='testID-ReactText.888d97d4-41e5-487a-bff6-34201eb3731e'])[1]");

    // Top drag handle used by the "drag down to go back" gesture from the User Verification screen.
    private static final By DRAG_BTN =
            AppiumBy.accessibilityId("testID-View.409a2074-6d13-4cbe-9726-c4c974663c15");

    // ── Calendar (native date spinner) ──
    private static final By CALENDAR_OPEN_BTN =
            AppiumBy.accessibilityId("testID-DatePicker.dateOfBirth");

    private static final By CALENDAR_OK_BTN =
            AppiumBy.xpath("//android.widget.Button[@resource-id='android:id/button1']");

    private static final By CALENDAR_NEXT_BTN =
            AppiumBy.accessibilityId("testID-primary--main");

    // ── Enter New Passcode / Confirm New Passcode screens ──
    // Header "Enter New Passcode" — shown on BOTH the new and confirm screens.
    private static final By ENTER_NEW_PASSCODE_HEADER =
            AppiumBy.accessibilityId("testID-Text.1fca2237-43d5-4c65-8d84-6de6906b7784");

    // Subtitle below the header — reused element; "Enter your wallet passcode." on the new screen,
    // "please enter the value" on the confirm screen.
    private static final By TEXT_BELOW_HEADER =
            AppiumBy.accessibilityId("testID-Text.f97ae47e-5085-439d-b509-1b839b2f4ded");

    // Visible-text markers used to distinguish the new vs confirm screens.
    private static final By NEW_PASSCODE_SUBTITLE =
            AppiumBy.xpath("//*[@text='Enter your wallet passcode.']");

    private static final By CONFIRM_PASSCODE_SUBTITLE =
            AppiumBy.xpath("//*[@text='please enter the value']");

    // ── Shared toast / notification popup (success + date errors) ──
    private static final By NOTIFICATION_MESSAGE =
            AppiumBy.xpath("//*[@content-desc='testID-notification-message']");

    // ══════════════════════════════════════════════════
    //  PASSCODE SCREEN
    // ══════════════════════════════════════════════════

    public boolean isPasscodeScreenDisplayed() {
        return isPresent(PASSCODE_TEXT, 30);
    }

    public String getLanguageText() {
        return getText(LANGUAGE_TEXT);
    }

    public String getPasscodeText() {
        return getText(PASSCODE_TEXT);
    }

    public String getLoginText() {
        return getText(LOGIN_TEXT);
    }

    @Step("Tap 'Forgot your passcode?'")
    public void tapForgotPasscode() {
        waitUtils.waitForVisible(FORGOT_PASSCODE_LINK, 30);
        tap(FORGOT_PASSCODE_LINK);
    }

    // ══════════════════════════════════════════════════
    //  USER VERIFICATION SCREEN
    // ══════════════════════════════════════════════════

    public boolean isUserVerificationScreenDisplayed() {
        return isPresent(USER_VERIFICATION_TITLE, 30);
    }

    public String getUserVerificationTitle() {
        return getText(USER_VERIFICATION_TITLE);
    }

    public String getUserVerificationSubtitle() {
        return getText(USER_VERIFICATION_SUBTITLE);
    }

    public String getToggleButtonText() {
        return getText(TOGGLE_BUTTON_TEXT);
    }

    public String getNextButtonText() {
        return getText(NEXT_BUTTON_TEXT);
    }

    public String getDateOfBirthLabel() {
        return getText(DATE_OF_BIRTH_LABEL);
    }

    @Step("Toggle the Hijri/Gregorian switch")
    public void tapToggleButton() {
        tap(TOGGLE_BUTTON);
    }

    @Step("Enter date of birth {month}/{day}/{year} via the native date spinner")
    public void enterDateOfBirth(String month, String day, String year) {
        tap(CALENDAR_OPEN_BTN);
        new DatePickerHandler(driver).selectDate(month, day, year);
        tap(CALENDAR_OK_BTN);
        tap(CALENDAR_NEXT_BTN);
    }

    @Step("Open the date-of-birth calendar and confirm the default date (no selection)")
    public void openCalendarAndConfirmDefault() {
        tap(CALENDAR_OPEN_BTN);
        tap(CALENDAR_OK_BTN);
    }

    @Step("Tap Next on the User Verification screen")
    public void tapUserVerificationNext() {
        tap(CALENDAR_NEXT_BTN);
    }

    /**
     * Drag down from the top handle to navigate back from the User Verification screen to the
     * passcode screen (Katalon {@code dragAndDrop(dragBtn → nextButtonText)}). Implemented as a
     * coordinate drag from the drag handle to the Next button's position.
     */
    @Step("Drag down to go back to the passcode screen")
    public void dragDownToGoBack() {
        List<WebElement> handles = driver.findElements(DRAG_BTN);
        List<WebElement> targets = driver.findElements(NEXT_BUTTON_TEXT);
        if (handles.isEmpty() || targets.isEmpty()) {
            log.warn("Drag handle or target not found — cannot perform drag-down back gesture");
            return;
        }
        org.openqa.selenium.Rectangle src = handles.get(0).getRect();
        org.openqa.selenium.Rectangle dst = targets.get(0).getRect();
        int startX = src.getX() + src.getWidth() / 2;
        int startY = src.getY() + src.getHeight() / 2;
        int endX = dst.getX() + dst.getWidth() / 2;
        int endY = dst.getY() + dst.getHeight() / 2;
        swipeUtils.performSwipe(startX, startY, endX, endY);
    }

    public boolean isBackOnPasscodeScreen() {
        return isPresent(FORGOT_PASSCODE_VALUE, 30);
    }

    public String getForgotPasscodeValueText() {
        return getText(FORGOT_PASSCODE_VALUE);
    }

    // ══════════════════════════════════════════════════
    //  ENTER NEW / CONFIRM PASSCODE SCREENS
    // ══════════════════════════════════════════════════

    public boolean isEnterNewPasscodeScreenDisplayed() {
        return isPresent(ENTER_NEW_PASSCODE_HEADER, 30);
    }

    public boolean isConfirmPasscodeScreenDisplayed() {
        return isPresent(CONFIRM_PASSCODE_SUBTITLE, 30);
    }

    public String getEnterNewPasscodeHeader() {
        return getText(ENTER_NEW_PASSCODE_HEADER);
    }

    public String getTextBelowHeader() {
        return getText(TEXT_BELOW_HEADER);
    }

    /**
     * Key the passcode on the custom on-screen keypad by tapping each digit button. Entering all
     * four digits auto-advances to the next screen (no Next button), mirroring the Katalon flow
     * which taps the keypad digit buttons directly.
     */
    @Step("Enter passcode on the on-screen keypad")
    public void enterPasscodeOnKeypad(String passcode) {
        for (char digit : passcode.toCharArray()) {
            tapKeypadDigit(String.valueOf(digit));
        }
    }

    /**
     * Enter the passcode on the on-screen keypad and immediately read the resulting toast. The
     * shared {@code testID-notification-message} toast is transient; {@link BasePage#tap} runs a
     * per-tap error-banner poll that adds ~1s, and on the final digit that delay would push the
     * first notification poll past the toast's lifetime and miss it. The last digit is therefore
     * tapped without that check so the read starts at once.
     */
    @Step("Enter passcode on the on-screen keypad and read the notification toast")
    public String enterPasscodeAndReadNotification(String passcode, long timeoutSec) {
        char[] digits = passcode.toCharArray();
        for (int i = 0; i < digits.length - 1; i++) {
            tapKeypadDigit(String.valueOf(digits[i]));
        }
        if (digits.length > 0) {
            tapKeypadDigitRaw(String.valueOf(digits[digits.length - 1]));
        }
        return getNotificationMessage(timeoutSec);
    }

    private void tapKeypadDigit(String digit) {
        tap(keypadDigit(digit));
    }

    private void tapKeypadDigitRaw(String digit) {
        // Raw click without BasePage.tap's per-tap error-banner poll (~1s) so the transient
        // rejection toast fired by the final digit can be read immediately.
        waitUtils.waitForClickable(keypadDigit(digit)).click();
    }

    private By keypadDigit(String digit) {
        return AppiumBy.xpath(
                "//*[@content-desc='testID-ReactText.888d97d4-41e5-487a-bff6-34201eb3731e'"
                + " and @text='" + digit + "']");
    }

    /**
     * Enter a passcode on the LOGIN passcode screen (the screen reached before tapping "Forgot
     * your passcode?"). Mirrors Katalon {@code Keypad.fillPassCodeToLogin}: the hidden RN input is
     * focused first (its TextInput is not auto-focused) and the digits are sent as native key
     * events. Used by the wrong-passcode negative cases.
     */
    @Step("Enter passcode on the login passcode screen")
    public void enterLoginPasscode(String passcode) {
        focusPasscodeInput();
        platformActions.enterDigits(passcode);
    }

    private void focusPasscodeInput() {
        try {
            List<WebElement> els = driver.findElements(PASSCODE_INPUT);
            if (!els.isEmpty()) {
                els.get(els.size() - 1).click();
                return;
            }
        } catch (Exception e) {
            log.warn("Click passcode input wrapper failed: {}", e.getMessage());
        }
        try {
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            int x = (int) (size.getWidth() * 0.5);
            int y = (int) (size.getHeight() * 0.34);
            swipeUtils.tapAtCoordinates(x, y);
        } catch (Exception e) {
            log.warn("Tap-to-focus passcode failed: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    //  SUCCESS / NOTIFICATION
    // ══════════════════════════════════════════════════

    /**
     * Read the shared toast/notification message (success or error). Mirrors Katalon
     * {@code SystemPopupHandler.getMessageFromNotificationPopup}: poll for
     * {@code testID-notification-message} and read its {@code text} attribute. Best-effort —
     * returns an empty string if the transient popup never appears within the timeout.
     */
    public String getNotificationMessage(long timeoutSec) {
        List<WebElement> els = waitUtils.findQuick(NOTIFICATION_MESSAGE, timeoutSec);
        if (els.isEmpty()) {
            log.warn("Notification message popup did not appear within {}s", timeoutSec);
            return "";
        }
        WebElement el = els.get(0);
        String text = el.getAttribute("text");
        if (text == null || text.isEmpty()) {
            text = el.getText();
        }
        log.info("Notification popup message: {}", text);
        return text == null ? "" : text;
    }
}
