package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.DriverFactory;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.AccountDetailsPage;
import com.urpay.pages.wallet.AccountStatementPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Account Statement flow — orchestrates navigation to Account Statement screen.
 *
 * Navigation: Dashboard → Wallet → Account Details → Account Statement
 *
 * Katalon source: Scripts/Wallet_VAS/AccountStatement/
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns page objects/values for test to verify)
 *   - NO hardcoded values
 */
public class AccountStatementFlow {

    private static final Logger log = LoggerFactory.getLogger(AccountStatementFlow.class);
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final DashboardPage dashboardPage;

    public AccountStatementFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.dashboardPage = new DashboardPage();
    }

    @Step("Navigate to Account Statement via Wallet → Account Details → Statement")
    public AccountStatementPage navigateToAccountStatement() {
        dashboardPage.clickWallet();
        AccountDetailsPage accountDetailsPage = new AccountDetailsPage();
        accountDetailsPage.waitUntilLoaded();
        log.info("Account Details page loaded");

        accountDetailsPage.tapAccountStatement();
        AccountStatementPage page = new AccountStatementPage();
        page.waitUntilLoaded();
        log.info("Account Statement page loaded");
        return page;
    }

    @Step("Select date range: from previous month to today")
    public void selectLastMonthDateRange(AccountStatementPage page) {
        page.tapFromDate();
        page.tapPreviousMonth();
        page.tapDateDialogOk();
        log.info("From date set to previous month");

        page.tapToDate();
        page.tapDateDialogOk();
        log.info("To date set to today");
    }
}
