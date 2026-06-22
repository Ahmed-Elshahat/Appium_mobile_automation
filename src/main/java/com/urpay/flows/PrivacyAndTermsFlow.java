package com.urpay.flows;

import com.urpay.pages.settings.ConsentsPage;
import com.urpay.pages.settings.TermsConditionsPage;

import io.qameta.allure.Step;

/**
 * Privacy & Terms flow — navigates to Consents page and provides access
 * to Privacy Policy and Terms & Conditions pages.
 *
 * Katalon source: Test Cases/Wallet_VAS/PolicyAndTerm/
 */
public class PrivacyAndTermsFlow {

    private final ConsentsPage consentsPage;

    public PrivacyAndTermsFlow() {
        this.consentsPage = new ConsentsPage();
    }

    @Step("Navigate to Consents page from Dashboard")
    public ConsentsPage navigateToConsents() {
        consentsPage.navigateToConsents();
        return consentsPage;
    }

    @Step("Open Privacy Policy page")
    public ConsentsPage openPrivacyPolicy() {
        consentsPage.tapPrivacyPolicy();
        return consentsPage;
    }

    @Step("Open Terms & Conditions page")
    public TermsConditionsPage openTermsAndConditions() {
        consentsPage.tapTermsAndConditions();
        return new TermsConditionsPage();
    }
}
