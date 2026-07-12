package com.urpay.pages.settings;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Rating/Feedback page — handles the single-question NPS survey:
 *   Screen:  "How likely recommend Urpay …?" NPS rating (0-10) → Submit
 *   Success: "Thank you for taking the survey" → Close
 *
 * Katalon source: Object Repository/android/WalletVas/Rating/
 */
public class RatingPage extends BasePage {

    // ── Navigation ───────────────────────────────────
    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

    private static final By RATE_URPAY_BTN =
            AppiumBy.accessibilityId("testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.RateUs");

    // ── NPS Survey Screen ────────────────────────────
    private static final By QUESTION_TEXT =
            AppiumBy.xpath("//*[contains(@text,'recommend Urpay')]");

    private static final By NOT_LIKELY_TEXT =
            AppiumBy.xpath("//*[@text='Not Likely']");

    private static final By EXTREMELY_LIKELY_TEXT =
            AppiumBy.xpath("//*[@text='Extremely Likely']");

    private static final By SUBMIT_BUTTON =
            AppiumBy.accessibilityId("Submit");

    // ── Success Screen ───────────────────────────────
    private static final By THANK_YOU_TITLE =
            AppiumBy.xpath("//*[@text='Thank you']");

    private static final By THANK_YOU_MESSAGE =
            AppiumBy.xpath("//*[contains(@text,'Thank you for taking the survey')]");

    private static final By CLOSE_BUTTON =
            AppiumBy.accessibilityId("Close");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Rate Urpay from More menu")
    public void navigateToRateUrpay() {
        tap(MORE_NAV);
        waitUtils.waitForVisible(RATE_URPAY_BTN, 15);
        tap(RATE_URPAY_BTN);
    }

    // ══════════════════════════════════════════════════
    //  NPS SURVEY SCREEN
    // ══════════════════════════════════════════════════

    @Step("Get NPS question text")
    public String getQuestionText() {
        return getText(QUESTION_TEXT);
    }

    @Step("Get 'Not Likely' label text")
    public String getNotLikelyText() {
        return getText(NOT_LIKELY_TEXT);
    }

    @Step("Get 'Extremely Likely' label text")
    public String getExtremelyLikelyText() {
        return getText(EXTREMELY_LIKELY_TEXT);
    }

    @Step("Select NPS rating {rating}")
    public void selectRating(String rating) {
        tap(AppiumBy.accessibilityId(rating));
    }

    @Step("Tap Submit button")
    public void tapSubmit() {
        tap(SUBMIT_BUTTON);
    }

    public boolean isFirstScreenLoaded() {
        // Identify the NPS screen by its stable scale label / question rather than the personalised
        // greeting. Either one confirms the survey rendered.
        return isPresent(NOT_LIKELY_TEXT, 15)
                || isPresent(QUESTION_TEXT, 2);
    }

    /** Submit is disabled until an NPS score is selected — lets the test assert the selection took. */
    public boolean isSubmitEnabled() {
        List<WebElement> els = waitUtils.findQuick(SUBMIT_BUTTON, 5);
        return !els.isEmpty() && els.get(0).isEnabled();
    }

    // ══════════════════════════════════════════════════
    //  SUCCESS SCREEN
    // ══════════════════════════════════════════════════

    @Step("Get thank-you title text")
    public String getThankYouTitle() {
        return getText(THANK_YOU_TITLE);
    }

    @Step("Get thank-you message text")
    public String getThankYouMessage() {
        return getText(THANK_YOU_MESSAGE);
    }

    @Step("Tap Close button")
    public void tapClose() {
        tap(CLOSE_BUTTON);
    }

    public boolean isSuccessScreenLoaded() {
        return isPresent(THANK_YOU_MESSAGE, 15);
    }
}
