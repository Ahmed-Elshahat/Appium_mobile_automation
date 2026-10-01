package com.urpay.tests.settings;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.UpdateNameFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.settings.UpdateNamePage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/** Update Name — login → Profile → Update Name → Update (re-syncs the name from government records). */
@Epic("Wallet & VAS")
@Feature("Update Name")
public class UpdateNameTest extends BaseTest {

    @Test(groups = {"settings", "profile"}, priority = 1)
    @Story("Open Update Name")
    @Description("Login and open Profile → Update Name; the read-only name field shows the current name")
    @Severity(SeverityLevel.CRITICAL)
    public void testOpenUpdateName() {
        Assert.assertTrue(login().isLoaded(), "Dashboard should be visible after login");
        UpdateNamePage page = new UpdateNameFlow().openUpdateName();
        Assert.assertTrue(page.isUpdateNameScreenDisplayed(15), "Update Name screen should be displayed");
        Assert.assertFalse(page.getDisplayedName().isBlank(), "Current name should be shown");
    }

    @Test(groups = {"settings", "profile"}, priority = 2, dependsOnMethods = "testOpenUpdateName")
    @Story("Update Name")
    @Description("Tap Update; the app must not return an error")
    @Severity(SeverityLevel.CRITICAL)
    public void testTapUpdateName() {
        String result = new UpdateNameFlow().submitUpdate();
        Assert.assertFalse(result.matches("(?is).*(error|try again|unavailable).*"),
                "Update Name should succeed but the app returned: " + result);
    }

    @Step("Login with Update Name test user")
    private DashboardPage login() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("updateName.mobileNumber"),
                config.get("updateName.id"),
                config.get("updateName.verificationCode", "1234"),
                config.get("updateName.passCode", "2233"));
    }
}
