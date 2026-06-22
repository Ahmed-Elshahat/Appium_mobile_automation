package com.urpay.flows;

import com.urpay.pages.settings.RatingPage;

import io.qameta.allure.Step;

/**
 * Rating flow — navigates to the rating screen and provides
 * methods to progress through the 3-screen feedback flow.
 *
 * Katalon source: Test Cases/Wallet_VAS/Rating/
 */
public class RatingFlow {

    private final RatingPage ratingPage;

    public RatingFlow() {
        this.ratingPage = new RatingPage();
    }

    @Step("Navigate to Rate Urpay feedback screen")
    public RatingPage navigateToRating() {
        ratingPage.navigateToRateUrpay();
        return ratingPage;
    }

    @Step("Complete first screen: select rating 6 and tap Next")
    public RatingPage completeFirstScreen() {
        ratingPage.selectRating6();
        ratingPage.tapNext();
        return ratingPage;
    }

    @Step("Complete second screen: select Urpay App factor and tap Next")
    public RatingPage completeSecondScreen() {
        ratingPage.selectUrpayAppFactor();
        ratingPage.tapNext();
        return ratingPage;
    }

    @Step("Complete third screen: enter comment and submit")
    public RatingPage completeThirdScreen(String comment) {
        ratingPage.enterComment(comment);
        ratingPage.tapSubmit();
        return ratingPage;
    }
}
