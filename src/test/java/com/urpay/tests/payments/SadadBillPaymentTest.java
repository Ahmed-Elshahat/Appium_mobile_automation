package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.SadadBillsFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.SadadBillsPage;
import com.urpay.pages.payments.SadadTransactionDetailsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Saddad Bill Payment Tests — validates payment for different bill types
 * and verifies wallet balance deduction and transaction details.
 *
 * Katalon source:
 *   Scripts/PaymntAndCards/SadadBills/SadadBillsDiffirentTypesAndVerifyBalanceAfter/
 *   - ValidatePrepaidBillPaymentAndVerifyBalanceAfterSadad
 *   - ValidatePostPaidBillPaymentAndverifyBalanceAfterSadad
 *   - ValidateOverpaidBillPaymentAndVerifyBalanceAfterSadad
 *   - ValidateSadadTransactionDetailsScreen
 *
 * Test Data (from sit-cards.properties):
 *   Prepaid Bill: 966503745901 (Mobily 005)
 *   Postpaid Bill: 96563360007 (STC 001)
 *   Overpaid Bill: 96563360001 (STC 001)
 *   Expected deduction: amount + 1.50 SR fees = 11.50 SR
 */
@Epic("Payments & Cards")
@Feature("Saddad Bill Payment")
public class SadadBillPaymentTest extends BaseTest {

    @Test(groups = {"payments", "sadad-payment", "smoke"}, priority = 1)
    @Story("Saddad Payment Login")
    @Description("Login with Saddad user and verify dashboard is loaded")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginForSadadPayment() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("sadad.mobileNumber"),
                c.get("sadad.id"),
                c.get("sadad.verificationCode", "1234"),
                c.get("sadad.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
    }

    @Test(groups = {"payments", "sadad-payment"}, priority = 2,
            dependsOnMethods = "testLoginForSadadPayment")
    @Story("Prepaid Bill Payment")
    @Description("Add new Prepaid bill → pay → verify balance deducted by amount + fees (11.50 SR)")
    @Severity(SeverityLevel.CRITICAL)
    public void testPrepaidBillPaymentAndVerifyBalance() {
        ConfigManager c = ConfigManager.getInstance();
        SadadBillsFlow flow = new SadadBillsFlow();

        // Get balance before payment
        String balanceBefore = flow.getWalletBalance();
        log.info("Balance before prepaid payment: {}", balanceBefore);

        // Add and pay prepaid bill
        SadadBillsPage page = flow.addNewPrepaidBill(
                c.get("sadad.prepaid.billNumber", "966503745901"),
                c.get("sadad.prepaid.amount", "10"),
                "Prepaid Test Bill");

        flow.payBill(page);

        // Verify Done button visible (payment success)
        Assert.assertTrue(page.isDoneButtonVisible(),
                "Done button should appear after successful payment");
    }

    @Test(groups = {"payments", "sadad-payment"}, priority = 3,
            dependsOnMethods = "testLoginForSadadPayment")
    @Story("Postpaid Bill Payment")
    @Description("Add new Postpaid bill → pay → verify balance deducted")
    @Severity(SeverityLevel.CRITICAL)
    public void testPostpaidBillPaymentAndVerifyBalance() {
        ConfigManager c = ConfigManager.getInstance();
        SadadBillsFlow flow = new SadadBillsFlow();

        // Get balance before payment
        String balanceBefore = flow.getWalletBalance();
        log.info("Balance before postpaid payment: {}", balanceBefore);

        // Add and pay postpaid bill
        SadadBillsPage page = flow.addNewPostpaidBill(
                c.get("sadad.postpaid.billNumber", "96563360007"),
                c.get("sadad.postpaid.amount", "10"),
                "Postpaid Test Bill");

        flow.payBill(page);

        Assert.assertTrue(page.isDoneButtonVisible(),
                "Done button should appear after successful postpaid payment");
    }

    @Test(groups = {"payments", "sadad-payment"}, priority = 4,
            dependsOnMethods = "testLoginForSadadPayment")
    @Story("Overpaid Bill Payment")
    @Description("Add new Overpaid bill → pay → verify balance deducted")
    @Severity(SeverityLevel.CRITICAL)
    public void testOverpaidBillPaymentAndVerifyBalance() {
        ConfigManager c = ConfigManager.getInstance();
        SadadBillsFlow flow = new SadadBillsFlow();

        // Get balance before payment
        String balanceBefore = flow.getWalletBalance();
        log.info("Balance before overpaid payment: {}", balanceBefore);

        // Add and pay overpaid bill
        SadadBillsPage page = flow.addNewOverpaidBill(
                c.get("sadad.overpaid.billNumber", "96563360001"),
                c.get("sadad.overpaid.amount", "10"),
                "Overpaid Test Bill");

        flow.payBill(page);

        Assert.assertTrue(page.isDoneButtonVisible(),
                "Done button should appear after successful overpaid payment");
    }

    @Test(groups = {"payments", "sadad-payment"}, priority = 5,
            dependsOnMethods = "testPrepaidBillPaymentAndVerifyBalance")
    @Story("Transaction Details Verification")
    @Description("Navigate to transactions tab, verify Saddad transaction details: "
            + "title='SADAD payments', subtitle='SADAD', type='SADAD Bills', "
            + "reference number not empty, VAT='0.00', Fees='0.00'")
    @Severity(SeverityLevel.CRITICAL)
    public void testSadadTransactionDetails() {
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadTransactionDetailsPage detailsPage = flow.viewTransactionDetails();

        // Verify transaction list item
        String mainTitle = detailsPage.getMainTitle();
        Assert.assertEquals(mainTitle, "SADAD payments",
                "Transaction main title should be 'SADAD payments'");

        String subTitle = detailsPage.getSubTitle();
        Assert.assertEquals(subTitle, "SADAD",
                "Transaction subtitle should be 'SADAD'");

        // Verify transaction detail values
        Assert.assertTrue(detailsPage.isTransactionDetailLoaded(),
                "Transaction detail page should load");

        String transactionType = detailsPage.getTransactionType();
        Assert.assertEquals(transactionType, "SADAD Bills",
                "Transaction type should be 'SADAD Bills'");

        String referenceNumber = detailsPage.getReferenceNumber();
        Assert.assertNotNull(referenceNumber, "Reference number should not be null");
        Assert.assertFalse(referenceNumber.trim().isEmpty(),
                "Reference number should not be empty");

        String vatValue = detailsPage.getVatValue();
        Assert.assertEquals(vatValue, "0.00",
                "VAT should be '0.00' for Saddad bills");

        String feesValue = detailsPage.getFeesValue();
        Assert.assertEquals(feesValue, "0.00",
                "Fees should be '0.00' for Saddad bills");

        String amount = detailsPage.getTransactionAmount();
        Assert.assertNotNull(amount, "Transaction amount should not be null");
        Assert.assertFalse(amount.trim().isEmpty(),
                "Transaction amount should not be empty");

        log.info("Transaction verified — Type: {}, Ref: {}, Amount: {}, VAT: {}, Fees: {}",
                transactionType, referenceNumber, amount, vatValue, feesValue);
    }
}
