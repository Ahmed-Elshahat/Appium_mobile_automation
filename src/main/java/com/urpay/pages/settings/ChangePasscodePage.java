package com.urpay.pages.settings;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Change Passcode page — accessed from More → Settings → Change Passcode.
 *
 * Covers every screen of the Change Passcode journey:
 *   Current passcode → New passcode → Confirm new passcode → Verification (OTP) → Thank You.
 *
 * Migrated from Katalon:
 *   Test Suites/WMVSuites/ResetPasscode/ChangePasscode/DefaultTier/ChangePasscodeDefaultTier
 *   Object Repository/android/WalletVas/ResetPasscode/ChangePasscode/*
 *
 * Locator notes:
 *   - The title/subtitle React testIDs are REUSED across every screen
 *     (e.g. testID-Text.ae1bcb9a-… is "Current passcode", "New Passcode" and
 *     "Confirm new passcode" depending on the screen). They are therefore
 *     disambiguated here by their visible @text, not by content-desc.
 *   - Passcode/OTP digits go through a custom keypad → platformActions.enterDigits();
 *     entering all 4 digits auto-advances to the next screen (no Next button).
 */
public class ChangePasscodePage extends BasePage {

    // ── Navigation (More → Settings → Change Passcode) ──
    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

    private static final By SETTINGS_LABEL = AppiumBy.xpath(
            "//*[@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
            + " and @text='Settings']");

    private static final By CHANGE_PASSCODE_BTN = AppiumBy.xpath(
            "//*[@text='Change Passcode'"
            + " or @content-desc='testID-TouchableOpacity.57c34429-0f55-429b-a34f-abce709d70fb.0'"
            + " or @content-desc='testID-Text.390fe1ce-cf3f-4333-b7c8-f6fbac527adf.0']");

    private static final By CHANGE_PASSCODE_HEADER =
            AppiumBy.xpath("//*[@text='Change Passcode']");

    // ── Header back / close buttons ──
    private static final By BACK_BTN =
            AppiumBy.accessibilityId("testID-right-icon-item");

    private static final By CLOSE_BTN =
            AppiumBy.accessibilityId("testID-right-icon-0");

    // ── Current Passcode screen ──
    private static final By CURRENT_PASSCODE_TITLE =
            AppiumBy.xpath("//*[@text='Current passcode']");

    private static final By CURRENT_PASSCODE_SUBTITLE =
            AppiumBy.xpath("//*[@text='Enter your current passcode']");

    // ── New Passcode screen ──
    private static final By NEW_PASSCODE_TITLE =
            AppiumBy.xpath("//*[@text='New Passcode']");

    private static final By NEW_PASSCODE_SUBTITLE =
            AppiumBy.xpath("//*[contains(@text,'Setup a new passcode for your account')]");

    // ── Confirm New Passcode screen ──
    private static final By CONFIRM_PASSCODE_TITLE =
            AppiumBy.xpath("//*[@text='Confirm new passcode']");

    private static final By CONFIRM_PASSCODE_SUBTITLE =
            AppiumBy.xpath("//*[@text='Re-enter your new passcode']");

    // ── Verification (OTP) screen ──
    // Match by the stable title; the helper text below uses a typographic apostrophe (’)
    // that differs from a straight ASCII ' so an exact @text match is unreliable.
    private static final By OTP_SCREEN_TITLE = AppiumBy.xpath(
            "//*[@text='Verification code' or contains(@text,'enter the code sent')]");
    private static final By DIDNT_GET_CODE_TEXT =
            AppiumBy.xpath("//*[contains(@text,'get the code')]");

    // First OTP box — the reusable OTP component (same testID as login / Money Request).
    // Tapping it focuses the hidden input so the digit key events actually register.
    private static final By OTP_INPUT_FIELD_0 =
            AppiumBy.accessibilityId("testID-OTP-Input-Field-0");

