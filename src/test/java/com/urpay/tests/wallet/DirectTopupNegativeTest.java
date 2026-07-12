package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.DirectTopupPage;
import com.urpay.pages.wallet.FamilyWalletPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Direct Top-up — negative (unhappy) scenario.
 *
 * <p>Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/DirectTopup-Negative Scenarios and Family Wallet Direct Top Up Unhappy
 *   scenario Test Suite ({@code Wallet_VAS/FamilyWallet/Direct-Topup/ErrorMsgsValidation/*}).
 *
 * <p>Parent opens the kid profile and attempts to add an amount that exceeds the allowed limit
 * (2000) → verifies the "exceeded the allowed amount limit" error message.
 */
@Epic("Wallet & VAS")
@Feature("Direct Top-up")
public class DirectTopupNegativeTest extends BaseTest {

    @Test(groups = {"wallet", "direct-topup", "family", "negative"}, priority = 1)
    @Story("Parent cannot exceed the allowed top-up amount")
    @Description("Parent attempts to add an amount above the allowed limit to the kid wallet and "
            + "verifies the exceeded-limit error message")
    @Severity(SeverityLevel.NORMAL)
    public void testParentCannotExceedAllowedAmount() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsParent();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent login");

        FamilyWalletPage family = new FamilyWalletPage();
        family.tapFamilyWallet();
        family.waitUntilLoaded();
        family.tapFirstFamilyMember();

        DirectTopupPage topup = new DirectTopupPage();
        String message = topup.enterAmountAndReadNotification(
                c.get("directTopup.exceedAmount", "2000"), 20);

        Assert.assertEquals(message,
                c.get("directTopup.exceedError"),
                "The exceeded-limit error message should be displayed");
    }

    @Step("Login as the direct-topup (negative) parent")
    private DashboardPage loginAsParent() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("directTopupNegative.parent.mobileNumber"),
                c.get("directTopupNegative.parent.id"),
                c.get("directTopupNegative.parent.verificationCode", "1234"),
                c.get("directTopupNegative.parent.passCode", "2233"));
    }
}
