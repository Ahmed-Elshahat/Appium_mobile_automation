package com.urpay.pages.auth;

import java.util.concurrent.ThreadLocalRandom;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;
import com.urpay.utils.DatePickerHandler;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Registration Wizard — covers every screen of the in-app registration journey:
 *   Landing → Register → enter mobile+ID → OTP (OtpPage) → [Visitor: DOB] →
 *   Create Passcode → Confirm Passcode → Accept Terms → Done → Dashboard.
 *
 * Katalon source: Object Repository/android/Dev/RegistrationPage/
 */
public class RegistrationWizardPage extends BasePage {

    // ── Registration entry form ──────────────────────────────────────
    private static final By MOBILE_FIELD =
            AppiumBy.accessibilityId("testID-input-direct-mobile");

    private static final By ID_FIELD =
            AppiumBy.accessibilityId("testID-input-direct-id");

    // Next / Submit — testID-primary--main (obfuscated middle segment = empty) OR text 'Next'
    // Also covers the Register landing button since both share testID-primary--main on SIT APK.
    private static final By NEXT_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary--main'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')"
            + " or @text='Next' or @text='Register']");

    // ── Terms + Privacy checkboxes ───────────────────────────────────
    private static final By TERMS_CHECKBOX = AppiumBy.xpath(
            "(//*[@content-desc='testID-check-box-main'])[1]");

    private static final By PRIVACY_CHECKBOX = AppiumBy.xpath(
            "(//*[@content-desc='testID-check-box-main'])[2]");

    // ── Passcode setup (Create / Confirm) ────────────────────────────
    // The custom RN passCode component; clickable=true = inner wrapper that focuses the input.
    private static final By PASSCODE_SCREEN_MARKER = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-passCode.screen')"
            + " or @text='Create your passcode' or @text='Set your passcode'"
            + " or @text='Confirm your passcode' or @text='Confirm passcode']");

    private static final By PASSCODE_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-passCode.screen' and @clickable='true']");

    // ── Date of birth screen (shown for ALL tiers in registration, Step 3/5) ────────────────
    // Prefer the stable accessibility id used by the app's DOB control; fall back to generic
    // DatePicker/text matches when the cloud build obfuscates the wrapper id.
    private static final By DOB_PICKER_STABLE =
            AppiumBy.accessibilityId("testID-DatePicker.dateOfBirth");

        private static final By DOB_PICKER_CLICKABLE_ROW = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-DatePicker.') and @clickable='true']");

    private static final By DOB_PICKER = AppiumBy.xpath(
            "//*[starts-with(@content-desc,'testID-DatePicker.')"
            + " or @text='Date of birth' or @text='Date Of Birth' or @text='Date of Birth'"
            + " or (@clickable='true' and .//*[@text='Date of birth'])]");

    private static final By DOB_SCREEN_TITLE = AppiumBy.xpath(
            "//*[@text='Date Of Birth' or @text='Date of Birth']");

    private static final By DOB_HIJRI_TOGGLE_TEXT = AppiumBy.xpath(
            "//*[@text='Switch to Hijri']");

    // Fallbacks for builds where the DatePicker wrapper isn't directly clickable.
    private static final By DOB_PICKER_ROW_FALLBACK = AppiumBy.xpath(
            "//*[(@text='Date of birth' or @text='Date Of Birth' or @text='Date of Birth')"
            + "/ancestor::*[@clickable='true'][1]]");

    private static final By DOB_PICKER_TEXT_FALLBACK = AppiumBy.xpath(
            "//*[@text='Date of birth' or @text='Date Of Birth' or @text='Date of Birth']");

        private static final By DOB_CALENDAR_ICON = AppiumBy.xpath(
            "//*[contains(@content-desc,'.Calender') or contains(@content-desc,'.Calendar')]");

            private static final By ANY_NATIVE_DATE_PICKER = AppiumBy.xpath(
                "//android.widget.NumberPicker | //android.widget.DatePicker | //android.widget.Button[@resource-id='android:id/button1']");

    // Calendar OK button (native Android date spinner confirmation)
    private static final By CALENDAR_OK_BTN =
            AppiumBy.xpath("//android.widget.Button[@resource-id='android:id/button1']");

    // ── Done / Finish screen ─────────────────────────────────────────
    // testID-primary-buttonAction-main; middle segment 'buttonAction' is obfuscated on cloud.
    private static final By DONE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-buttonAction-main'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')"
            + " or @text='Done']");

    // Finish screen marker — any distinctive 'Done' or success text
    private static final By FINISH_SCREEN_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-buttonAction-main'"
            + " or @text='Done' or @text='Get Started' or @text='Welcome']");

    // Nafath "Verification Requirements" screen shown after passcode confirmation
    private static final By NAFATH_SCREEN_MARKER = AppiumBy.xpath(
            "//*[@text='Verification Requirements' or @text=\"You're all set!\""
            + " or @text='Nafath']");

    // Nafath number-match screen shown after tapping Next on the requirements screen
    private static final By NAFATH_NUMBER_SCREEN_MARKER = AppiumBy.xpath(
            "//*[@text='Nafath Verification' or @text='Open Nafath App'"
            + " or @content-desc='testID-Steps.42a2f0a1-933a-4201-8c6e-46536e98761c']");

        // Absher consent screen shown after Nafath in some builds.
        private static final By ABSHER_CONSENT_SCREEN_MARKER = AppiumBy.xpath(
            "//*[@text='Absher Verification' or @text='Absher Consent']");

        // Top-right close icon used to dismiss Absher consent.
        private static final By ABSHER_CLOSE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-right-icon-0' or @content-desc='testID-right-icon-item']");

                // Optional post-registration card screens.
                private static final By CARD_PIN_SETUP_MARKER = AppiumBy.xpath(
                    "//*[@text='Card PIN Code' or @text='Setup PIN Code'"
                    + " or contains(@content-desc,'testID-passCode.screen')]");

                private static final By CARD_ISSUANCE_SCREEN_MARKER = AppiumBy.xpath(
                    "//*[@text='Request Card' or @text='Card Issuance' or @text='Card issuance']");

                // Clickable Skip CTA on Request Card screen (testID-secondary-*-main obfuscated per build).
                private static final By CARD_ISSUANCE_SKIP_BTN = AppiumBy.xpath(
                    "//*[@content-desc='testID-secondary-ETY-main'"
                    + " or (starts-with(@content-desc,'testID-secondary-')"
                    + " and substring(@content-desc,string-length(@content-desc)-4)='-main')"
                    + " or (@clickable='true' and .//*[@text='Skip'])]");

                // Top-right close icon used on Setup PIN screens.
                private static final By CARD_PIN_CLOSE_BTN = AppiumBy.xpath(
                    "//*[@content-desc='testID-right-icon-0' or @content-desc='testID-right-icon-item']");

            // KYC personal information screen (post-Nafath on some runs)
            private static final By KYC_PERSONAL_INFO_MARKER = AppiumBy.xpath(
                "//*[@text='Personal Information'"
                + " or @content-desc='testID-multi-select-employmentStatus'"
                + " or @content-desc='testID-input-direct-employer']");

            private static final By KYC_JOB_SECTOR_DROPDOWN =
                AppiumBy.accessibilityId("testID-multi-select-employmentStatus");

            private static final By KYC_EMPLOYER_INPUT =
                AppiumBy.accessibilityId("testID-input-direct-employer");

            private static final By KYC_INCOME_SOURCE_DROPDOWN =
                AppiumBy.accessibilityId("testID-multi-select-basicIncomeSource");

            private static final By KYC_JOB_CATEGORY_DROPDOWN =
                AppiumBy.accessibilityId("testID-multi-select-jobCategory");

            private static final By KYC_INCOME_RANGE_DROPDOWN =
                AppiumBy.accessibilityId("testID-multi-select-incomeRange");

            private static final By KYC_SAVE_BTN = AppiumBy.xpath(
                "//*[@content-desc='testID-primary--main' or @text='Save']");

            private static final By KYC_FIRST_OPTION = AppiumBy.xpath(
                "//*[@content-desc='testID-search-item-0'"
                + " or @content-desc='testID-radio-item-0'"
                + " or contains(@content-desc,'testID-search-item-')"
                + " or contains(@content-desc,'testID-radio-item-')][1]");

    // ══════════════════════════════════════════════════
    //  SCREEN CHECKS
    // ══════════════════════════════════════════════════

    public boolean isRegistrationFormLoaded() {
        return waitUtils.isPresent(MOBILE_FIELD, 20);
    }

    public boolean isRegistrationFormLoaded(long timeoutSec) {
        return waitUtils.isPresent(MOBILE_FIELD, timeoutSec);
    }

    public boolean isPasscodeScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(PASSCODE_SCREEN_MARKER, timeoutSec);
    }

    public boolean isDobScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(DOB_SCREEN_TITLE, timeoutSec)
                || waitUtils.isPresent(DOB_HIJRI_TOGGLE_TEXT, 2)
                || waitUtils.isPresent(DOB_PICKER_STABLE, 2)
                || waitUtils.isPresent(DOB_PICKER, 2);
    }

    public boolean isTermsScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(TERMS_CHECKBOX, timeoutSec);
    }

    public boolean isFinishScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(FINISH_SCREEN_MARKER, timeoutSec);
    }

    public boolean isNafathVerificationScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(NAFATH_SCREEN_MARKER, timeoutSec);
    }

    public boolean isNafathNumberScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(NAFATH_NUMBER_SCREEN_MARKER, timeoutSec);
    }

    public boolean isAbsherConsentScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(ABSHER_CONSENT_SCREEN_MARKER, timeoutSec);
    }

    public boolean isKycPersonalInformationScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(KYC_PERSONAL_INFO_MARKER, timeoutSec);
    }

    public boolean isCardPinSetupScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(CARD_PIN_SETUP_MARKER, timeoutSec);
    }

    public boolean isCardIssuanceScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(CARD_ISSUANCE_SCREEN_MARKER, timeoutSec);
    }

    /**
     * Wait on the Nafath number-match screen until it disappears (SIT auto-verifies in ~30s).
     * Returns true if the screen cleared within the timeout, false if still showing.
     */
    @Step("Wait for Nafath auto-verification to complete")
    public boolean waitUntilNafathAutoVerifies(long timeoutSec) {
        log.info("Waiting up to {}s for Nafath auto-verification...", timeoutSec);
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (!waitUtils.isPresent(NAFATH_NUMBER_SCREEN_MARKER, 3)) {
                log.info("Nafath number-match screen cleared — verification complete");
                return true;
            }
            log.info("Nafath number-match screen still showing — waiting...");
        }
        log.warn("Nafath auto-verification did not complete within {}s", timeoutSec);
        return false;
    }

    /**
     * Dismiss Absher consent screen by tapping the top-right X button.
     * Returns true if the screen is gone, false if still visible.
     */
    @Step("Dismiss Absher consent screen by tapping X")
    public boolean dismissAbsherConsentIfPresent(long timeoutSec) {
        if (!isAbsherConsentScreenDisplayed(timeoutSec)) {
            return true;
        }
        log.info("Absher consent detected — dismissing via top-right X");
        for (int attempt = 1; attempt <= 3 && isAbsherConsentScreenDisplayed(3); attempt++) {
            log.info("Absher dismiss attempt {}", attempt);
            waitUtils.waitForClickable(ABSHER_CLOSE_BTN, 10).click();
        }
        boolean dismissed = !isAbsherConsentScreenDisplayed(5);
        if (!dismissed) {
            log.warn("Absher consent screen still visible after dismiss attempts");
        }
        return dismissed;
    }

    /**
     * Fill the KYC Personal Information form with random-valid test values and submit.
     * Returns true when the KYC screen is no longer visible after save attempts.
     */
    @Step("Complete KYC Personal Information with random values")
    public boolean completeKycPersonalInformationRandomly(long timeoutSec) {
        if (!isKycPersonalInformationScreenDisplayed(timeoutSec)) {
            return true;
        }

        log.info("KYC Personal Information detected — filling required fields");
        selectFirstOptionFromDropdown(KYC_JOB_SECTOR_DROPDOWN, "Job Sector");
        type(KYC_EMPLOYER_INPUT, randomEmployerName());
        hideKeyboard();
        selectFirstOptionFromDropdown(KYC_INCOME_SOURCE_DROPDOWN, "Income Source");
        selectFirstOptionFromDropdown(KYC_JOB_CATEGORY_DROPDOWN, "Job Category");
        selectFirstOptionFromDropdown(KYC_INCOME_RANGE_DROPDOWN, "Income Range");

        // Save button sits below the fold — scroll down to reveal it before tapping.
        swipeUp();
        for (int attempt = 1; attempt <= 3 && isKycPersonalInformationScreenDisplayed(4); attempt++) {
            log.info("KYC Save attempt {}", attempt);
            try {
                var saveEls = waitUtils.findQuick(KYC_SAVE_BTN, 5);
                if (!saveEls.isEmpty()) {
                    saveEls.get(0).click();
                } else {
                    // Coordinate fallback: tap ~92% down the screen where Save is always rendered.
                    var sz = driver.manage().window().getSize();
                    tapAtCoordinates(sz.getWidth() / 2, (int) (sz.getHeight() * 0.92));
                    log.info("KYC Save: coordinate tap at 92% height");
                }
            } catch (Exception saveFailure) {
                log.warn("KYC Save tap failed on attempt {}: {}", attempt, saveFailure.getMessage());
            }
        }

        boolean completed = !isKycPersonalInformationScreenDisplayed(8);
        if (!completed) {
            log.warn("KYC screen still visible after random-fill save attempts");
        }
        return completed;
    }

    /**
     * Some accounts are routed to an optional card PIN setup screen.
     * Skip/dismiss it via Skip or top-right close icon.
     */
    @Step("Skip optional Card PIN setup screen if present")
    public boolean skipCardPinSetupIfPresent(long timeoutSec) {
        if (!isCardPinSetupScreenDisplayed(timeoutSec) && !isCardIssuanceScreenDisplayed(timeoutSec)) {
            return true;
        }
        log.info("Optional card screen detected — attempting to skip/dismiss");
        for (int attempt = 1; attempt <= 4
                && (isCardPinSetupScreenDisplayed(2) || isCardIssuanceScreenDisplayed(2)); attempt++) {
            log.info("Card screen skip attempt {}", attempt);
            try {
                if (isCardIssuanceScreenDisplayed(1)) {
                    tap(CARD_ISSUANCE_SKIP_BTN, 8);
                    // Fallback: some builds miss the RN click event on first tap.
                    if (isCardIssuanceScreenDisplayed(2)) {
                        var skipText = waitUtils.waitForVisible(AppiumBy.xpath("//*[@text='Skip']"), 3);
                        var r = skipText.getRect();
                        tapAtCoordinates(r.getX() + (r.getWidth() / 2), r.getY() + (r.getHeight() / 2));
                        log.info("Coordinate-tapped Skip text center as fallback");
                    }
                } else {
                    tap(CARD_PIN_CLOSE_BTN, 8);
                }
            } catch (Exception e) {
                log.warn("Card screen skip tap failed on attempt {}: {}", attempt, e.getMessage());
            }
        }
        boolean skipped = !isCardPinSetupScreenDisplayed(6) && !isCardIssuanceScreenDisplayed(6);
        if (!skipped) {
            log.warn("Card screen still visible after skip attempts");
        }
        return skipped;
    }

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Enter mobile number: {mobile}")
    public void enterMobileNumber(String mobile) {
        waitUtils.waitForClickable(MOBILE_FIELD, 20).click();
        type(MOBILE_FIELD, mobile);
    }

    @Step("Enter ID / POI number: {id}")
    public void enterIdNumber(String id) {
        waitUtils.waitForClickable(ID_FIELD, 15).click();
        type(ID_FIELD, id);
    }

    @Step("Tap Next on registration form")
    public void tapNext() {
        waitUtils.waitForClickable(NEXT_BTN, 15).click();
    }

    @Step("Accept Terms of Service checkbox")
    public void acceptTerms() {
        waitUtils.waitForClickable(TERMS_CHECKBOX, 15).click();
    }

    @Step("Accept Privacy Policy checkbox")
    public void acceptPrivacyPolicy() {
        waitUtils.waitForClickable(PRIVACY_CHECKBOX, 10).click();
    }

    /**
     * Accept Terms + Privacy Policy and submit registration (later-screen variant).
     * If only one checkbox is present the second tap is a no-op (best-effort).
     */
    @Step("Accept Terms + Privacy Policy and submit registration")
    public void acceptTermsAndSubmit() {
        acceptTerms();
        if (waitUtils.isPresent(PRIVACY_CHECKBOX, 3)) {
            acceptPrivacyPolicy();
        }
        tapNext();
    }

    /**
     * Best-effort accept of Terms + Privacy on the credentials entry form.
     * On the current build these checkboxes sit on the same screen as mobile/ID.
     * If they are absent (different build / later screen) this is a no-op.
     */
    @Step("Accept Terms + Privacy Policy on credentials form (if present)")
    public void acceptTermsAndPrivacyIfPresent() {
        if (waitUtils.isPresent(TERMS_CHECKBOX, 5)) {
            tap(TERMS_CHECKBOX);
            log.info("Accepted Terms checkbox on credentials form");
        }
        if (waitUtils.isPresent(PRIVACY_CHECKBOX, 3)) {
            tap(PRIVACY_CHECKBOX);
            log.info("Accepted Privacy Policy checkbox on credentials form");
        }
    }

    /**
     * Set a new passcode on the Create/Confirm Passcode screen.
     * Focuses the hidden RN TextInput first (same mechanism as login passcode entry).
     */
    @Step("Enter new passcode on Create Passcode screen")
    public void enterNewPasscode(String passcode) {
        waitForPasscodeScreen(30);
        focusPasscodeInput();
        platformActions.clearDigits(passcode.length() + 2);
        platformActions.enterDigits(passcode);
        log.info("New passcode entered on Create Passcode screen");
    }

    /**
     * Confirm passcode on the Confirm Passcode screen.
     * Re-focuses the hidden input before entry.
     */
    @Step("Enter passcode on Confirm Passcode screen")
    public void confirmPasscode(String passcode) {
        waitForPasscodeScreen(20);
        focusPasscodeInput();
        platformActions.clearDigits(passcode.length() + 2);
        platformActions.enterDigits(passcode);
        log.info("Passcode confirmed on Confirm Passcode screen");
    }

    // ── Visitor (BOR) DOB ────────────────────────────────────────────

    /**
     * Enter date of birth via the native Android date spinner.
     * Opens the DatePicker, drives the spinner to the target date, confirms, then taps Next.
     */
    @Step("Enter date of birth: {month} {day} {year}")
    public void enterDateOfBirth(String month, String day, String year) {
        boolean openedPickerUi = false;
        for (int attempt = 1; attempt <= 3 && !openedPickerUi; attempt++) {
            try {
                waitUtils.waitForClickable(DOB_PICKER_STABLE, 4).click();
                log.info("Tapped stable DOB picker on attempt {}", attempt);
            } catch (Exception stableFailure) {
                log.warn("Stable DOB picker tap failed on attempt {}: {}", attempt, stableFailure.getMessage());
                try {
                    waitUtils.waitForClickable(DOB_PICKER_CLICKABLE_ROW, 4).click();
                    log.info("Tapped clickable DOB row on attempt {}", attempt);
                } catch (Exception rowFailure) {
                    log.warn("Clickable DOB row tap failed on attempt {}: {}", attempt, rowFailure.getMessage());
                    try {
                        waitUtils.waitForClickable(DOB_PICKER, 4).click();
                        log.info("Tapped generic DOB picker on attempt {}", attempt);
                    } catch (Exception genericFailure) {
                        log.warn("Generic DOB picker tap failed on attempt {}: {}", attempt, genericFailure.getMessage());
                        try {
                            waitUtils.waitForClickable(DOB_PICKER_ROW_FALLBACK, 4).click();
                        } catch (Exception ignored) {
                            waitUtils.waitForClickable(DOB_PICKER_TEXT_FALLBACK, 4).click();
                        }
                    }
                }
            }

            if (waitUtils.isPresent(ANY_NATIVE_DATE_PICKER, 2)) {
                openedPickerUi = true;
                break;
            }

            if (waitUtils.isPresent(DOB_CALENDAR_ICON, 2)) {
                try {
                    waitUtils.waitForClickable(DOB_CALENDAR_ICON, 3).click();
                    log.info("Tapped DOB calendar icon fallback on attempt {}", attempt);
                } catch (Exception iconFailure) {
                    log.warn("DOB calendar icon tap failed on attempt {}: {}", attempt, iconFailure.getMessage());
                }
            }

            openedPickerUi = waitUtils.isPresent(ANY_NATIVE_DATE_PICKER, 2);
        }

        new DatePickerHandler(driver).selectDate(month, day, year);
        waitUtils.waitForClickable(CALENDAR_OK_BTN, 10).click();
        log.info("Date of birth set: {} {} {}", month, day, year);
    }

    @Step("Tap Next on DOB screen")
    public void tapDobNext() {
        // The first tap can be swallowed right after the native dialog closes; retry once if the
        // DOB screen is still present and we did not advance.
        waitUtils.waitForClickable(NEXT_BTN, 15).click();
        if (isDobScreenDisplayed(3) && !isPasscodeScreenDisplayed(3)) {
            log.warn("DOB Next tap did not advance on the first attempt — re-tapping Next once");
            waitUtils.waitForClickable(NEXT_BTN, 10).click();
        }
    }

    // ── Done / Finish ────────────────────────────────────────────────

    @Step("Tap Done on registration finish screen")
    public void tapDone() {
        waitUtils.waitForClickable(DONE_BTN, 20).click();
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void waitForPasscodeScreen(long timeoutSec) {
        if (!waitUtils.isPresent(PASSCODE_SCREEN_MARKER, timeoutSec)) {
            log.warn("Passcode setup screen not detected within {}s", timeoutSec);
        }
    }

    private void focusPasscodeInput() {
        try {
            var els = driver.findElements(PASSCODE_INPUT);
            if (!els.isEmpty()) {
                els.get(els.size() - 1).click();
                log.info("Clicked passCode.screen wrapper to focus input");
                return;
            }
        } catch (Exception e) {
            log.warn("Passcode wrapper click failed: {}", e.getMessage());
        }
        // Coordinate fallback: tap the centre of the passcode boxes region (~34% down)
        try {
            var size = driver.manage().window().getSize();
            int x = size.getWidth() / 2;
            int y = (int) (size.getHeight() * 0.34);
            var finger = new org.openqa.selenium.interactions.PointerInput(
                    org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "f");
            var seq = new org.openqa.selenium.interactions.Sequence(finger, 0)
                    .addAction(finger.createPointerMove(java.time.Duration.ZERO,
                            org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y))
                    .addAction(finger.createPointerDown(0))
                    .addAction(new org.openqa.selenium.interactions.Pause(finger, java.time.Duration.ofMillis(80)))
                    .addAction(finger.createPointerUp(0));
            driver.perform(java.util.Collections.singletonList(seq));
            log.info("Coordinate tap to focus passcode input at ({},{})", x, y);
        } catch (Exception e) {
            log.warn("Coordinate focus tap failed: {}", e.getMessage());
        }
    }

    private void selectFirstOptionFromDropdown(By dropdown, String label) {
        try {
            tap(dropdown, 10);
            tap(KYC_FIRST_OPTION, 10);
            log.info("Selected first option for {}", label);
        } catch (Exception e) {
            log.warn("Failed to select {} dropdown option: {}", label, e.getMessage());
        }
    }

    private String randomEmployerName() {
        int suffix = ThreadLocalRandom.current().nextInt(100, 999);
        return "URPAY AUTO " + suffix;
    }
}
