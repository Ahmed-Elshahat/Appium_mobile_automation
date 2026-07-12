package com.urpay.flows;

import com.urpay.pages.settings.RatingPage;

import io.qameta.allure.Step;

/**
 * Rating flow — navigates to the rating screen and submits the single-question NPS survey.
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

    @Step("Submit NPS rating {rating}")
    public RatingPage submitRating(String rating) {
        ratingPage.selectRating(rating);
        ratingPage.tapSubmit();
        return ratingPage;
    }
}
