package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.RatingFlow;
import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.RatingPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Rating Test Suite — verifies the complete rating/feedback flow:
 *   Navigate → First screen (NPS) → Second screen (Factors) →
 *   Third screen (Comments) → Submit → Success
 *
 * Katalon source: Test Cases/Wallet_VAS/Rating/
 */
@Epic("Wallet & VAS")
@Feature("Rating")
public class RatingTest extends BaseTest {

    /** Credentials of the brand-new consumer provisioned for this run (see {@link #provisionFreshUser()}). */
    private static RegistrationApiHelper.Provisioned freshUser;

    // ══════════════════════════════════════════════════
    //  PRE-SUITE: PROVISION A BRAND-NEW USER
    // ══════════════════════════════════════════════════

    /**
     * Register a fresh consumer via the backend before the UI journey runs. The NPS/feedback
     * survey is gated to once-per-period, so a reused account that has already submitted feedback
     * would never reach the rating screen — a never-used account guarantees it is available.
     * Requires the neoleap VPN (SIT API host, simulator and Oracle DB are internal-network only).
     */
    @BeforeClass(alwaysRun = true)
    public void provisionFreshUser() {
        freshUser = RegistrationApiHelper.registerNationalAndReturn();
        Assert.assertNotNull(freshUser,
                "Failed to provision a fresh consumer for the rating suite (needs the neoleap VPN)");
        log.info("Rating suite will use freshly registered user: {}", freshUser.mobile);
    }

    // ══════════════════════════════════════════════════
    //  TEST: NPS SURVEY SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating", "smoke"}, priority = 1)
    @Story("NPS Survey")
    @Description("Navigate to the rating screen and verify the NPS question and scale labels")
    @Severity(SeverityLevel.CRITICAL)
    public void testFirstFeedbackScreenTexts() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        RatingFlow flow = new RatingFlow();
        RatingPage page = flow.navigateToRating();
        Assert.assertTrue(page.isFirstScreenLoaded(), "NPS survey screen should be loaded");

        String question = page.getQuestionText();
        Assert.assertTrue(question.contains("recommend Urpay"),
                "Question should ask about recommending Urpay: " + question);

        String notLikely = page.getNotLikelyText();
        Assert.assertEquals(notLikely, "Not Likely", "Lower limit text mismatch");

        String extremelyLikely = page.getExtremelyLikelyText();
        Assert.assertEquals(extremelyLikely, "Extremely Likely", "Upper limit text mismatch");

        log.info("NPS survey screen texts verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SELECT RATING AND SUBMIT
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating"}, priority = 2,
            dependsOnMethods = "testFirstFeedbackScreenTexts")
    @Story("NPS Survey")
    @Description("Select an NPS rating, confirm Submit enables, submit and reach the success screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testSelectRatingAndSubmit() {
        RatingPage page = new RatingPage();
        page.selectRating(ConfigManager.getInstance().get("rating.npsScore", "9"));

        Assert.assertTrue(page.isSubmitEnabled(), "Submit should be enabled after selecting a rating");

        page.tapSubmit();
        Assert.assertTrue(page.isSuccessScreenLoaded(), "Success screen should be loaded after submit");

        log.info("Rating selected and submitted successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SURVEY SUCCESS AND CLOSE
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating"}, priority = 3,
            dependsOnMethods = "testSelectRatingAndSubmit")
    @Story("NPS Survey")
    @Description("Verify the thank-you message on the success screen and close the survey")
    @Severity(SeverityLevel.NORMAL)
    public void testSurveySuccessAndClose() {
        RatingPage page = new RatingPage();

        String thankYouMsg = page.getThankYouMessage();
        Assert.assertTrue(thankYouMsg.contains("Thank you for taking the survey"),
                "Thank-you message mismatch: " + thankYouMsg);

        page.tapClose();
        log.info("Survey success verified and closed");
    }

    // ══════════════════════════════════════════════════
    //  ALLURE STEP METHODS
    // ══════════════════════════════════════════════════

    @Step("Login with the freshly registered rating user")
    private DashboardPage login() {
        return new LoginFlow().loginWith(
                toLocalMobile(freshUser.mobile),
                freshUser.poi,
                ConfigManager.getInstance().get("rating.verificationCode", "1234"),
                freshUser.passcode);
    }

    /** Convert the generated {@code +966XXXXXXXXX} mobile to the local {@code 05XXXXXXXX} login form. */
    private static String toLocalMobile(String intlMobile) {
        if (intlMobile != null && intlMobile.startsWith("+966")) {
            return "0" + intlMobile.substring(4);
        }
        return intlMobile;
    }
}
