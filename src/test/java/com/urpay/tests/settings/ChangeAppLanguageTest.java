package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.ChangeAppLanguageFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.ChangeAppLanguagePage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Change App Language Test — verifies language switching:
 *   Navigate to Language settings → switch to Arabic → verify →
 *   switch back to English → verify.
 *
 * Katalon source: Test Cases/Wallet_VAS/ChangeAppLanguage/
 */
@Epic("Wallet & VAS")
@Feature("Change App Language")
public class ChangeAppLanguageTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  TEST: NAVIGATE TO LANGUAGE SETTINGS
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "language", "smoke"}, priority = 1)
    @Story("Language Settings Navigation")
    @Description("Login and navigate to Language settings page")
    @Severity(SeverityLevel.CRITICAL)
    public void testNavigateToLanguageSettings() {
        ConfigManager config = ConfigManager.getInstance();

        DashboardPage dashboard = login(config);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        ChangeAppLanguageFlow flow = new ChangeAppLanguageFlow();
        ChangeAppLanguagePage page = flow.navigateToLanguageSettings();
        Assert.assertTrue(page.isLanguagePageLoaded(), "Language settings page should be loaded");

        String header = page.getLanguageHeader();
        Assert.assertEquals(header, "Language", "Language page header mismatch");

        log.info("Language settings page navigated successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SWITCH TO ARABIC
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "language"}, priority = 2,
            dependsOnMethods = "testNavigateToLanguageSettings")
    @Story("Switch to Arabic")
    @Description("Switch app language to Arabic and verify Arabic text appears on dashboard")
    @Severity(SeverityLevel.CRITICAL)
    public void testSwitchToArabic() {
        ChangeAppLanguagePage page = new ChangeAppLanguagePage();
        page.switchToArabic();

        Assert.assertTrue(page.isArabicApplied(),
                "Arabic text (رصيدك) should be visible after switching to Arabic");

        log.info("Language switched to Arabic successfully");
    }

    // ══════════════════════════════════════════════════
    //  TEST: SWITCH BACK TO ENGLISH
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "language"}, priority = 3,
            dependsOnMethods = "testSwitchToArabic")
    @Story("Switch to English")
    @Description("Navigate back to Language settings, switch to English and verify English text appears")
    @Severity(SeverityLevel.CRITICAL)
    public void testSwitchBackToEnglish() {
        ConfigManager config = ConfigManager.getInstance();

        // Switching to Arabic restarts the app to the passcode screen (logged out), so
        // re-authenticate to reach the dashboard before navigating to Language settings.
        // loginWith() detects the passcode screen and re-enters the passcode only.
        login(config);

        ChangeAppLanguagePage page = new ChangeAppLanguagePage();
        page.navigateToLanguageSettings();
        page.switchToEnglish();

        Assert.assertTrue(page.isEnglishApplied(),
                "English text should be visible after switching back to English");

        log.info("Language switched back to English successfully");
    }

    // ══════════════════════════════════════════════════
    //  ALLURE STEP METHODS
    // ══════════════════════════════════════════════════

    @Step("Login with Change Language test user")
    private DashboardPage login(ConfigManager config) {
        return new LoginFlow().loginWith(
                config.get("changeLanguage.mobileNumber"),
                config.get("changeLanguage.id"),
                config.get("changeLanguage.verificationCode", "1234"),
                config.get("changeLanguage.passCode", "2233"));
    }
}
