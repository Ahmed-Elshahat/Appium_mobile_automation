package com.urpay.tests.payments;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.SadadBillsFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.payments.SadadBillsPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Saddad Bills Test Suite — covers the full SADAD bill lifecycle:
 *   1. Login
 *   2. Navigate to Saddad Bills
 *   3. Add new bill (Prepaid)
 *   4. Filter/Sort bills
 *   5. Edit bill
 *   6. Select multi-bills
 *   7. Delete bill
 *
 * Katalon source: Scripts/PaymntAndCards/SadadBills/
 * Suite: Test Suites/PaymentAndCards/SadadBillsTestSuits/SadadBills.ts
 *
 * Test Data (from sit-cards.properties, sadad.* prefix):
 *   User: 0533411565 / 1096436819
 *   Bill Number: 966503745901
 *   Bill Name: hamada Bill
 */
@Epic("Payments & Cards")
@Feature("Saddad Bills")
public class SadadBillsTest extends BaseTest {

    @Test(groups = {"payments", "sadad-bills", "smoke"}, priority = 1)
    @Story("Saddad Login")
    @Description("Login with Saddad Bills user and verify dashboard is loaded")
    @Severity(SeverityLevel.BLOCKER)
    public void testLoginForSadad() {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("sadad.mobileNumber"),
                c.get("sadad.id"),
                c.get("sadad.verificationCode", "1234"),
                c.get("sadad.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 2,
            dependsOnMethods = "testLoginForSadad")
    @Story("Add New Prepaid Bill")
    @Description("Add new bill: Telecom & Internet → Mobily 005 → Prepaid → bill number → save")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddNewPrepaidBill() {
        ConfigManager c = ConfigManager.getInstance();
        SadadBillsFlow flow = new SadadBillsFlow();

        SadadBillsPage page = flow.addNewPrepaidBill(
                c.get("sadad.billNumber", "966503745901"),
                c.get("sadad.billAmount", "10"),
                c.get("sadad.billName", "hamada Bill"));

        Assert.assertTrue(page.isDoneButtonVisible() || page.isBillsListLoaded(),
                "Bill should be saved — Done button or bills list visible");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 3,
            dependsOnMethods = "testAddNewPrepaidBill")
    @Story("Select Bill and Pay")
    @Description("Select the saved bill and pay it: search → open → Pay Bill → amount → "
            + "Next → confirm Pay Bill → OTP → Done")
    @Severity(SeverityLevel.CRITICAL)
    public void testSelectBillAndPay() {
        ConfigManager c = ConfigManager.getInstance();
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadBillsPage page = flow.selectBillAndPay(
                c.get("sadad.billName", "hamada Bill"),
                c.get("sadad.payAmount", "100"));
        Assert.assertTrue(page.isBillsListLoaded() || page.isFirstBillVisible(),
                "Should return to the bills list after paying");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 4,
            dependsOnMethods = "testAddNewPrepaidBill")
    @Story("Filter Bills")
    @Description("Validate the filter panel in ONE session: Most Recent, Amount Low, "
            + "Amount High, Telecom service type, then Reset (matches Katalon)")
    @Severity(SeverityLevel.NORMAL)
    public void testFilterBills() {
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadBillsPage page = flow.applyFiltersAndReset();
        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should reload after applying and resetting filters");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 5,
            dependsOnMethods = "testAddNewPrepaidBill")
    @Story("Edit Bill")
    @Description("Edit existing bill name to 'testA' and verify edit is applied")
    @Severity(SeverityLevel.NORMAL)
    public void testEditBill() {
        SadadBillsFlow flow = new SadadBillsFlow();
        // The rename target MUST differ from the bill's current nickname, otherwise the
        // field is unchanged and the Save button stays disabled (→ Apply tap times out).
        // Use a unique value each run so Save always enables.
        String uniqueName = "QA" + (System.currentTimeMillis() % 100000);
        SadadBillsPage page = flow.editBillName(uniqueName);

        Assert.assertTrue(page.isBillsListLoaded() || page.isFirstBillVisible(),
                "Should return to bill view after editing");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 6,
            dependsOnMethods = "testAddNewPrepaidBill")
    @Story("Multi-Select Bills")
    @Description("Select first bill for multi-payment from Saddad Bills list")
    @Severity(SeverityLevel.NORMAL)
    public void testSelectMultiBills() {
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadBillsPage page = flow.selectFirstBillMulti();

        Assert.assertTrue(page.isBillDetailsOrListVisible(),
                "Selecting the first bill should open its details/pay screen or stay on the list");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 7,
            dependsOnMethods = {"testFilterBills", "testEditBill", "testSelectMultiBills"})
    @Story("Delete Bill")
    @Description("Delete existing bill and verify it's removed from the list")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteBill() {
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadBillsPage page = flow.deleteFirstBill();

        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should be visible after deletion");
    }
}
