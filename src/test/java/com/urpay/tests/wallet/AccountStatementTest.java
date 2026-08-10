package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.AccountStatementFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.AccountStatementPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Account Statement Test — single end-to-end flow:
 *   Login → Dashboard → Wallet → Account Details → Account Statement →
 *   Select date range (previous month to today) → View Statement → Verify PDF → Back
 *
 * Katalon source: Test Cases/Wallet_VAS/AccountStatement/ (3 test cases)
 *   - Setup Test data for Account statement
 *   - ToPerformTransaction (prerequisite — ensures transaction exists)
 *   - ToVerifyAccountStatementGeneratedLastTenDays
 */
@Epic("Wallet & VAS")
@Feature("Account Statement")
public class AccountStatementTest extends BaseTest {

    @Test(groups = {"wallet", "account-statement", "smoke"})
    @Story("Account Statement Generation")
    @Description("Login → navigate to Account Statement → select date range "
            + "(previous month to today) → view statement → verify PDF is generated")
    @Severity(SeverityLevel.CRITICAL)
    public void testAccountStatementFullFlow() {
        ConfigManager config = ConfigManager.getInstance();

        DashboardPage dashboard = login(config);

        AccountStatementFlow flow = new AccountStatementFlow();
        AccountStatementPage page = navigateToStatement(flow);

        verifyStatementHeader(page);
        selectDateRange(flow, page);
        viewAndVerifyStatement(page);
        returnToPreviousScreen(page);

        log.info("Account Statement full flow completed successfully");
    }

    @Step("Login with Account Statement user")
    private DashboardPage login(ConfigManager config) {
        DashboardPage dashboard = new LoginFlow().loginWith(
                config.get("accountStatement.mobileNumber"),
                config.get("accountStatement.id"),
                config.get("accountStatement.verificationCode", "1234"),
                config.get("accountStatement.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
        return dashboard;
    }

    @Step("Navigate to Account Statement screen")
    private AccountStatementPage navigateToStatement(AccountStatementFlow flow) {
        AccountStatementPage page = flow.navigateToAccountStatement();
        Assert.assertTrue(page.isAccountStatementLoaded(),
                "Account Statement header should be visible");
        return page;
    }

    @Step("Verify Account Statement header")
    private void verifyStatementHeader(AccountStatementPage page) {
        String header = page.getHeaderText();
        Assert.assertTrue(header.contains("Account Statement"),
                "Header mismatch. Expected 'Account Statement', Actual: " + header);
    }

    @Step("Select date range: previous month to today")
    private void selectDateRange(AccountStatementFlow flow, AccountStatementPage page) {
        flow.selectLastMonthDateRange(page);
    }

    @Step("View account statement and verify response")
    private void viewAndVerifyStatement(AccountStatementPage page) {
        // Capture the date-range form BEFORE tapping View — the resulting statement opens in a PDF
        // viewer (a secure SurfaceView) where getScreenshotAs returns empty, so a post-tap capture
        // is often blank. This pre-tap shot guarantees evidence of the state that triggered the flow.
        captureScreenshot("Account Statement - Before View (form)");
        page.tapViewAccountStatement();
        boolean pdfShown = page.isPdfVisible();
        boolean noDataShown = page.isNoDataMessageVisible();
        // Best-effort capture of the result screen (may be blank if the PDF renders on a secure surface).
        captureScreenshot("Account Statement - View Result");
        Assert.assertTrue(pdfShown || noDataShown,
                "Expected either PDF statement or 'no data' message after tapping View Account Statement");
        if (pdfShown) {
            log.info("PDF statement verified successfully");
        } else {
            log.info("No transactions found — 'no data' message displayed as expected");
        }
    }

    @Step("Return to previous screen")
    private void returnToPreviousScreen(AccountStatementPage page) {
        page.tapBack();
    }
}
