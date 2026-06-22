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
    //  TEST: CARD TERMS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "privacy-terms"}, priority = 3,
            dependsOnMethods = "testGeneralTermsPage")
    @Story("Card Terms & Conditions")
    @Description("Verify Card Terms subtitle, Digital Card and Physical Card tabs with content")
    @Severity(SeverityLevel.NORMAL)
    public void testCardTermsPage() {
        ConsentsPage consentsPage = new ConsentsPage();
        consentsPage.tapTermsAndConditions();

        TermsConditionsPage termsPage = new TermsConditionsPage();
        Assert.assertTrue(termsPage.isTermsPageLoaded(), "Terms page should be loaded");

        termsPage.tapCardTermsTab();
        Assert.assertTrue(termsPage.isCardTermsSubtitleVisible(),
                "Card Terms subtitle should be visible");

        String cardTitle = termsPage.getCardTermsSubtitle();
        Assert.assertEquals(cardTitle, "Urpay Cards Terms And Conditions",
                "Card Terms subtitle mismatch");

        // Digital Card tab
        termsPage.tapDigitalCardTab();
        Assert.assertTrue(termsPage.isTheseTermsVisible(), "Digital Card 'These Terms' should be visible");
        Assert.assertTrue(termsPage.scrollToAcknowledgement(), "Acknowledgement section should exist in Digital Card");

        // Physical Card tab
        termsPage.scrollBackToCardTermsTitle();
        termsPage.tapPhysicalCardTab();
        Assert.assertTrue(termsPage.isTheseTermsVisible(), "Physical Card 'These Terms' should be visible");
        Assert.assertTrue(termsPage.scrollToAcknowledgement(), "Acknowledgement section should exist in Physical Card");

        termsPage.scrollBackToCardTermsTitle();
        log.info("Card Terms page verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: MOKAFAA TERMS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "privacy-terms"}, priority = 4,
            dependsOnMethods = "testGeneralTermsPage")
    @Story("Mokafaa Terms & Conditions")
    @Description("Verify Mokafaa Terms subtitle and content")
    @Severity(SeverityLevel.NORMAL)
    public void testMokafaaTermsPage() {
        ConsentsPage consentsPage = new ConsentsPage();
        consentsPage.tapTermsAndConditions();

        TermsConditionsPage termsPage = new TermsConditionsPage();
        termsPage.tapMokafaaTab();

        Assert.assertTrue(termsPage.isMokafaaSubtitleVisible(),
                "Mokafaa subtitle should be visible");

        String mokafaaTitle = termsPage.getMokafaaSubtitle();
        Assert.assertEquals(mokafaaTitle, "Mokafaa Terms & Conditions",
                "Mokafaa subtitle mismatch");

        log.info("Mokafaa Terms page verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: MONEYGRAM TERMS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "privacy-terms"}, priority = 5,
            dependsOnMethods = "testGeneralTermsPage")
    @Story("MoneyGram Terms & Conditions")
    @Description("Verify MoneyGram Terms subtitle and fraud warning content")
    @Severity(SeverityLevel.NORMAL)
    public void testMoneyGramTermsPage() {
        ConsentsPage consentsPage = new ConsentsPage();
        consentsPage.tapTermsAndConditions();

        TermsConditionsPage termsPage = new TermsConditionsPage();
        termsPage.tapMoneyGramTab();

        Assert.assertTrue(termsPage.isMoneyGramSubtitleVisible(),
                "MoneyGram subtitle should be visible");

        String moneyGramTitle = termsPage.getMoneyGramSubtitle();
        Assert.assertEquals(moneyGramTitle, "Moneygram Terms And Conditions For Sending Money",
                "MoneyGram subtitle mismatch");

        Assert.assertTrue(termsPage.scrollToMoneyGramFraudWarning(),
                "MoneyGram fraud warning content should be visible");

        log.info("MoneyGram Terms page verified successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: COMPLAINTS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "privacy-terms"}, priority = 6,
            dependsOnMethods = "testGeneralTermsPage")
    @Story("Complaints Terms")
    @Description("Verify Complaints (How To File A Complaint) page title")
    @Severity(SeverityLevel.NORMAL)
    public void testComplaintsPage() {
        ConsentsPage consentsPage = new ConsentsPage();
        consentsPage.tapTermsAndConditions();

        TermsConditionsPage termsPage = new TermsConditionsPage();
        termsPage.tapComplaintsTab();

        Assert.assertTrue(termsPage.isComplaintsTitleVisible(),
                "Complaints title should be visible");

        String complaintsTitle = termsPage.getComplaintsTitle();
        Assert.assertEquals(complaintsTitle, "How To File A Complaint",
                "Complaints title mismatch");

        log.info("Complaints page verified successfully");
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
