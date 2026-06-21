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
import io.qameta.allure.Step;
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
        ConfigManager config = ConfigManager.getInstance();

        DashboardPage dashboard = login(config);
        String dashboardBalance = captureDashboardBalance(dashboard);

        AccountDetailsPage page = new AccountDetailsFlow().navigateToAccountDetails();
        Assert.assertTrue(page.isAccountDetailsLoaded(),
                "Account Details header should be visible");

        verifyAccountIdentity(page, config);
        verifyBalance(page, dashboardBalance);
        verifyCashback(page);
        verifyPendingAmounts(page);
        verifyAccountStatement(page);
        verifyShareAndReturn(page);

        log.info("Account Details full flow completed successfully");
    }

    // ══════════════════════════════════════════════════
    //  ALLURE STEP METHODS
    // ══════════════════════════════════════════════════

    @Step("Login with Account Details user")
    private DashboardPage login(ConfigManager config) {
        DashboardPage dashboard = new LoginFlow().loginWith(
                config.get("accountDetails.mobileNumber"),
                config.get("accountDetails.id"),
                config.get("accountDetails.verificationCode", "1234"),
                config.get("accountDetails.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
        return dashboard;
    }

    @Step("Capture dashboard balance before navigation")
    private String captureDashboardBalance(DashboardPage dashboard) {
        AccountDetailsFlow flow = new AccountDetailsFlow();
        String balance = flow.getDashboardBalance();
        Assert.assertNotNull(balance, "Dashboard balance should not be null");
        return balance;
    }

    @Step("Verify bank name, IBAN, QR code and copy IBAN")
    private void verifyAccountIdentity(AccountDetailsPage page, ConfigManager config) {
        Assert.assertTrue(page.isQrCodeVisible(),
                "QR code should be visible on Account Details page");
        String qrText = page.getQrCodeText();
        Assert.assertTrue(qrText.contains("For transfers use the QR code or details below"),
                "QR text mismatch. Actual: " + qrText);

        Assert.assertEquals(page.getBankName(), config.get("accountDetails.bankName"),
                "Bank name should match expected value");

        Assert.assertEquals(page.getIbanText(), config.get("accountDetails.iban"),
                "IBAN should match expected value");

        page.tapCopyIban();
        log.info("Copy IBAN tapped");
    }

    @Step("Verify available balance matches dashboard and footer text")
    private void verifyBalance(AccountDetailsPage page, String dashboardBalance) {
        String accountBalance = page.getAvailableBalance();
        Assert.assertNotNull(accountBalance, "Available balance should not be null");
        Assert.assertFalse(accountBalance.trim().isEmpty(),
                "Available balance should not be empty");

        BigDecimal accountBd = parseCurrency(accountBalance);
        BigDecimal dashboardBd = parseCurrency(dashboardBalance);
        Assert.assertEquals(accountBd.compareTo(dashboardBd), 0,
                "Balance mismatch. Account: " + accountBalance + ", Dashboard: " + dashboardBalance);
        log.info("Available balance: {}", accountBalance);

        String footerText = page.getFooterText();
        Assert.assertNotNull(footerText, "Footer text should not be null");
        Assert.assertTrue(footerText.contains("Cashback has already been added to your balance"),
                "Footer mismatch. Actual: " + footerText);
    }

    @Step("Verify cashback details")
    private void verifyCashback(AccountDetailsPage page) {
        String cashbackBefore = page.getCashbackAmountBefore();
        Assert.assertNotNull(cashbackBefore, "Cashback amount (before) should not be null");
        log.info("Cashback before: {}", cashbackBefore);

        page.tapViewAllCashbackDetails();
        try {
            Assert.assertTrue(page.getCashbackScreenHeader().contains("Cashback"),
                    "Cashback header should contain 'Cashback'");
            String cashbackAfter = page.getCashbackAmountAfter();
            Assert.assertNotNull(cashbackAfter, "Cashback amount (after) should not be null");
            log.info("Cashback after: {}", cashbackAfter);
            Assert.assertEquals(parseCurrency(cashbackAfter).compareTo(parseCurrency(cashbackBefore)), 0,
                    "Cashback mismatch. Before: " + cashbackBefore + ", After: " + cashbackAfter);
        } finally {
            page.tapBack();
        }
    }

    @Step("Verify pending amounts")
    private void verifyPendingAmounts(AccountDetailsPage page) {
        page.tapViewAllHoldAmounts();
        try {
            Assert.assertTrue(page.getHoldBalanceHeader().contains("Pending Amounts"),
                    "Pending Amounts header mismatch");
        } finally {
            page.tapBack();
        }
    }

    @Step("Verify account statement")
    private void verifyAccountStatement(AccountDetailsPage page) {
        page.tapAccountStatement();
        try {
            Assert.assertTrue(page.getAccountStatementHeader().contains("Account Statement"),
                    "Account Statement header mismatch");
        } finally {
            page.tapBack();
        }
    }

    @Step("Verify share button and return to dashboard")
    private void verifyShareAndReturn(AccountDetailsPage page) {
        Assert.assertTrue(page.isShareButtonVisible(),
                "Share button should be visible on Account Details page");
        page.tapBack();
    }

    // ══════════════════════════════════════════════════
    //  UTILITY
    // ══════════════════════════════════════════════════

    private BigDecimal parseCurrency(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot parse null currency value");
        }
        String clean = value.replaceAll("[^\\d.]", "").trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot parse currency from value: '" + value + "' — no digits found");
        }
        return new BigDecimal(clean);
    }
}
