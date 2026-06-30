package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.WalletTransferFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.WalletTransactionDetailsPage;
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

        Assert.assertTrue(page.isTransferSuccessful(30),
                "Success (Thank You) screen should be visible after the wallet transfer");
    }

    @Test(priority = 3, dependsOnMethods = "testWalletTransferToUnsavedNumber",
            groups = {"remittance", "wallet-transfer", "smoke"})
    @Story("Validate Sender Transaction History")
    @Description("After transfer → Transactions → open latest → verify Send money type, receiver mobile & amount")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateSenderTransactionHistory() {
        ConfigManager c = ConfigManager.getInstance();
        String recipient = c.get("receiver.mobileNumber");
        // Significant digits: KSA numbers show as +966XXXXXXXXX (leading 0 dropped).
        String significantDigits = recipient.startsWith("0") ? recipient.substring(1) : recipient;
        String amount = c.get("walletTransfer.amount", "20");

        WalletTransactionDetailsPage details = new WalletTransferFlow().openLastTransaction();

        Assert.assertTrue(details.isLoaded(), "Transaction details should load");
        Assert.assertTrue(details.isWalletTransferTransaction(10),
                "Latest transaction should be a wallet transfer (first row was: "
                + details.getFirstDetail() + ")");
        Assert.assertTrue(details.showsReceiverMobile(significantDigits),
                "Transaction details should show the receiver mobile " + recipient);
        Assert.assertTrue(details.showsAmount(amount),
                "Transaction details should show the transferred amount " + amount);
    }

    @Test(priority = 4, dependsOnMethods = "testSearchWalletBeneficiary",
            groups = {"remittance", "wallet-transfer"})
    @Story("Existing User Name Confirmation")
    @Description("Wallet Transfer → unsaved number → existing URPay user → Confirm screen shows the recipient's name (no money moved)")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateRecipientNameConfirmation() {
        ConfigManager c = ConfigManager.getInstance();
        // The receiver (0520553918) is an existing URPay user — entering their number must
        // resolve & show their name on the Confirm screen. Migrated from
        // ValidateExistingUserPayMobileNameConfirmationPage (no Confirm/OTP — no money moved).
        WalletTransferPage page = new WalletTransferFlow().openTransferConfirmation(
                c.get("receiver.mobileNumber"),
                c.get("walletTransfer.amount", "20"));

        Assert.assertTrue(page.isExistingUserNameShown(10),
                "Confirm screen should resolve & show the existing recipient's name (was: "
                + page.getRecipientName() + ")");
    }

    @Test(priority = 5, dependsOnMethods = "testSearchWalletBeneficiary",
            groups = {"remittance", "wallet-transfer"})
    @Story("Transfer to Active Beneficiary")
    @Description("Wallet Transfer → select an active/saved beneficiary DIRECTLY (no unsaved number) → amount → confirm → OTP → success")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToActiveBeneficiary() {
        ConfigManager c = ConfigManager.getInstance();
        WalletTransferFlow flow = new WalletTransferFlow();

        WalletTransferPage page = flow.openWalletTransferWithContacts();
        // Faithful to Katalon Scripts/Remittance/WalletTransfer: tap a saved beneficiary directly.
        // Requires an ACTIVE beneficiary on the account (device contact or saved/recent recipient).
        if (!page.isBeneficiaryResultVisible(8)) {
            throw new SkipException("No active beneficiary on account "
                    + c.get("urpayUser.mobileNumber")
                    + " — the direct-beneficiary transfer needs a saved beneficiary "
                    + "(add a device contact or a saved recipient).");
        }

        page = flow.transferToActiveBeneficiary(page,
                c.get("walletTransfer.amount", "20"),
                c.get("urpayUser.verificationCode", "1234"));

        Assert.assertTrue(page.isTransferSuccessful(30),
                "Success (Thank You) screen should show after transferring to the active beneficiary");
    }
}
