package com.urpay.helpers;

import com.urpay.utils.MQJobRunner;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MTO Corridor Helper — triggers MQ code table refresh and queries corridor data.
 *
 * Flow:
 *   1. Trigger MQ job (e.g. RefreshMGCodeTableRq for MoneyGram)
 *   2. Wait for DB to populate
 *   3. Query EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV for corridor data
 *   4. Return available country/currency/delivery combos for a given MTO provider
 *
 * The queried data is used to create international beneficiaries with valid corridors.
 */
public class MTOCorridorHelper {

    private static final Logger log = LoggerFactory.getLogger(MTOCorridorHelper.class);
    private static final String MTO_TABLE = "EPAY_MTO_CORE.MTO_CNTRY_CRNCY_DLV";

    private final MQJobRunner mqRunner;

    public MTOCorridorHelper() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("mq-job-config.properties");
            if (is == null) {
                throw new RuntimeException("mq-job-config.properties not found on classpath");
            }
            this.mqRunner = new MQJobRunner(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MQJobRunner: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════
    //  STEP 1: Trigger MQ Refresh
    // ══════════════════════════════════════════════════

    @Step("Trigger MoneyGram code table refresh via MQ")
    public int refreshMoneyGram() throws Exception {
        return mqRunner.refreshMoneyGramCodeTable();
    }

    @Step("Trigger Western Union code table refresh via MQ")
    public int refreshWesternUnion() throws Exception {
        return mqRunner.refreshWUCodeTable();
    }

    @Step("Trigger Transfast code table refresh via MQ")
    public int refreshTransfast() throws Exception {
        return mqRunner.refreshTransfastCodeTable();
    }

    @Step("Trigger Tahweel (H2H) bank branches refresh via MQ")
    public int refreshTahweel() throws Exception {
        return mqRunner.refreshTahweelBankBranches();
    }

    @Step("Trigger RIA code table refresh via MQ")
    public int refreshRIA() throws Exception {
        return mqRunner.riaRefreshCodeTable();
    }

    @Step("Trigger ALL MTO code table refreshes")
    public void refreshAll() throws Exception {
        mqRunner.runAllJobs();
    }

    // ══════════════════════════════════════════════════
    //  STEP 2: Query Corridor Data from DB
    // ══════════════════════════════════════════════════

    /**
     * Get ALL rows from MTO_CNTRY_CRNCY_DLV.
     * Each row is a Map of column name → value.
     */
    @Step("Query all MTO corridor data from DB")
    public List<Map<String, String>> getAllCorridors() throws Exception {
        String query = "SELECT * FROM " + MTO_TABLE;
        List<Map<String, String>> rows = mqRunner.queryAllRows(query);
        log.info("Retrieved {} total corridor records from {}", rows.size(), MTO_TABLE);
        return rows;
    }

    /**
     * Get corridors filtered by MTO provider name (e.g. "MoneyGram", "WesternUnion").
     * Searches across all columns for the provider name (since we don't know exact column name yet).
     */
    @Step("Query corridors for provider: {providerName}")
    public List<Map<String, String>> getCorridorsForProvider(String providerName) throws Exception {
        List<Map<String, String>> all = getAllCorridors();
        List<Map<String, String>> filtered = all.stream()
                .filter(row -> row.values().stream()
                        .anyMatch(val -> val != null && val.toUpperCase().contains(providerName.toUpperCase())))
                .collect(Collectors.toList());
        log.info("Found {} corridors for provider '{}'", filtered.size(), providerName);
        return filtered;
    }

    /**
     * Get the first corridor matching a provider. Useful for quick single-corridor beneficiary creation.
     */
    @Step("Get first available corridor for: {providerName}")
    public Map<String, String> getFirstCorridor(String providerName) throws Exception {
        List<Map<String, String>> corridors = getCorridorsForProvider(providerName);
        if (corridors.isEmpty()) {
            log.error("No corridors found for provider '{}' — MQ refresh may not have run", providerName);
            return null;
        }
        Map<String, String> first = corridors.get(0);
        log.info("First corridor for '{}': {}", providerName, first);
        return first;
    }

    /**
     * Save all corridor data to a JSON file for inspection.
     */
    @Step("Export all corridors to JSON: {outputPath}")
    public void exportCorridorsToJson(String outputPath) throws Exception {
        mqRunner.saveResultsToJson(outputPath);
        log.info("Corridors exported to: {}", outputPath);
    }

    /**
     * Save corridors for a specific provider to JSON.
     */
    @Step("Export corridors for {providerName} to JSON: {outputPath}")
    public void exportProviderCorridorsToJson(String providerName, String outputPath) throws Exception {
        String query = "SELECT * FROM " + MTO_TABLE;
        mqRunner.saveQueryResultsToJson(query, outputPath);
        log.info("All corridors saved — filter for '{}' in the JSON", providerName);
    }

    // ══════════════════════════════════════════════════
    //  STEP 3: Refresh + Query in one call
    // ══════════════════════════════════════════════════

    /**
     * Full flow: trigger MQ refresh for MoneyGram, then return its corridors.
     */
    @Step("Refresh MoneyGram code table and return available corridors")
    public List<Map<String, String>> refreshAndGetMoneyGramCorridors() throws Exception {
        refreshMoneyGram();
        return getCorridorsForProvider("MoneyGram");
    }

    /**
     * Full flow: trigger MQ refresh for WU, then return its corridors.
     */
    @Step("Refresh WU code table and return available corridors")
    public List<Map<String, String>> refreshAndGetWUCorridors() throws Exception {
        refreshWesternUnion();
        return getCorridorsForProvider("WesternUnion");
    }

    /**
     * Full flow: trigger MQ refresh for Transfast, then return its corridors.
     */
    @Step("Refresh Transfast code table and return available corridors")
    public List<Map<String, String>> refreshAndGetTransfastCorridors() throws Exception {
        refreshTransfast();
        return getCorridorsForProvider("Transfast");
    }

    // ══════════════════════════════════════════════════
    //  UTILITY: Print corridor info for debugging
    // ══════════════════════════════════════════════════

    /**
     * Log all column names and first 5 rows (for discovering table structure).
     */
    @Step("Dump table structure and sample rows")
    public void dumpTableStructure() throws Exception {
        List<Map<String, String>> rows = getAllCorridors();
        if (rows.isEmpty()) {
            log.warn("Table is EMPTY — need to trigger MQ refresh first");
            return;
        }

        // Print column names
        Map<String, String> firstRow = rows.get(0);
        log.info("========== MTO_CNTRY_CRNCY_DLV COLUMNS ==========");
        log.info("Columns: {}", firstRow.keySet());
        log.info("==================================================");

        // Print first 5 rows
        int limit = Math.min(5, rows.size());
        for (int i = 0; i < limit; i++) {
            log.info("Row {}: {}", i + 1, rows.get(i));
        }
        log.info("Total rows: {}", rows.size());
    }

    /**
     * Close DB connection.
     */
    public void close() {
        mqRunner.close();
    }
}