    // Clickable passcode container (shared RN passcode component, same as login). Its hidden
    // TextInput is NOT auto-focused, so the digit key events are dropped unless we tap it first.
    private static final By PASSCODE_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-passCode.screen' and @clickable='true']");

    // ── Thank You screen ──
    private static final By THANK_YOU_TITLE = AppiumBy.xpath(
            "//*[@text='Thank You!'"
            + " or @content-desc='testID-Text.7e9fc765-884f-4f79-9f43-1ae77833b7a5']");

    private static final By THANK_YOU_SUBTITLE =
            AppiumBy.xpath("//*[@content-desc='testID-TextV2.654f578a-2ee5-47a4-8260-465122290381']");

    private static final By DONE_TEXT =
            AppiumBy.xpath("//android.widget.TextView["
                    + "@content-desc='testID-ReactText.c8f08fb6-ea7b-4dd0-b237-cf96296e4c42'"
                    + " or @text='Done']");

    private static final By DONE_BTN =
            AppiumBy.accessibilityId("testID-primary--main");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Change Passcode (More → Settings → Change Passcode)")
    public void navigateToChangePasscode() {
        tap(MORE_NAV);
        waitUtils.waitForVisible(SETTINGS_LABEL, 20);
        tap(SETTINGS_LABEL);
        waitUtils.waitForVisible(CHANGE_PASSCODE_BTN, 20);
        tap(CHANGE_PASSCODE_BTN);
        waitUtils.waitForVisible(CHANGE_PASSCODE_HEADER, 20);
    }

    @Step("Reopen Change Passcode from Settings screen")
    public void reopenChangePasscodeFromSettings() {
        waitUtils.waitForVisible(SETTINGS_LABEL, 20);
        tap(CHANGE_PASSCODE_BTN);
        waitUtils.waitForVisible(CHANGE_PASSCODE_HEADER, 20);
    }

    @Step("Tap header back button")
    public void tapBack() {
        tap(BACK_BTN);
    }

