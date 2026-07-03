package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.WalletBalancePage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Wallet Balance — dashboard total money verification.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/WalletBalance/VerifyTotalMoneyCalculatedSuccessDashboard
 *   (Setup Test Data → login → Get the Wallet Balance From Dashboard → RechargeWallet →
 *   verifyWalletBalanceAfterRecharge).
 *
 * <p>The upstream DB-preset recharge was removed, so the suite now logs in, reveals and reads the
 * dashboard balance, refreshes the page, and verifies the balance stays consistent.
 */
@Epic("Wallet & VAS")
@Feature("Wallet Balance")
public class WalletBalanceTest extends BaseTest {

    @Test(groups = {"wallet", "wallet-balance"}, priority = 1)
    @Story("Dashboard wallet balance is consistent after a refresh")
    @Description("Login, reveal and read the dashboard wallet balance, pull-to-refresh, and verify "
            + "the balance is unchanged")
    @Severity(SeverityLevel.NORMAL)
    public void testWalletBalanceConsistentAfterRefresh() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        WalletBalancePage page = new WalletBalancePage();
        page.revealBalance();
        String before = page.getBalance();

        page.refresh();
        String after = page.getBalance();

        Assert.assertEquals(after, before,
                "Dashboard wallet balance should be unchanged after a refresh");
    }

    @Step("Login as the wallet-balance user")
    private DashboardPage login() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("walletBalance.mobileNumber"),
                c.get("walletBalance.id"),
                c.get("walletBalance.verificationCode", "1234"),
                c.get("walletBalance.passCode", "2233"));
    }
}
