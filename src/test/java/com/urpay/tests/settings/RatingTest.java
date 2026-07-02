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
    //  TEST: FIRST FEEDBACK SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating", "smoke"}, priority = 1)
    @Story("First Feedback Screen")
    @Description("Navigate to rating screen and verify all texts on the first NPS screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testFirstFeedbackScreenTexts() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        RatingFlow flow = new RatingFlow();
        RatingPage page = flow.navigateToRating();
        Assert.assertTrue(page.isFirstScreenLoaded(), "First feedback screen should be loaded");

        String header = page.getFirstScreenHeader();
        Assert.assertTrue(header.contains("Hello"), "Header should contain greeting: " + header);

        String question = page.getQuestionText();
        Assert.assertTrue(question.contains("recommend urpay"),
                "Question should ask about recommending urpay: " + question);

        String notLikely = page.getNotLikelyText();
        Assert.assertEquals(notLikely, "Not Likely", "Lower limit text mismatch");

        String extremelyLikely = page.getExtremelyLikelyText();
        Assert.assertEquals(extremelyLikely, "Extremely Likely", "Upper limit text mismatch");

        log.info("First feedback screen texts verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SELECT RATING AND NAVIGATE TO NEXT SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating"}, priority = 2,
            dependsOnMethods = "testFirstFeedbackScreenTexts")
    @Story("First Feedback Screen")
    @Description("Select rating 6 and verify navigation to second screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testSelectRatingAndNavigateToNext() {
        RatingPage page = new RatingPage();
        page.selectRating6();

        Assert.assertTrue(page.isNextButtonVisible(), "Next button should be visible after rating selection");

        page.tapNext();
        Assert.assertTrue(page.isSecondScreenLoaded(), "Second feedback screen should be loaded");

        log.info("Rating selected and navigated to second screen successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SECOND FEEDBACK SCREEN TEXTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating"}, priority = 3,
            dependsOnMethods = "testSelectRatingAndNavigateToNext")
    @Story("Second Feedback Screen")
    @Description("Verify all factor texts on the second feedback screen")
    @Severity(SeverityLevel.NORMAL)
    public void testSecondFeedbackScreenTexts() {
        RatingPage page = new RatingPage();

        String header = page.getSecondScreenHeader();
        Assert.assertEquals(header, "What Factors influenced your rating?",
                "Second screen header mismatch");

        Assert.assertEquals(page.getUrpayCardsText(), "Urpay Cards", "Urpay Cards text mismatch");
        Assert.assertEquals(page.getMoneyTransferText(), "Money Transfer", "Money Transfer text mismatch");
        Assert.assertEquals(page.getUrpayAppText(), "Urpay App", "Urpay App text mismatch");
        Assert.assertEquals(page.getUrpayStoreText(), "Urpay Store", "Urpay Store text mismatch");
        Assert.assertEquals(page.getCustomerServiceText(), "Customer Service", "Customer Service text mismatch");

        log.info("Second feedback screen texts verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SELECT FACTOR AND NAVIGATE TO THIRD SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating"}, priority = 4,
            dependsOnMethods = "testSecondFeedbackScreenTexts")
    @Story("Second Feedback Screen")
    @Description("Select Urpay App factor and navigate to third screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testSelectFactorAndNavigateToThird() {
        RatingPage page = new RatingPage();
        page.selectUrpayAppFactor();
        page.tapNext();

        Assert.assertTrue(page.isThirdScreenLoaded(), "Third feedback screen should be loaded");

        String header = page.getThirdScreenHeader();
        Assert.assertEquals(header, "Do you have any comments?",
                "Third screen header mismatch");

        log.info("Factor selected and navigated to third screen successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: ENTER COMMENT AND SUBMIT RATING
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "rating"}, priority = 5,
            dependsOnMethods = "testSelectFactorAndNavigateToThird")
    @Story("Third Feedback Screen")
    @Description("Enter comment text and submit the rating, verify success message")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterCommentAndSubmit() {
        RatingPage page = new RatingPage();
        page.enterComment("test");
        page.tapSubmit();

        Assert.assertTrue(page.isSuccessScreenLoaded(), "Success screen should be loaded");

        String submittedText = page.getSubmittedText();
        Assert.assertEquals(submittedText, "Submitted", "Submitted text mismatch");

        String thankYouMsg = page.getThankYouMessage();
        Assert.assertTrue(thankYouMsg.contains("Thank you"),
                "Thank you message should contain 'Thank you': " + thankYouMsg);

        page.tapClose();
        log.info("Rating submitted and success message verified");
    }

    // ══════════════════════════════════════════════════
    //  ALLURE STEP METHODS
    // ══════════════════════════════════════════════════

    @Step("Login with the freshly registered rating user")
    private DashboardPage login() {
        return new LoginFlow().loginWith(
                freshUser.mobile,
                freshUser.poi,
                ConfigManager.getInstance().get("rating.verificationCode", "1234"),
                freshUser.passcode);
    }
}
