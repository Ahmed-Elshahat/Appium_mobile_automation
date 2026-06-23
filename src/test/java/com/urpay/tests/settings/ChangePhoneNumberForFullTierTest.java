package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.ChangePhoneNumberFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.ChangePhoneNumberPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Change Phone Number For Full Tier.
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/ChangePhoneNumber/ChangePhoneNumberForFullTier
 *
 * Katalon test cases → mapping:
 *   1. SetupTestData                     → changePhone.* properties (sit-cards.properties)
 *   2. ToValidateLoginForRemoteDevicesOnly → LoginFlow.loginWith() in step 1
 *   3. ValidateOpeningChangePhoneNumber  → testValidateOpeningChangePhoneNumber()
 *   4. ValidateChangingPhoneNumber       → testValidateChangingPhoneNumber()
 *   5. ValidateThankScreenPresents       → testValidateThankScreenPresents()
 *   6. validateResetPasscode             → testValidateResetPasscode() (restores original number)
 */
@Epic("Wallet & VAS")
@Feature("Change Phone Number")
public class ChangePhoneNumberForFullTierTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  1) OPEN CHANGE PHONE NUMBER
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "phone", "smoke"}, priority = 1)
    @Story("Open Change Phone Number Screen")
    @Description("Login, tap profile avatar, open 'Change mobile number' and verify the form")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateOpeningChangePhoneNumber() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        ChangePhoneNumberPage page = new ChangePhoneNumberFlow().navigateToChangePhoneNumber();

        Assert.assertTrue(page.isChangePhoneScreenLoaded(),
                "Change Mobile Number screen should be displayed");
    }

    // ══════════════════════════════════════════════════
    //  2) CHANGE PHONE NUMBER
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "phone"}, priority = 2,
            dependsOnMethods = "testValidateOpeningChangePhoneNumber")
    @Story("Change Phone Number")
    @Description("Enter a new mobile number, handle 'already in use', enter OTP")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateChangingPhoneNumber() {
        ConfigManager config = ConfigManager.getInstance();

        ChangePhoneNumberPage page = new ChangePhoneNumberFlow().submitNewNumber(
                config.get("changePhone.newMobileNumber"),
                config.get("changePhone.mobileNumber"),
                config.get("changePhone.verificationCode", "1234"));

        Assert.assertTrue(page.isThankYouScreenDisplayed(),
                "Thank You screen should appear after changing the number");
    }

    // ══════════════════════════════════════════════════
    //  3) VALIDATE THANK YOU SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "phone"}, priority = 3,
            dependsOnMethods = "testValidateChangingPhoneNumber")
    @Story("Thank You Screen")
    @Description("Verify Thank You text and tap Done to return to Dashboard")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateThankScreenPresents() {
        ChangePhoneNumberPage page = new ChangePhoneNumberPage();

        Assert.assertEquals(page.getThankYouText(), "Thank You!",
                "Thank You text should match");

        page.tapDone();

        Assert.assertTrue(new DashboardPage().isLoaded(),
                "Dashboard should be visible after tapping Done");
    }

    // ══════════════════════════════════════════════════
    //  4) RESET — RESTORE ORIGINAL NUMBER
    // ══════════════════════════════════════════════════

    @Test(groups = {"settings", "phone"}, priority = 4,
            dependsOnMethods = "testValidateThankScreenPresents")
    @Story("Restore Original Phone Number")
    @Description("Change the number back to the original to restore test-data state")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateResetPasscode() {
        ConfigManager config = ConfigManager.getInstance();

        ChangePhoneNumberPage page = new ChangePhoneNumberFlow().restoreOriginalNumber(
                config.get("changePhone.mobileNumber"),
                config.get("changePhone.verificationCode", "1234"));

        Assert.assertTrue(page.isThankYouScreenDisplayed(),
                "Thank You screen should appear after restoring the original number");

        page.tapDone();
    }

    // ══════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════

    @Step("Login with Change Phone Number test user")
    private DashboardPage login() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("changePhone.mobileNumber"),
                config.get("changePhone.id"),
                config.get("changePhone.verificationCode", "1234"),
                config.get("changePhone.passCode", "2233"));
    }
}
