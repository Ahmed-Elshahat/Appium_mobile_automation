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
 * Direct Top-up — positive scenario.
 *
 * <p>Migrated from Katalon suites:
 *   Test Suites/.../WMVSuites/DirectTopup-Positive Scenarios and Family Wallet Direct Topup Test
 *   Suite ({@code Wallet_VAS/FamilyWallet/Direct-Topup/*}).
 *
 * <p>Parent logs in → opens the family wallet and the kid profile → adds money to the kid wallet →
 * confirms → verifies the "Thank You" success screen.
 */
@Epic("Wallet & VAS")
@Feature("Direct Top-up")
public class DirectTopupPositiveTest extends BaseTest {

    @Test(groups = {"wallet", "direct-topup", "family"}, priority = 1)
    @Story("Parent adds money to a kid wallet")
    @Description("Parent opens the kid profile, adds money to the kid wallet and confirms, then "
            + "verifies the success screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentAddsMoneyToKidWallet() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsParent();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent login");

        FamilyWalletPage family = new FamilyWalletPage();
        family.tapFamilyWallet();
        family.waitUntilLoaded();
        family.tapFirstFamilyMember();

        DirectTopupPage topup = new DirectTopupPage();
        topup.addMoneyAndConfirm(c.get("directTopup.amount", "10"),
                c.get("directTopup.parent.verificationCode", "1234"));

        Assert.assertTrue(topup.isThankYouShown(),
                "The 'Thank You' success screen should appear after adding money to the kid wallet");
        topup.tapDone();
    }

    @Step("Login as the direct-topup parent")
    private DashboardPage loginAsParent() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("directTopup.parent.mobileNumber"),
                c.get("directTopup.parent.id"),
                c.get("directTopup.parent.verificationCode", "1234"),
                c.get("directTopup.parent.passCode", "2233"));
    }
}
