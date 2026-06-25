package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.WalletTransferFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.WalletTransferPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Wallet Transfer — peer-to-peer transfer between URPay wallets (Remittance squad).
 *
 * Migrated from Katalon: Scripts/Remittance/WalletTran/
 *
 * Single LambdaTest session: the first test logs in, the second reuses the session
 * (dependsOnMethods). Navigation always returns to the dashboard before re-entering
 * Wallet Transfer, so the order is self-correcting.
 *
 *   1. Search a wallet beneficiary  → verify the search results screen
 *   2. Transfer to an unsaved number → verify the success (Done) screen
 */
@Epic("Remittance")
@Feature("Wallet Transfer")
public class WalletTransferTest extends BaseTest {

    @Test(priority = 1, groups = {"remittance", "wallet-transfer", "smoke"})
    @Story("Search Wallet Beneficiary")
    @Description("Login → Transfer → Wallet Transfer → search recipient → verify results screen")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchWalletBeneficiary() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("urpayUser.mobileNumber"),
                c.get("urpayUser.id"),
                c.get("urpayUser.verificationCode", "1234"),
                c.get("urpayUser.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        WalletTransferPage page = new WalletTransferFlow()
                .searchWalletBeneficiary(c.get("receiver.mobileNumber"));

        Assert.assertTrue(
                page.isTransferToUnsavedNumberVisible(10) || page.isBeneficiaryResultVisible(10),
                "Search should surface a beneficiary result or the 'Transfer to unsaved number' option");
    }

    @Test(priority = 2, dependsOnMethods = "testSearchWalletBeneficiary",
            groups = {"remittance", "wallet-transfer", "smoke"})
    @Story("Transfer to Unsaved Number")
    @Description("Wallet Transfer → unsaved number → amount → purpose → confirm → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testWalletTransferToUnsavedNumber() {
        ConfigManager c = ConfigManager.getInstance();

        // Session is already authenticated by testSearchWalletBeneficiary.
        WalletTransferPage page = new WalletTransferFlow().transferToUnsavedNumber(
                c.get("receiver.mobileNumber"),
                c.get("walletTransfer.amount", "10"),
                c.get("urpayUser.verificationCode", "1234"));

        Assert.assertTrue(page.isDoneVisible(30),
                "Done / success screen should be visible after the wallet transfer");
    }
}
