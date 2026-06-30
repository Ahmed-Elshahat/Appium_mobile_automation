package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LocalTransferFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.LocalTransactionDetailsPage;
import com.urpay.pages.remittance.LocalTransferPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Local Transfer — bank (IBAN) transfer to a local beneficiary (Remittance squad).
 *
 * Migrated from Katalon: Scripts/Remittance/LocalTran/ (Test Suites/Remittance/Local Transfer Test Suite).
 *
 * Strategy: the account already has a PRE-EXISTING ACTIVE local beneficiary ("Noura Faisal"),
 * so each case transfers to it directly. Adding a NEW local beneficiary requires backend IVR
 * activation and is intentionally out of scope; cases SKIP cleanly if no active beneficiary exists.
 *
 * Single LambdaTest session: the first test logs in, the rest reuse the session (dependsOnMethods).
 * Navigation always deep-links to Local Transfer / the Dashboard, so the order is self-correcting.
 *
 *   1. Local transfer to an active beneficiary  → success (Thank You) screen
 *   2. Validate the sender's transaction history → Type/Category/Beneficiary/Amount
 *   3. Local transfer WITH fees                  → success screen
 *   4. Local transfer with an EDITED purpose/note → success screen
 */
@Epic("Remittance")
@Feature("Local Transfer")
public class LocalTransferTest extends BaseTest {

    @Test(priority = 1, groups = {"remittance", "local-transfer", "smoke"})
    @Story("Local Transfer to Active Beneficiary")
    @Description("Login → Local Transfer → amount → select active beneficiary → purpose → confirm → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testLocalTransfer() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("urpayUser.mobileNumber"),
                c.get("urpayUser.id"),
                c.get("urpayUser.verificationCode", "1234"),
                c.get("urpayUser.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        LocalTransferFlow flow = new LocalTransferFlow();
        LocalTransferPage page = flow.openLocalTransferAmount(c.get("localTransfer.amount", "60"));

        if (!flow.selectBeneficiary(page, c.get("localTransfer.beneficiaryName"))) {
            throw new SkipException("No active local beneficiary on account "
                    + c.get("urpayUser.mobileNumber")
                    + " — local transfer needs a pre-activated beneficiary "
                    + "(adding a new one requires backend IVR activation).");
        }

        boolean ok = flow.completeTransfer(page, c.get("urpayUser.verificationCode", "1234"));

        Assert.assertTrue(ok,
                "Success (Thank You) screen should be visible after the local transfer");
    }

    @Test(priority = 2, dependsOnMethods = "testLocalTransfer",
            groups = {"remittance", "local-transfer", "smoke"})
    @Story("Validate Sender Local Transaction History")
    @Description("After transfer → Transactions → open latest → verify Outgoing transfer / Expense / beneficiary / amount")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidateLocalTransactionHistory() {
        ConfigManager c = ConfigManager.getInstance();
        String expectedType = c.get("localTransfer.type", "Outgoing transfer");
        String expectedCategory = c.get("localTransfer.category", "Expense");
        String beneficiary = c.get("localTransfer.beneficiaryName");
        String amount = c.get("localTransfer.amount", "60");

        LocalTransactionDetailsPage details = new LocalTransferFlow().openLastTransaction();

        Assert.assertTrue(details.isLoaded(), "Transaction details should load");
        Assert.assertTrue(details.showsType(expectedType),
                "Latest transaction Type should be '" + expectedType + "' (first row was: "
                + details.getFirstDetail() + ")");
        Assert.assertTrue(details.showsCategory(expectedCategory),
                "Latest transaction Category should be '" + expectedCategory + "'");
        Assert.assertTrue(details.showsBeneficiaryName(beneficiary),
                "Transaction details should show the beneficiary '" + beneficiary + "'");
        Assert.assertTrue(details.showsAmount(amount),
                "Transaction details should show the transferred amount " + amount);
    }

    @Test(priority = 3, dependsOnMethods = "testLocalTransfer",
            groups = {"remittance", "local-transfer"})
    @Story("Local Transfer With Fees")
    @Description("Local Transfer → amount → beneficiary → purpose → include fees → confirm → OTP → success")
    @Severity(SeverityLevel.NORMAL)
    public void testLocalTransferWithFees() {
        ConfigManager c = ConfigManager.getInstance();
        LocalTransferFlow flow = new LocalTransferFlow();

        LocalTransferPage page = flow.openLocalTransferAmount(c.get("localTransfer.amount", "60"));
        if (!flow.selectBeneficiary(page, c.get("localTransfer.beneficiaryName"))) {
            throw new SkipException("No active local beneficiary to run the with-fees transfer");
        }

        boolean ok = flow.completeTransferWithFees(page, c.get("urpayUser.verificationCode", "1234"));

        Assert.assertTrue(ok,
                "Success (Thank You) screen should show after the local transfer with fees");
    }

    @Test(priority = 4, dependsOnMethods = "testLocalTransfer",
            groups = {"remittance", "local-transfer"})
    @Story("Local Transfer Edit Transaction")
    @Description("Local Transfer → amount → beneficiary → purpose → edit pot → another pot → note → confirm → OTP → success")
    @Severity(SeverityLevel.NORMAL)
    public void testLocalTransferEditTransaction() {
        ConfigManager c = ConfigManager.getInstance();
        LocalTransferFlow flow = new LocalTransferFlow();

        LocalTransferPage page = flow.openLocalTransferAmount(c.get("localTransfer.amount", "60"));
        if (!flow.selectBeneficiary(page, c.get("localTransfer.beneficiaryName"))) {
            throw new SkipException("No active local beneficiary to run the edit-transaction transfer");
        }

        boolean ok = flow.completeTransferWithEditedPurpose(page,
                c.get("localTransfer.note", "testing"),
                c.get("urpayUser.verificationCode", "1234"));

        Assert.assertTrue(ok,
                "Success (Thank You) screen should show after the edited local transfer");
    }
}
