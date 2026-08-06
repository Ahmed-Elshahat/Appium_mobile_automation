package com.urpay.pages.auth;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

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

    // ── Visitor (BOR) — date of birth screen ────────────────────────
    private static final By DOB_FIELD =
            AppiumBy.accessibilityId("testID-input-direct-dateOfBirth");

    // Validate-date / Next on the DOB screen — testID-primary-validateDate-main (middle=validateDate)
    private static final By VALIDATE_DATE_BTN = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-validateDate-main'"
            + " or (starts-with(@content-desc,'testID-primary-')"
            + " and substring(@content-desc,string-length(@content-desc)-4)='-main')"
            + " or @text='Next' or @text='Validate']");

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

    // ══════════════════════════════════════════════════
    //  SCREEN CHECKS
    // ══════════════════════════════════════════════════

    public boolean isRegistrationFormLoaded() {
        return waitUtils.isPresent(MOBILE_FIELD, 20);
    }

    public boolean isPasscodeScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(PASSCODE_SCREEN_MARKER, timeoutSec);
    }

    public boolean isDobScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(DOB_FIELD, timeoutSec);
    }

    public boolean isTermsScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(TERMS_CHECKBOX, timeoutSec);
    }

    public boolean isFinishScreenDisplayed(long timeoutSec) {
        return waitUtils.isPresent(FINISH_SCREEN_MARKER, timeoutSec);
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
     * Accept both Terms and Privacy Policy checkboxes, then tap Next to submit registration.
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
     * Enter the date of birth in the registration DOB text field.
     * The field accepts the date as text (format YYYY-MM-DD or DD/MM/YYYY — use YYYY-MM-DD
     * as that is what the BE report and config use, e.g. {@code 1994-01-22}).
     */
    @Step("Enter date of birth: {dob}")
    public void enterDateOfBirth(String dob) {
        waitUtils.waitForClickable(DOB_FIELD, 20).click();
        type(DOB_FIELD, dob);
        log.info("Date of birth entered: {}", dob);
    }

    @Step("Tap Validate Date / Next on DOB screen")
    public void tapValidateDateNext() {
        waitUtils.waitForClickable(VALIDATE_DATE_BTN, 15).click();
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
}
