package com.urpay.tests.remittance;

import com.urpay.helpers.MTOCorridorHelper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.List;
import java.util.Map;

/**
 * MTO Code Table Refresh & Corridor Discovery.
 *
 * Runs on the DB machine:
 *   1. Triggers MQ jobs to refresh MTO code tables
 *   2. Queries the DB for available corridors
 *   3. Exports results to JSON for inspection
 *   4. Logs table structure (column names + sample data)
 *
 * This is a SETUP/DISCOVERY test — no Appium, no UI.
 * Run it to see what corridors are available before running beneficiary tests.
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/mto-corridor-refresh.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=false
 */
@Epic("Remittance")
@Feature("MTO Code Table Refresh")
public class MTOCorridorRefreshTest {

    private MTOCorridorHelper helper;

    private MTOCorridorHelper getHelper() {
        if (helper == null) {
            helper = new MTOCorridorHelper();
        }
        return helper;
    }

    @Test(priority = 1, groups = {"remittance", "mto-refresh"})
    @Story("Refresh MoneyGram code table via MQ")
    @Description("Trigger RefreshMGCodeTableRq MQ job → verify DB has records")
    public void testRefreshMoneyGramCodeTable() throws Exception {
        int count = getHelper().refreshMoneyGram();
        Assert.assertTrue(count > 0, "MTO_CNTRY_CRNCY_DLV should have records after MoneyGram refresh");
    }

    @Test(priority = 2, groups = {"remittance", "mto-refresh"})
    @Story("Refresh Western Union code table via MQ")
    @Description("Trigger RefreshWUCodeTableRq MQ job → verify DB has records")
    public void testRefreshWUCodeTable() throws Exception {
        int count = getHelper().refreshWesternUnion();
        Assert.assertTrue(count > 0, "MTO_CNTRY_CRNCY_DLV should have records after WU refresh");
    }

    @Test(priority = 3, groups = {"remittance", "mto-refresh"})
    @Story("Refresh Transfast code table via MQ")
    @Description("Trigger RefreshTransfastCodeTableRq MQ job → verify DB has records")
    public void testRefreshTransfastCodeTable() throws Exception {
        int count = getHelper().refreshTransfast();
        Assert.assertTrue(count > 0, "MTO_CNTRY_CRNCY_DLV should have records after Transfast refresh");
    }

    @Test(priority = 4, groups = {"remittance", "mto-refresh"}, dependsOnMethods = "testRefreshMoneyGramCodeTable")
    @Story("Dump table structure and sample corridors")
    @Description("Log all column names and first 5 rows from MTO_CNTRY_CRNCY_DLV")
    public void testDumpTableStructure() throws Exception {
        getHelper().dumpTableStructure();
    }

    @Test(priority = 5, groups = {"remittance", "mto-refresh"}, dependsOnMethods = "testRefreshMoneyGramCodeTable")
    @Story("Query MoneyGram corridors")
    @Description("Get all corridors where provider = MoneyGram")
    public void testGetMoneyGramCorridors() throws Exception {
        List<Map<String, String>> corridors = getHelper().getCorridorsForProvider("MoneyGram");
        System.out.println("========== MONEYGRAM CORRIDORS ==========");
        for (Map<String, String> corridor : corridors) {
            System.out.println(corridor);
        }
        System.out.println("Total MoneyGram corridors: " + corridors.size());
        System.out.println("==========================================");
        Assert.assertFalse(corridors.isEmpty(), "Should find MoneyGram corridors");
    }

    @Test(priority = 6, groups = {"remittance", "mto-refresh"}, dependsOnMethods = "testRefreshMoneyGramCodeTable")
    @Story("Export all corridors to JSON")
    @Description("Save full MTO_CNTRY_CRNCY_DLV table to target/mto-corridors.json")
    public void testExportCorridorsToJson() throws Exception {
        getHelper().exportCorridorsToJson("target/mto-corridors.json");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (helper != null) {
            helper.close();
        }
    }
}
