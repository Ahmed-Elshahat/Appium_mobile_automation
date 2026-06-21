package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.AccountDetailsPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Account Details flow — orchestrates navigation to Account Details.
 *
 * Navigation: Dashboard → Tap Wallet button → Account Details screen
 *
 * Katalon source: Scripts/Wallet_VAS/AccountDetails/
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns page objects/values for test to verify)
 *   - NO hardcoded credentials (reads from ConfigManager)
 */
public class AccountDetailsFlow {

    private static final Logger log = LoggerFactory.getLogger(AccountDetailsFlow.class);
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final DashboardPage dashboardPage;

    public AccountDetailsFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.dashboardPage = new DashboardPage();
    }

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    /**
     * Navigate from Dashboard to Account Details by tapping the Wallet button.
     * Matches Katalon step: tap walletButton → wait for Account Details header.
     *
     * @return AccountDetailsPage for test assertions
     */
    @Step("Navigate to Account Details via Wallet button")
    public AccountDetailsPage navigateToAccountDetails() {
        dashboardPage.clickWallet();

        // Wait for Account Details screen to load (header text)
        waits.waitForVisible(
                AppiumBy.xpath("//android.widget.TextView[@text='Account Details']"), 15);
        log.info("Account Details page loaded");

        return new AccountDetailsPage();
    }

    /**
     * Get the wallet balance text from the Dashboard BEFORE navigating.
     * Mirrors Katalon: CustomKeywords.'com.uspace.wallet.keyword.Wallet.getWalletBalance'()
     *
     * @return balance text from dashboard (e.g., "1,990.00")
     */
    @Step("Get wallet balance from Dashboard")
    public String getDashboardBalance() {
        String balanceText = dashboardPage.getBalanceText();
        log.info("Dashboard balance: {}", balanceText);
        return balanceText;
    }
}