    @Step("Tap header close button")
    public void tapClose() {
        tap(CLOSE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  PASSCODE / OTP ENTRY (custom keypad — auto-advances)
    // ══════════════════════════════════════════════════

    @Step("Enter passcode digits")
    public void enterPasscode(String passcode) {
        // The passcode boxes have a hidden input that is NOT auto-focused — without focus the
        // DIGIT_x key events are dropped and the boxes stay empty (so the screen never advances).
        focusPasscodeInput();
        platformActions.enterDigits(passcode);
    }

    /**
     * Focus the hidden passcode TextInput by clicking its clickable RN wrapper; fall back to a
     * coordinate tap on the boxes region (~34% down) if the wrapper is not resolvable.
     */
    private void focusPasscodeInput() {
        try {
            java.util.List<org.openqa.selenium.WebElement> els = driver.findElements(PASSCODE_INPUT);
            if (!els.isEmpty()) {
                els.get(els.size() - 1).click(); // inner clickable wrapper
                log.info("Clicked passcode input wrapper to focus");
                return;
            }
        } catch (Exception e) {
            log.warn("Click passcode input wrapper failed: {}", e.getMessage());
        }
        try {
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            int x = (int) (size.getWidth() * 0.5);
            int y = (int) (size.getHeight() * 0.34);
            var finger = new org.openqa.selenium.interactions.PointerInput(
                    org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1");
            var tap = new org.openqa.selenium.interactions.Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(java.time.Duration.ZERO,
                    org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(
                    org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(new org.openqa.selenium.interactions.Pause(finger,
                    java.time.Duration.ofMillis(100)));
            tap.addAction(finger.createPointerUp(
                    org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(java.util.Collections.singletonList(tap));
            log.info("Tapped passcode boxes to focus input at ({}, {})", x, y);
        } catch (Exception e) {
            log.warn("Tap-to-focus passcode failed: {}", e.getMessage());
        }
    }

    @Step("Enter OTP digits")
    public void enterOtp(String otp) {
        // The digit key events only land when an input is focused — focus the first OTP box first,
        // mirroring the reliable login OtpPage pattern (otherwise the boxes stay empty).
        try {
            waitUtils.waitForVisible(OTP_INPUT_FIELD_0, 15);
            tap(OTP_INPUT_FIELD_0);
        } catch (Exception ignored) {
            log.debug("OTP field focus tap failed — screen may auto-focus the input");
        }
        platformActions.enterDigits(otp);
    }

    @Step("Tap Done on Thank You screen")
    public void tapDone() {
        tap(DONE_BTN);
    }

    // ══════════════════════════════════════════════════
    //  STATE QUERIES (no assertions — booleans/text for the test)
    // ══════════════════════════════════════════════════

    public String getHeaderText() {
        return getText(CHANGE_PASSCODE_HEADER);
    }

    public boolean isChangePasscodeHeaderDisplayed() {
        return isPresent(CHANGE_PASSCODE_HEADER, 20);
    }

    public boolean isSettingsScreenDisplayed() {
        return isPresent(SETTINGS_LABEL, 20);
    }

    // Current passcode screen
    public boolean isCurrentPasscodeScreenDisplayed() {
        return isPresent(CURRENT_PASSCODE_TITLE, 20);
    }

    public String getCurrentPasscodeTitle() {
        return getText(CURRENT_PASSCODE_TITLE);
    }

    public String getCurrentPasscodeSubtitle() {
        return getText(CURRENT_PASSCODE_SUBTITLE);
    }

    // New passcode screen
    public boolean isNewPasscodeScreenDisplayed() {
        // RN transition + backend passcode-policy check can be slow on remote devices.
        return isPresent(NEW_PASSCODE_TITLE, 45);
    }

    public String getNewPasscodeTitle() {
        return getText(NEW_PASSCODE_TITLE);
    }

    public String getNewPasscodeSubtitle() {
        return getText(NEW_PASSCODE_SUBTITLE);
    }

    // Confirm new passcode screen
    public boolean isConfirmPasscodeScreenDisplayed() {
        // Entering a new passcode that differs from the current one triggers a backend
        // validation; on remote devices this transition has been observed to take >30s.
        return isPresent(CONFIRM_PASSCODE_TITLE, 60);
    }

    public String getConfirmPasscodeTitle() {
        return getText(CONFIRM_PASSCODE_TITLE);
    }

    public String getConfirmPasscodeSubtitle() {
        return getText(CONFIRM_PASSCODE_SUBTITLE);
    }

    // Verification (OTP) screen
    public boolean isOtpScreenDisplayed() {
        // Confirming the passcode triggers a server-side OTP send before this screen renders.
        return isPresent(OTP_SCREEN_TITLE, 45);
    }

    public String getDidntGetCodeText() {
        return getText(DIDNT_GET_CODE_TEXT);
    }

    // Thank You screen
    /**
     * The Thank You screen is transient: the app commits the passcode change and logs the
     * user out within a couple of seconds, so individual element reads race against the
     * dismiss (StaleElementReferenceException). Capture the whole screen as a static XML
     * snapshot the instant the title is detected so the test can assert its texts atomically.
     */
    private String thankYouSource = "";

    public boolean isThankYouScreenDisplayed() {
        boolean present = isPresent(THANK_YOU_TITLE, 30);
        if (present) {
            try {
                thankYouSource = driver.getPageSource();
            } catch (Exception e) {
                log.warn("Could not capture Thank You snapshot: {}", e.getMessage());
            }
        }
        return present;
    }

    /** Static XML snapshot of the Thank You screen captured by {@link #isThankYouScreenDisplayed()}. */
    public String getThankYouSnapshot() {
        return thankYouSource;
    }

    public String getThankYouTitle() {
        return getText(THANK_YOU_TITLE);
    }

    public String getThankYouSubtitle() {
        return getText(THANK_YOU_SUBTITLE);
    }

    public String getDoneButtonText() {
        return getText(DONE_TEXT);
    }
}
