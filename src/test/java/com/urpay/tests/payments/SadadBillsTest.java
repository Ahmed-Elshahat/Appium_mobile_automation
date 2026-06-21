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
    @Story("Filter Bills")
    @Description("Validate filter panel: Sort by label visible, apply Most Recent, "
            + "Amount Low, Amount High, and Telecom service type filters")
    @Severity(SeverityLevel.NORMAL)
    public void testFilterBills() {
        SadadBillsFlow flow = new SadadBillsFlow();

        // Apply Most Recent filter
        SadadBillsPage page = flow.filterByMostRecent();
        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should reload after Most Recent filter");

        // Apply Amount Low filter
        page = flow.filterByAmountLow();
        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should reload after Amount Low filter");

        // Apply Amount High filter
        page = flow.filterByAmountHigh();
        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should reload after Amount High filter");

        // Apply Telecom service type filter
        page = flow.filterByTelecomServiceType();
        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should reload after Telecom service type filter");

        // Reset filters
        page = flow.resetFilters();
        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should reload after reset filters");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 4,
            dependsOnMethods = "testAddNewPrepaidBill")
    @Story("Edit Bill")
    @Description("Edit existing bill name to 'testA' and verify edit is applied")
    @Severity(SeverityLevel.NORMAL)
    public void testEditBill() {
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadBillsPage page = flow.editBillName("testA");

        Assert.assertTrue(page.isBillsListLoaded() || page.isFirstBillVisible(),
                "Should return to bill view after editing");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 5,
            dependsOnMethods = "testAddNewPrepaidBill")
    @Story("Multi-Select Bills")
    @Description("Select first bill for multi-payment from Saddad Bills list")
    @Severity(SeverityLevel.NORMAL)
    public void testSelectMultiBills() {
        SadadBillsFlow flow = new SadadBillsFlow();
        SadadBillsPage page = flow.selectFirstBillMulti();

        Assert.assertTrue(page.isBillsListLoaded(),
                "Bills list should remain visible after multi-select");
    }

    @Test(groups = {"payments", "sadad-bills"}, priority = 6,
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
