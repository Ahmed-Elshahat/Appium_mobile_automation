package com.urpay.helpers;

import com.urpay.utils.MQJobRunner;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
     * Only returns ENABLED corridors that support ADD_BENEFICIARY journey.
     */
    @Step("Query enabled corridors for provider: {providerName}")
    public List<Map<String, String>> getCorridorsForProvider(String providerName) throws Exception {
        String query = "SELECT MTO_CODE, CNTRY_ISO3_CODE, CRNCY_ISO3_CODE, DELIVERY_OPTION_CODE, "
                + "IS_ACTIVE, IS_ENABLED, JOURNEY_TYPES "
                + "FROM " + MTO_TABLE + " "
                + "WHERE MTO_CODE = '" + providerName + "' "
                + "AND IS_ENABLED = 'Y' "
                + "AND JOURNEY_TYPES LIKE '%ADD_BENEFICIARY%' "
                + "ORDER BY CNTRY_ISO3_CODE";
        List<Map<String, String>> rows = mqRunner.queryAllRows(query);
        log.info("Found {} enabled corridors for provider '{}' (ADD_BENEFICIARY)", rows.size(), providerName);
        for (Map<String, String> row : rows) {
            log.info("  Corridor: {} → {} | currency={} | delivery={}",
                    providerName, row.get("CNTRY_ISO3_CODE"), row.get("CRNCY_ISO3_CODE"),
                    row.get("DELIVERY_OPTION_CODE"));
        }
        return rows;
    }

    /**
     * Get the first corridor matching a provider. Useful for quick single-corridor beneficiary creation.
     */
    @Step("Get first available corridor for: {providerName}")
    public Map<String, String> getFirstCorridor(String providerName) throws Exception {
        List<Map<String, String>> corridors = getCorridorsForProvider(providerName);
        if (corridors.isEmpty()) {
            log.error("No enabled corridors found for provider '{}' — MQ refresh may not have run or none are enabled", providerName);
            return null;
        }
        Map<String, String> first = corridors.get(0);
        log.info("First corridor for '{}': country={}, currency={}, delivery={}",
                providerName, first.get("CNTRY_ISO3_CODE"), first.get("CRNCY_ISO3_CODE"),
                first.get("DELIVERY_OPTION_CODE"));
        return first;
    }

    // ══════════════════════════════════════════════════
    //  MAPPING: DB codes → App UI text
    // ══════════════════════════════════════════════════

    /**
     * Map DB DELIVERY_OPTION_CODE to the text shown in the app UI.
     */
    public static String deliveryCodeToAppText(String deliveryOptionCode) {
        if (deliveryOptionCode == null) return "Cash pickup";
        switch (deliveryOptionCode) {
            case "BANK_DEPOSIT": return "Bank Deposit";
            case "WILL_CALL": return "Cash pickup";
            case "DIRECT_TO_ACCT": return "Send to Wallet";
            default: return deliveryOptionCode;
        }
    }

    /**
     * Map ISO3 country code to country name (for app UI selection).
     */
    public static String countryCodeToName(String iso3) {
        if (iso3 == null) return "";
        switch (iso3) {
            case "IND": return "India";
            case "PAK": return "Pakistan";
            case "EGY": return "Egypt";
            case "BGD": return "Bangladesh";
            case "LKA": return "Sri Lanka";
            case "PHL": return "Philippines";
            case "IDN": return "Indonesia";
            case "NPL": return "Nepal";
            case "JOR": return "Jordan";
            case "SDN": return "Sudan";
            case "NGA": return "Nigeria";
            case "KEN": return "Kenya";
            case "GHA": return "Ghana";
            case "ETH": return "Ethiopia";
            case "TUR": return "Turkey";
            case "USA": return "United States";
            case "GBR": return "United Kingdom";
            case "CAN": return "Canada";
            case "AUS": return "Australia";
            case "ARE": return "United Arab Emirates";
            case "SAU": return "Saudi Arabia";
            case "OMN": return "Oman";
            case "KWT": return "Kuwait";
            case "QAT": return "Qatar";
            case "IRQ": return "Iraq";
            default: return iso3; // fallback to code
        }
    }

    /**
     * Map ISO3 currency code to currency display name (for app UI selection).
     */
    public static String currencyCodeToName(String iso3) {
        if (iso3 == null) return "";
        switch (iso3) {
            case "INR": return "Indian Rupee";
            case "PKR": return "Pakistani Rupee";
            case "EGP": return "Egyptian Pound";
            case "USD": return "US Dollar";
            case "EUR": return "Euro";
            case "GBP": return "British Pound";
            case "BDT": return "Bangladeshi Taka";
            case "LKR": return "Sri Lankan Rupee";
            case "PHP": return "Philippine Peso";
            case "IDR": return "Indonesian Rupiah";
            case "NPR": return "Nepalese Rupee";
            case "JOD": return "Jordanian Dinar";
            case "SAR": return "Saudi Riyal";
            case "AED": return "UAE Dirham";
            case "TRY": return "Turkish Lira";
            default: return iso3;
        }
    }

    /**
     * Get corridors for a specific provider + delivery type.
     * Prefers enabled corridors for popular countries (IND, EGY, PAK, PHL, BGD, LKA).
     */
    @Step("Query corridors for {providerName} with delivery: {deliveryCode}")
    public List<Map<String, String>> getCorridorsForDelivery(String providerName, String deliveryCode) throws Exception {
        // First try: enabled corridors for popular countries
        String query = "SELECT MTO_CODE, CNTRY_ISO3_CODE, CRNCY_ISO3_CODE, DELIVERY_OPTION_CODE, "
                + "IS_ACTIVE, IS_ENABLED, JOURNEY_TYPES "
                + "FROM " + MTO_TABLE + " "
                + "WHERE MTO_CODE = '" + providerName + "' "
                + "AND DELIVERY_OPTION_CODE = '" + deliveryCode + "' "
                + "AND IS_ENABLED = 'Y' "
                + "AND CNTRY_ISO3_CODE IN ('IND','EGY','PAK','PHL','BGD','LKA','NPL','IDN') "
                + "ORDER BY CNTRY_ISO3_CODE";
        List<Map<String, String>> rows = mqRunner.queryAllRows(query);

        // Fallback: any enabled corridor with this delivery type
        if (rows.isEmpty()) {
            query = "SELECT MTO_CODE, CNTRY_ISO3_CODE, CRNCY_ISO3_CODE, DELIVERY_OPTION_CODE, "
                    + "IS_ACTIVE, IS_ENABLED, JOURNEY_TYPES "
                    + "FROM " + MTO_TABLE + " "
                    + "WHERE MTO_CODE = '" + providerName + "' "
                    + "AND DELIVERY_OPTION_CODE = '" + deliveryCode + "' "
                    + "AND IS_ENABLED = 'Y' "
                    + "ORDER BY CNTRY_ISO3_CODE";
            rows = mqRunner.queryAllRows(query);
        }

        // Last fallback: any corridor (even disabled) for popular countries — beneficiary will be DB-activated
        if (rows.isEmpty()) {
            log.warn("No enabled {} corridors for {} — falling back to disabled corridors for popular countries",
                    deliveryCode, providerName);
            query = "SELECT MTO_CODE, CNTRY_ISO3_CODE, CRNCY_ISO3_CODE, DELIVERY_OPTION_CODE, "
                    + "IS_ACTIVE, IS_ENABLED, JOURNEY_TYPES "
                    + "FROM " + MTO_TABLE + " "
                    + "WHERE MTO_CODE = '" + providerName + "' "
                    + "AND DELIVERY_OPTION_CODE = '" + deliveryCode + "' "
                    + "AND CNTRY_ISO3_CODE IN ('IND','EGY','PAK','PHL','BGD','LKA') "
                    + "ORDER BY CNTRY_ISO3_CODE";
            rows = mqRunner.queryAllRows(query);
        }

        log.info("Found {} corridors for {} + {}", rows.size(), providerName, deliveryCode);
        for (Map<String, String> row : rows) {
            log.info("  {} → {} | {} | enabled={}",
                    row.get("CNTRY_ISO3_CODE"), row.get("CRNCY_ISO3_CODE"),
                    row.get("DELIVERY_OPTION_CODE"), row.get("IS_ENABLED"));
        }
        return rows;
    }

    /**
     * Get a random corridor for MoneyGram Cash Pickup (WILL_CALL).
     */
    @Step("Get MoneyGram Cash Pickup corridor")
    public Map<String, String> getMoneyGramCashPickup() throws Exception {
        List<Map<String, String>> corridors = getCorridorsForDelivery("MoneyGram", "WILL_CALL");
        if (corridors.isEmpty()) return null;
        return corridors.get(new java.util.Random().nextInt(corridors.size()));
    }

    /**
     * Get a random corridor for MoneyGram Bank Deposit (BANK_DEPOSIT).
     */
    @Step("Get MoneyGram Bank Deposit corridor")
    public Map<String, String> getMoneyGramBankDeposit() throws Exception {
        List<Map<String, String>> corridors = getCorridorsForDelivery("MoneyGram", "BANK_DEPOSIT");
        if (corridors.isEmpty()) return null;
        return corridors.get(new java.util.Random().nextInt(corridors.size()));
    }

    /**
     * Get a random corridor for MoneyGram Send to Wallet (DIRECT_TO_ACCT).
     */
    @Step("Get MoneyGram Send to Wallet corridor")
    public Map<String, String> getMoneyGramSendToWallet() throws Exception {
        List<Map<String, String>> corridors = getCorridorsForDelivery("MoneyGram", "DIRECT_TO_ACCT");
        if (corridors.isEmpty()) return null;
        return corridors.get(new java.util.Random().nextInt(corridors.size()));
    }

    // ══════════════════════════════════════════════════
    //  RIA corridor helpers
    // ══════════════════════════════════════════════════

    /**
     * Get a random corridor for RIA Cash Pickup (WILL_CALL).
     */
    @Step("Get RIA Cash Pickup corridor")
    public Map<String, String> getRIACashPickup() throws Exception {
        List<Map<String, String>> corridors = getCorridorsForDelivery("RIA", "WILL_CALL");
        if (corridors.isEmpty()) return null;
        return corridors.get(new java.util.Random().nextInt(corridors.size()));
    }

    /**
     * Get a random corridor for RIA Bank Deposit (BANK_DEPOSIT).
     */
    @Step("Get RIA Bank Deposit corridor")
    public Map<String, String> getRIABankDeposit() throws Exception {
        List<Map<String, String>> corridors = getCorridorsForDelivery("RIA", "BANK_DEPOSIT");
        if (corridors.isEmpty()) return null;
        return corridors.get(new java.util.Random().nextInt(corridors.size()));
    }

    /**
     * Get a random corridor for RIA Send to Wallet (DIRECT_TO_ACCT).
     */
    @Step("Get RIA Send to Wallet corridor")
    public Map<String, String> getRIASendToWallet() throws Exception {
        List<Map<String, String>> corridors = getCorridorsForDelivery("RIA", "DIRECT_TO_ACCT");
        if (corridors.isEmpty()) return null;
        return corridors.get(new java.util.Random().nextInt(corridors.size()));
    }

    /**
     * Full flow: trigger MQ refresh for RIA, then return its corridors.
     */
    @Step("Refresh RIA code table and return available corridors")
    public List<Map<String, String>> refreshAndGetRIACorridors() throws Exception {
        refreshRIA();
        return getCorridorsForProvider("RIA");
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
