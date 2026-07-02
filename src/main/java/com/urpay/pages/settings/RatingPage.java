package com.urpay.pages.settings;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Rating/Feedback page — handles the multi-screen rating flow:
 *   Screen 1: NPS rating (0-10)
 *   Screen 2: Factor selection (Urpay Cards, Money Transfer, etc.)
 *   Screen 3: Comments text field
 *   Success: Submission confirmation
 *
 * Katalon source: Object Repository/android/WalletVas/Rating/
 */
public class RatingPage extends BasePage {

    // ── Navigation ───────────────────────────────────
    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

    private static final By RATE_URPAY_BTN =
            AppiumBy.accessibilityId("testID-IconView.dddbe7a7-5de7-48e0-8d3f-90dd4f5eb995.RateUs");

    // ── First Feedback Screen (NPS) ──────────────────
    private static final By HEADER_TEXT_FIRST_SCREEN =
            AppiumBy.xpath("//*[contains(@text,'Hello')]");

    private static final By TEXT_BELOW_HEADER =
            AppiumBy.accessibilityId("testID-Text.65ee6655-f13b-4d89-af7a-66c664b97c69");

    private static final By NOT_LIKELY_TEXT =
            AppiumBy.accessibilityId("testID-q1.lowerLimit");

    private static final By EXTREMELY_LIKELY_TEXT =
            AppiumBy.accessibilityId("testID-q1.upperLimit");

    private static final By RATING_BTN_6 =
            AppiumBy.accessibilityId("6");

    private static final By NEXT_BUTTON =
            AppiumBy.accessibilityId("Next");

    private static final By SKIP_BTN =
            AppiumBy.accessibilityId("testID-tertiary-skipSurvey-main");

    // ── Second Feedback Screen (Factors) ─────────────
    private static final By HEADER_TEXT_SECOND_SCREEN =
            AppiumBy.accessibilityId("testID-Text.9c75cabd-3550-4f4b-b1c2-0e160fa7d94b");

    private static final By URPAY_CARDS_OPTION =
            AppiumBy.accessibilityId("Urpay Cards");

    private static final By MONEY_TRANSFER_OPTION =
            AppiumBy.xpath("//*[@content-desc='Money Transfer' or @text='Money Transfer']");

    private static final By URPAY_APP_OPTION =
            AppiumBy.xpath("//*[@content-desc='Urpay App' or @text='Urpay App']");

    private static final By URPAY_STORE_OPTION =
            AppiumBy.xpath("//*[@content-desc='Urpay Store' or @text='Urpay Store']");

    private static final By CUSTOMER_SERVICE_OPTION =
            AppiumBy.xpath("//*[@content-desc='Customer Service' or @text='Customer Service']");

    // ── Third Feedback Screen (Comments) ─────────────
    private static final By HEADER_TEXT_THIRD_SCREEN =
            AppiumBy.xpath("//*[contains(@text,'Do you have any comments?')]");

    private static final By TEXT_FIELD =
            AppiumBy.xpath("//*[contains(@text,'Enter Text answer')]");

    private static final By SUBMIT_BUTTON =
            AppiumBy.accessibilityId("Submit");

    // ── Success Screen ───────────────────────────────
    private static final By SUBMITTED_TEXT =
            AppiumBy.accessibilityId("testID-total-transfer");

    private static final By TEXT_BELOW_SUBMITTED =
            AppiumBy.accessibilityId("testID-total-transfer-message");

    private static final By DONE_BUTTON =
            AppiumBy.accessibilityId("testID-primary-action-main");

    private static final By CLOSE_BUTTON =
            AppiumBy.xpath("//*[contains(@text,'Close')]");

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
    //  FIRST SCREEN (NPS Rating)
    // ══════════════════════════════════════════════════

    @Step("Get first screen header text")
    public String getFirstScreenHeader() {
        return getText(HEADER_TEXT_FIRST_SCREEN);
    }

