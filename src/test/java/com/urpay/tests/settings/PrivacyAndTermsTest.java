package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.PrivacyAndTermsFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.ConsentsPage;
import com.urpay.pages.settings.TermsConditionsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Privacy & Terms Test Suite — verifies all consent pages:
 *   Privacy Policy, General Terms, Card Terms, MoneyGram, Mokafaa, Complaints.
 *
 * Katalon source: Test Cases/Wallet_VAS/PolicyAndTerm/
 */
@Epic("Wallet & VAS")
@Feature("Privacy & Terms")
public class PrivacyAndTermsTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  TEST: PRIVACY POLICY
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "privacy-terms", "smoke"}, priority = 1)
    @Story("Privacy Policy Page")
    @Description("Navigate to Privacy Policy page, verify title and reject toggle dialog")
    @Severity(SeverityLevel.CRITICAL)
    public void testPrivacyPolicyPage() {
        ConfigManager config = ConfigManager.getInstance();

        DashboardPage dashboard = login(config);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        PrivacyAndTermsFlow flow = new PrivacyAndTermsFlow();
        ConsentsPage consentsPage = flow.navigateToConsents();
        Assert.assertTrue(consentsPage.isConsentPageLoaded(), "Consents page should be loaded");

        flow.openPrivacyPolicy();
        Assert.assertTrue(consentsPage.isPrivacyTitleVisible(), "Privacy Policy title should be visible");

        String privacyTitle = consentsPage.getPrivacyTitle();
        Assert.assertEquals(privacyTitle, config.get("privacyTerms.privacyTitle", "Privacy Policy"),
                "Privacy Policy title mismatch");

        consentsPage.tapToggle();
        Assert.assertTrue(consentsPage.isRejectDialogVisible(), "Reject dialog should appear");

        String rejectText = consentsPage.getRejectText();
        Assert.assertEquals(rejectText, "Reject Privacy Policy?",
                "Reject dialog text mismatch");

        consentsPage.tapBack();
        consentsPage.tapBack();

        log.info("Privacy Policy page verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: GENERAL TERMS & CONDITIONS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "privacy-terms"}, priority = 2,
            dependsOnMethods = "testPrivacyPolicyPage")
    @Story("General Terms & Conditions Page")
    @Description("Navigate to Terms & Conditions, verify title and General Terms subtitle, and reject toggle")
    @Severity(SeverityLevel.CRITICAL)
    public void testGeneralTermsPage() {
        ConsentsPage consentsPage = new ConsentsPage();
        Assert.assertTrue(consentsPage.isConsentPageLoaded(), "Should be back on Consents page");

        consentsPage.tapTermsAndConditions();
        TermsConditionsPage termsPage = new TermsConditionsPage();
        Assert.assertTrue(termsPage.isTermsPageLoaded(), "Terms & Conditions page should be loaded");

        String termsTitle = termsPage.getTermsTitle();
        Assert.assertEquals(termsTitle, "Terms & Conditions",
                "Terms & Conditions title mismatch");

        Assert.assertTrue(termsPage.isGeneralTermsSubtitleVisible(),
                "General Terms & Conditions subtitle should be visible");

        termsPage.tapToggle();
        Assert.assertTrue(termsPage.isRejectDialogVisible(), "Reject dialog should appear");

        String rejectText = termsPage.getRejectText();
        Assert.assertEquals(rejectText, "Reject Terms & Conditions?",
                "Reject dialog text mismatch");

        termsPage.tapBack();
        termsPage.tapBack();

        log.info("General Terms page verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  ALLURE STEP METHODS
    // ══════════════════════════════════════════════════

    @Step("Login with Privacy & Terms test user")
    private DashboardPage login(ConfigManager config) {
        return new LoginFlow().loginWith(
                config.get("privacyTerms.mobileNumber"),
                config.get("privacyTerms.id"),
                config.get("privacyTerms.verificationCode", "1234"),
                config.get("privacyTerms.passCode", "2233"));
    }
}
