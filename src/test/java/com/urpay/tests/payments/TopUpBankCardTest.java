package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.TopUpBankCardFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.TopUpPage;
import com.urpay.pages.payments.TopUpSettingsPage;
import com.urpay.pages.payments.TransactionDetailsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Top Up Using Bank Card Test Suite — validates the complete top-up lifecycle:
 *   1. Login with topup user
 *   2. Validate wallet balance before top-up
 *   3. Top up with new bank card (user has no saved cards)
 *   4. Validate wallet balance after top-up
 *   5. Validate transaction details
 *   6. Top up with existing saved card
 *   7. Auto top-up setup (first time)
 *   8. Auto top-up limit configuration
 *   9. Auto top-up 3D Secure confirmation
 *  10. Auto top-up enable/disable toggle
 *  11. Delete auto top-up card
 *  12. Delete saved top-up card
 *
 * Katalon source:
 *   Test Suites/PaymentAndCards/TopUpUsingBankCard.ts
 *   Scripts/PaymntAndCards/TOPUpUsingBankCard/*
 *
 * Test Data (from sit-cards.properties, topup.* prefix):
 *   User: 0520228810 / 1766117517
 *   Card: 4012001037141112 (Visa test card), expiry 12/27
 *   Amounts: 600 (new card), 500 (existing card)
 *   3DS OTP: 123123
 */
@Epic("Payments & Cards")
@Feature("Top Up Using Bank Card")
public class TopUpBankCardTest extends BaseTest {

    private double initialWalletBalance;

    // ══════════════════════════════════════════════════
    //  LOGIN
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard", "smoke"}, priority = 1)
    @Story("Top-Up Login")
    @Description("Login with top-up user and verify dashboard is loaded")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginForTopUp() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("topup.mobileNumber"),
                c.get("topup.id"),
                c.get("topup.verificationCode", "1234"),
                c.get("topup.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
    }