    @Step("Get text below header (question text)")
    public String getQuestionText() {
        return getText(TEXT_BELOW_HEADER);
    }

    @Step("Get 'Not Likely' label text")
    public String getNotLikelyText() {
        return getText(NOT_LIKELY_TEXT);
    }

    @Step("Get 'Extremely Likely' label text")
    public String getExtremelyLikelyText() {
        return getText(EXTREMELY_LIKELY_TEXT);
    }

    @Step("Select rating 6")
    public void selectRating6() {
        tap(RATING_BTN_6);
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(NEXT_BUTTON);
    }

    @Step("Tap Skip button")
    public void tapSkip() {
        tap(SKIP_BTN);
    }

    public boolean isFirstScreenLoaded() {
        // Identify the NPS screen by its stable scale markers (0-10 buttons and the lower/upper
        // limit labels) rather than the personalised "Hello …" greeting, whose exact text varies by
        // user/locale and is the weakest signal. Any one of these confirms the first screen rendered.
        return isPresent(NOT_LIKELY_TEXT, 15)
                || isPresent(RATING_BTN_6, 2)
                || isPresent(HEADER_TEXT_FIRST_SCREEN, 2);
    }

    public boolean isNextButtonVisible() {
        return isPresent(NEXT_BUTTON, 5);
    }

    // ══════════════════════════════════════════════════
    //  SECOND SCREEN (Factor Selection)
    // ══════════════════════════════════════════════════

    @Step("Get second screen header text")
    public String getSecondScreenHeader() {
        return getText(HEADER_TEXT_SECOND_SCREEN);
    }

    @Step("Get Urpay Cards option text")
    public String getUrpayCardsText() {
        return getText(URPAY_CARDS_OPTION);
    }

    @Step("Get Money Transfer option text")
    public String getMoneyTransferText() {
        return getText(MONEY_TRANSFER_OPTION);
    }

    @Step("Get Urpay App option text")
    public String getUrpayAppText() {
        return getText(URPAY_APP_OPTION);
    }

    @Step("Get Urpay Store option text")
    public String getUrpayStoreText() {
        return getText(URPAY_STORE_OPTION);
    }

    @Step("Get Customer Service option text")
    public String getCustomerServiceText() {
        return getText(CUSTOMER_SERVICE_OPTION);
    }

    @Step("Select 'Urpay App' factor")
    public void selectUrpayAppFactor() {
        tap(URPAY_APP_OPTION);
    }

    public boolean isSecondScreenLoaded() {
        return isPresent(HEADER_TEXT_SECOND_SCREEN, 15);
    }

    // ══════════════════════════════════════════════════
    //  THIRD SCREEN (Comments)
    // ══════════════════════════════════════════════════

    @Step("Get third screen header text")
    public String getThirdScreenHeader() {
        return getText(HEADER_TEXT_THIRD_SCREEN);
    }

    @Step("Enter comment text")
    public void enterComment(String text) {
        tap(TEXT_FIELD);
        type(TEXT_FIELD, text);
    }

    @Step("Tap Submit button")
    public void tapSubmit() {
        tap(SUBMIT_BUTTON);
    }

    public boolean isThirdScreenLoaded() {
        return isPresent(HEADER_TEXT_THIRD_SCREEN, 15);
    }

    // ══════════════════════════════════════════════════
    //  SUCCESS SCREEN
    // ══════════════════════════════════════════════════

    @Step("Get submitted confirmation text")
    public String getSubmittedText() {
        return getText(SUBMITTED_TEXT);
    }

    @Step("Get thank you message text")
    public String getThankYouMessage() {
        return getText(TEXT_BELOW_SUBMITTED);
    }

    @Step("Tap Done button")
    public void tapDone() {
        tap(DONE_BUTTON);
    }

    @Step("Tap Close button")
    public void tapClose() {
        tap(CLOSE_BUTTON);
    }

    public boolean isSuccessScreenLoaded() {
        return isPresent(SUBMITTED_TEXT, 15);
    }
}
