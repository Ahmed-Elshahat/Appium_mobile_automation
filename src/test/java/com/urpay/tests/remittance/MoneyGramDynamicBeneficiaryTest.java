package com.urpay.tests.remittance;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.InternationalTransferFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.BeneficiaryActivationHelper;
import com.urpay.helpers.MTOCorridorHelper;
import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.helpers.WalletBalanceHelper;
import com.urpay.model.InternationalTransferData;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.utils.LocalBeneficiaryGenerator;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

import java.util.Map;

/**
 * Dynamic MoneyGram Beneficiary Creation — uses real corridor data from MTO DB.
 *
 * Flow:
 *   1. Trigger MQ refresh (MoneyGram code table)
 *   2. Query DB for enabled Cash Pickup and Bank Deposit corridors
 *   3. Register a fresh user + top up balance
 *   4. Login → Add beneficiary using queried corridor data (country, currency, delivery)
 *   5. Activate beneficiary in DB (IVR skip)
 *
 * Requires DB machine (MQ + Oracle access + LambdaTest).
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/moneygram-dynamic-beneficiary.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=true
 */
@Epic("Remittance")
@Feature("MoneyGram Dynamic Beneficiary (DB-driven)")
public class MoneyGramDynamicBeneficiaryTest extends BaseTest {

    private MTOCorridorHelper corridorHelper;
    private Map<String, String> cashPickupCorridor;
    private Map<String, String> bankDepositCorridor;
    private RegistrationApiHelper.Provisioned user;

    @BeforeClass(alwaysRun = true)
    public void setupCorridors() throws Exception {
        corridorHelper = new MTOCorridorHelper();

        // Step 1: Refresh MoneyGram code table
        log.info("Triggering MoneyGram MQ refresh...");
        corridorHelper.refreshMoneyGram();

        // Step 2: Query corridors
        cashPickupCorridor = corridorHelper.getMoneyGramCashPickup();
        bankDepositCorridor = corridorHelper.getMoneyGramBankDeposit();

        log.info("========== CORRIDOR DATA ==========");
        if (cashPickupCorridor != null) {
            log.info("Cash Pickup: country={}, currency={}",
                    cashPickupCorridor.get("CNTRY_ISO3_CODE"), cashPickupCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No Cash Pickup corridor found");
        }
        if (bankDepositCorridor != null) {
            log.info("Bank Deposit: country={}, currency={}",
                    bankDepositCorridor.get("CNTRY_ISO3_CODE"), bankDepositCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No Bank Deposit corridor found");
        }
        log.info("====================================");

        // Step 3: Register fresh user + top up
        user = registerFreshUserWithBalance();
    }

    @Test(priority = 1, groups = {"remittance", "moneygram-dynamic"})
    @Story("Login with fresh registered user")
    @Description("Login with the newly provisioned user to prepare for beneficiary creation")
    @Severity(SeverityLevel.BLOCKER)
    public void testLogin() {
        String mobile = user.mobile;
        if (mobile.startsWith("+966")) {
            mobile = "0" + mobile.substring(4);
        }
        DashboardPage dashboard = new LoginFlow().loginWith(
                mobile, user.poi, "1234", user.passcode);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
    }

    @Test(priority = 2, dependsOnMethods = "testLogin",
            groups = {"remittance", "moneygram-dynamic"})
    @Story("Add MoneyGram Cash Pickup beneficiary (DB-driven corridor)")
    @Description("Add beneficiary using Cash Pickup corridor queried from MTO_CNTRY_CRNCY_DLV")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddCashPickupBeneficiary() {
        if (cashPickupCorridor == null) {
            throw new SkipException("No enabled MoneyGram Cash Pickup corridor in DB");
        }

        String country = MTOCorridorHelper.countryCodeToName(cashPickupCorridor.get("CNTRY_ISO3_CODE"));
        String currency = MTOCorridorHelper.currencyCodeToName(cashPickupCorridor.get("CRNCY_ISO3_CODE"));
        String delivery = MTOCorridorHelper.deliveryCodeToAppText(cashPickupCorridor.get("DELIVERY_OPTION_CODE"));
        String fullName = LocalBeneficiaryGenerator.randomFullName();

        log.info("Adding Cash Pickup beneficiary: country={}, currency={}, delivery={}, name={}",
                country, currency, delivery, fullName);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(fullName)
                .beneficiaryNickname(fullName.split(" ")[0])
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, "1234");
        Assert.assertTrue(submitted,
                "Add Cash Pickup beneficiary should reach the IVR 'Verification Call' screen");

        // Activate in DB
        int activated = BeneficiaryActivationHelper.activate(fullName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + fullName + "' should be activated in DB (rows=" + activated + ")");
        log.info("Cash Pickup beneficiary '{}' activated successfully", fullName);
    }

    @Test(priority = 3, dependsOnMethods = "testLogin",
            groups = {"remittance", "moneygram-dynamic"})
    @Story("Add MoneyGram Bank Deposit beneficiary (DB-driven corridor)")
    @Description("Add beneficiary using Bank Deposit corridor queried from MTO_CNTRY_CRNCY_DLV")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddBankDepositBeneficiary() {
        if (bankDepositCorridor == null) {
            throw new SkipException("No enabled MoneyGram Bank Deposit corridor in DB");
        }

        String country = MTOCorridorHelper.countryCodeToName(bankDepositCorridor.get("CNTRY_ISO3_CODE"));
        String currency = MTOCorridorHelper.currencyCodeToName(bankDepositCorridor.get("CRNCY_ISO3_CODE"));
        String delivery = MTOCorridorHelper.deliveryCodeToAppText(bankDepositCorridor.get("DELIVERY_OPTION_CODE"));
        String fullName = LocalBeneficiaryGenerator.randomFullName();
        // Generate unique account number (10 digits from timestamp)
        String accountNumber = String.valueOf(System.currentTimeMillis()).substring(3);

        log.info("Adding Bank Deposit beneficiary: country={}, currency={}, delivery={}, name={}, account={}",
                country, currency, delivery, fullName, accountNumber);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(fullName)
                .beneficiaryNickname(fullName.split(" ")[0])
                .bankName("")          // First bank in list selected by flow
                .branch("")            // First branch selected by flow
                .accountNumber(accountNumber)
                .routingNumber("")
                .city("")
                .purposeOfFunds("")
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, "1234");
        Assert.assertTrue(submitted,
                "Add Bank Deposit beneficiary should reach the IVR 'Verification Call' screen");

        // Activate in DB
        int activated = BeneficiaryActivationHelper.activate(fullName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + fullName + "' should be activated in DB (rows=" + activated + ")");
        log.info("Bank Deposit beneficiary '{}' activated successfully", fullName);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (corridorHelper != null) {
            corridorHelper.close();
        }
    }
}
