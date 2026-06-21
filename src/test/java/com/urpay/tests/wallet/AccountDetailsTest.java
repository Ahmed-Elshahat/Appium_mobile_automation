package com.urpay.tests.wallet;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.AccountDetailsFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.AccountDetailsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Account Details Test — single end-to-end flow:
 *   Login → Dashboard balance → Wallet → Account Details →
 *   QR code, bank name, IBAN, copy, balance, footer, cashback, hold, statement, share → back
 *
 * Katalon source: Test Cases/Wallet_VAS/AccountDetails/ (10 test cases + SetupTestData)
 */
@Epic("Wallet & VAS")
@Feature("Account Details")
public class AccountDetailsTest extends BaseTest {

    @Test(groups = {"wallet", "account-details", "smoke"})
    @Story("Account Details Verification")
    @Description("Login → navigate to Account Details → verify QR code, bank name, IBAN, "
            + "balance, footer, cashback, pending amounts, account statement, share → back")
    @Severity(SeverityLevel.CRITICAL)
    public void testAccountDetailsFullFlow() {
        ConfigManager c = ConfigManager.getInstance();

        // Step 1: Login with Account Details user
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("accountDetails.mobileNumber"),
                c.get("accountDetails.id"),
                c.get("accountDetails.verificationCode", "1234"),
                c.get("accountDetails.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        // Step 2: Get dashboard balance → navigate to Account Details
        AccountDetailsFlow flow = new AccountDetailsFlow();
        String dashboardBalance = flow.getDashboardBalance();
        Assert.assertNotNull(dashboardBalance, "Dashboard balance should not be null");

        AccountDetailsPage page = flow.navigateToAccountDetails();
        Assert.assertTrue(page.isAccountDetailsLoaded(),
                "Account Details header should be visible");

        // Step 3: Verify QR code
        Assert.assertTrue(page.isQrCodeVisible(),
                "QR code should be visible on Account Details page");
        String qrText = page.getQrCodeText();
        Assert.assertTrue(qrText.contains("For transfers use the QR code or details below"),
                "QR text mismatch. Actual: " + qrText);

        // Step 4: Verify bank name
        String actualBankName = page.getBankName();
        Assert.assertEquals(actualBankName, c.get("accountDetails.bankName"),
                "Bank name should match expected value");

        // Step 5: Verify IBAN & copy
        String actualIban = page.getIbanText();
        Assert.assertEquals(actualIban, c.get("accountDetails.iban"),
                "IBAN should match expected value");
        page.tapCopyIban();
        log.info("Copy IBAN tapped");

        // Step 6: Verify available balance matches dashboard
        String accountBalance = page.getAvailableBalance();
        Assert.assertNotNull(accountBalance, "Available balance should not be null");
        Assert.assertFalse(accountBalance.trim().isEmpty(),
                "Available balance should not be empty");
        BigDecimal accountBd = parseCurrency(accountBalance);
        BigDecimal dashboardBd = parseCurrency(dashboardBalance);
        Assert.assertEquals(accountBd.compareTo(dashboardBd), 0,
                "Balance mismatch. Account: " + accountBalance + ", Dashboard: " + dashboardBalance);
        log.info("Available balance: {}", accountBalance);

        // Step 7: Verify footer text
        String footerText = page.getFooterText();
        Assert.assertNotNull(footerText, "Footer text should not be null");
        Assert.assertTrue(footerText.contains("Cashback has already been added to your balance"),
                "Footer mismatch. Actual: " + footerText);

        // Step 8: Verify cashback — View All → compare amounts → back
        String cashbackBefore = page.getCashbackAmountBefore();
        Assert.assertNotNull(cashbackBefore, "Cashback amount (before) should not be null");
        log.info("Cashback before: {}", cashbackBefore);
        page.tapViewAllCashbackDetails();
        try {
            String cashbackHeader = page.getCashbackScreenHeader();
            Assert.assertTrue(cashbackHeader.contains("Cashback"),
                    "Cashback header mismatch. Actual: " + cashbackHeader);
            String cashbackAfter = page.getCashbackAmountAfter();
            Assert.assertNotNull(cashbackAfter, "Cashback amount (after) should not be null");
            log.info("Cashback after: {}", cashbackAfter);
            Assert.assertEquals(parseCurrency(cashbackAfter).compareTo(parseCurrency(cashbackBefore)), 0,
                    "Cashback mismatch. Before: " + cashbackBefore + ", After: " + cashbackAfter);
        } finally {
            page.tapBack();
        }

        // Step 9: Verify pending amounts screen — View All → check header → back
        page.tapViewAllHoldAmounts();
        try {
            String holdHeader = page.getHoldBalanceHeader();
            Assert.assertTrue(holdHeader.contains("Pending Amounts"),
                    "Pending Amounts header mismatch. Actual: " + holdHeader);
        } finally {
            page.tapBack();
        }

        // Step 10: Verify account statement screen → back
        page.tapAccountStatement();
        try {
            String statementHeader = page.getAccountStatementHeader();
            Assert.assertTrue(statementHeader.contains("Account Statement"),
                    "Account Statement header mismatch. Actual: " + statementHeader);
        } finally {
            page.tapBack();
        }

        // Step 11: Verify share button visible & go back to Dashboard
        Assert.assertTrue(page.isShareButtonVisible(),
                "Share button should be visible on Account Details page");
        page.tapBack();

        log.info("Account Details full flow completed successfully");
    }

    private BigDecimal parseCurrency(String value) {
        String clean = value.replaceAll("[^\\d.]", "").trim();
        return new BigDecimal(clean.isEmpty() ? "0" : clean);
    }
}