    // ══════════════════════════════════════════════════
    //  WALLET BALANCE BEFORE TOP-UP
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard"}, priority = 2,
            dependsOnMethods = "testLoginForTopUp")
    @Story("Wallet Balance Before Top-Up")
    @Description("Capture wallet balance before performing top-up for comparison after")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetWalletBalanceBeforeTopUp() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        initialWalletBalance = flow.getWalletBalanceAsDouble();
        log.info("Initial wallet balance: {}", initialWalletBalance);
        Assert.assertTrue(initialWalletBalance >= 0,
                "Wallet balance should be non-negative");
    }

    // ══════════════════════════════════════════════════
    //  TOP UP WITH NEW BANK CARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard"}, priority = 3,
            dependsOnMethods = "testGetWalletBalanceBeforeTopUp")
    @Story("Top Up With New Bank Card")
    @Description("Top up wallet with a new bank card: enter card details manually, "
            + "save card with nickname, enter amount 600, complete 3D Secure, verify success")
    @Severity(SeverityLevel.CRITICAL)
    public void testTopUpWithNewBankCard() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        TopUpPage page = flow.topUpWithNewCard();
        // After Done is tapped, we should be back on dashboard
        DashboardPage dashboard = new DashboardPage();
        Assert.assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after successful top-up");
    }

    // ══════════════════════════════════════════════════
    //  WALLET BALANCE AFTER TOP-UP
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard"}, priority = 4,
            dependsOnMethods = "testTopUpWithNewBankCard")
    @Story("Wallet Balance After Top-Up")
    @Description("Verify wallet balance increased by the top-up amount (600 SAR)")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateWalletBalanceAfterNewCardTopUp() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        double topUpAmount = ConfigManager.getInstance()
                .getDouble("topup.newCardAmount", 600.0);
        double expectedBalance = initialWalletBalance + topUpAmount;
        double actualBalance = flow.getWalletBalanceAsDouble();

        log.info("Expected balance: {}, Actual balance: {}", expectedBalance, actualBalance);
        Assert.assertEquals(actualBalance, expectedBalance, 0.01,
                "Wallet balance should increase by " + topUpAmount);
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTION DETAILS AFTER TOP-UP
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard"}, priority = 5,
            dependsOnMethods = "testTopUpWithNewBankCard")
    @Story("Transaction Details After Top-Up")
    @Description("Verify latest transaction: type='Add Money', category='Income', "
            + "reference number not empty, amount not 0.00, VAT not 0.00, fees not 0.00")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateTransactionDetailsAfterTopUp() {
        ConfigManager c = ConfigManager.getInstance();
        TopUpBankCardFlow flow = new TopUpBankCardFlow();

        TransactionDetailsPage detailsPage = flow.viewLatestTransactionDetails();

        // Verify transaction type
        String transactionType = detailsPage.getType();
        Assert.assertEquals(transactionType, c.get("topup.expectedTransactionType", "Add Money"),
                "Transaction type should be 'Add Money'");

        // Verify category
        String category = detailsPage.getCategory();
        Assert.assertEquals(category, c.get("topup.expectedTransactionCategory", "Income"),
                "Transaction category should be 'Income'");

        // Verify reference number not empty
        String referenceNumber = detailsPage.getReferenceNumber();
        Assert.assertNotNull(referenceNumber, "Reference number should not be null");
        Assert.assertFalse(referenceNumber.trim().isEmpty(),
                "Reference number should not be empty");

        // Verify fees and VAT are not 0.00
        String fees = detailsPage.getTransactionFees();
        Assert.assertNotEquals(fees, "0.00",
                "Transaction fees should not be 0.00");

        String vat = detailsPage.getVat();
        Assert.assertNotEquals(vat, "0.00",
                "VAT should not be 0.00");

        log.info("Transaction verified — Type: {}, Category: {}, Ref: {}, Fees: {}, VAT: {}",
                transactionType, category, referenceNumber, fees, vat);

        // Navigate back
        detailsPage.tapBack();
    }

    // ══════════════════════════════════════════════════
    //  TOP UP WITH EXISTING CARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard"}, priority = 6,
            dependsOnMethods = "testTopUpWithNewBankCard")
    @Story("Top Up With Existing Card")
    @Description("Top up wallet using the previously saved bank card, amount 500, verify success")
    @Severity(SeverityLevel.CRITICAL)
    public void testTopUpWithExistingCard() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        TopUpPage page = flow.topUpWithExistingCard();
        DashboardPage dashboard = new DashboardPage();
        Assert.assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after top-up with existing card");
    }

    @Test(groups = {"payments", "topup-bankcard"}, priority = 7,
            dependsOnMethods = "testTopUpWithExistingCard")
    @Story("Transaction Details After Existing Card Top-Up")
    @Description("Verify transaction details match 'Add Money' / 'Income' after existing card top-up")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateTransactionDetailsAfterExistingCardTopUp() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();

        TransactionDetailsPage detailsPage = flow.viewLatestTransactionDetails();

        String transactionType = detailsPage.getType();
        Assert.assertEquals(transactionType, "Add Money",
                "Transaction type should be 'Add Money'");

        String category = detailsPage.getCategory();
        Assert.assertEquals(category, "Income",
                "Transaction category should be 'Income'");

        detailsPage.tapBack();
    }

    // ══════════════════════════════════════════════════
    //  AUTO TOP-UP SETUP
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 8,
            dependsOnMethods = "testTopUpWithExistingCard")
    @Story("Auto Top-Up Setup")
    @Description("Set up auto top-up for the first time: navigate to settings, "
            + "tap Set Auto Top-Up → Next")
    @Severity(SeverityLevel.CRITICAL)
    public void testSetUpAutoTopUpForFirstTime() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        flow.goToDashboard();
        TopUpSettingsPage page = flow.setupAutoTopUpForFirstTime();
        log.info("Auto top-up initial setup completed");
    }

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 9,
            dependsOnMethods = "testSetUpAutoTopUpForFirstTime")
    @Story("Auto Top-Up Set Amount Limit")
    @Description("Set auto top-up amount limit to 1000 SAR")
    @Severity(SeverityLevel.NORMAL)
    public void testAutoTopUpSetAmountLimit() {
        ConfigManager c = ConfigManager.getInstance();
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        flow.setAutoTopUpAmountLimit(c.get("topup.autoTopUp.limit", "1000"));
    }

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 10,
            dependsOnMethods = "testAutoTopUpSetAmountLimit")
    @Story("Auto Top-Up Set Lower Limit")
    @Description("Set auto top-up lower limit to 500 SAR")
    @Severity(SeverityLevel.NORMAL)
    public void testAutoTopUpSetLowerLimit() {
        ConfigManager c = ConfigManager.getInstance();
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        flow.setAutoTopUpLowerLimit(c.get("topup.autoTopUp.lowerLimit", "500"));
    }

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 11,
            dependsOnMethods = "testAutoTopUpSetLowerLimit")
    @Story("Auto Top-Up 3D Secure Confirmation")
    @Description("Confirm auto top-up setup: accept terms → confirm → "
            + "passcode → 3D Secure OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfirmAutoTopupWith3dSecure() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        TopUpSettingsPage page = flow.confirmAutoTopUpWith3dSecure();
        log.info("Auto top-up confirmed with 3D Secure verification");
    }

    // ══════════════════════════════════════════════════
    //  AUTO TOP-UP MANAGEMENT
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 12,
            dependsOnMethods = "testConfirmAutoTopupWith3dSecure")
    @Story("Open Auto Top-Up Settings")
    @Description("Navigate from dashboard to auto top-up card settings")
    @Severity(SeverityLevel.NORMAL)
    public void testOpenAutoTopUpSettings() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        flow.goToDashboard();
        TopUpSettingsPage page = flow.openAutoTopUpCardSettings();
        Assert.assertTrue(page.isAutoTopUpToggleVisible(10),
                "Auto top-up toggle should be visible on settings page");
    }

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 13,
            dependsOnMethods = "testOpenAutoTopUpSettings")
    @Story("Auto Top-Up Disable and Enable")
    @Description("Toggle auto top-up OFF → verify disable message → "
            + "toggle ON → verify enable message")
    @Severity(SeverityLevel.CRITICAL)
    public void testAutoTopUpDisableAndEnable() {
        ConfigManager c = ConfigManager.getInstance();
        TopUpBankCardFlow flow = new TopUpBankCardFlow();

        // Disable auto top-up
        String disableMsg = flow.toggleAutoTopUp();
        Assert.assertEquals(disableMsg,
                c.get("topup.expectedDisableMsg",
                        "You have disabled the auto top-up successfully."),
                "Disable message should match expected");

        // Wait for notification to dismiss before re-enabling
        TopUpSettingsPage page = new TopUpSettingsPage();
        page.waitForNotificationToDismiss(5);

        // Re-enable auto top-up
        String enableMsg = flow.toggleAutoTopUp();
        Assert.assertEquals(enableMsg,
                c.get("topup.expectedEnableMsg",
                        "You have enabled the auto-top-up successfully."),
                "Enable message should match expected");
    }

    @Test(groups = {"payments", "topup-bankcard", "auto-topup"}, priority = 14,
            dependsOnMethods = "testAutoTopUpDisableAndEnable")
    @Story("Delete Auto Top-Up Card")
    @Description("Delete auto top-up card → verify confirmation dialog → "
            + "confirm → verify 'No auto top-up' banner visible")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteAutoTopUpCard() {
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        TopUpSettingsPage page = flow.deleteAutoTopUpCard();
        Assert.assertTrue(page.isNoAutoTopUpBannerVisible(10),
                "No auto top-up banner should be visible after deletion");
    }

    // ══════════════════════════════════════════════════
    //  DELETE SAVED TOP-UP CARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"payments", "topup-bankcard"}, priority = 15,
            dependsOnMethods = "testDeleteAutoTopUpCard")
    @Story("Delete Saved Top-Up Card")
    @Description("Navigate to profile → Manage Top Up Cards → Delete card → "
            + "verify 'Card has been deleted' notification")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteTopUpCard() {
        ConfigManager c = ConfigManager.getInstance();
        TopUpBankCardFlow flow = new TopUpBankCardFlow();
        flow.goToDashboard();

        String message = flow.deleteTopUpCard();
        Assert.assertEquals(message,
                c.get("topup.expectedCardDeletedMsg", "Card has been deleted"),
                "Card deletion message should match expected");
    }
}
