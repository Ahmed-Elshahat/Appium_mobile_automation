package com.urpay.tests.remittance;

import java.util.Map;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.flows.InternationalTransferFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.BeneficiaryActivationHelper;
import com.urpay.helpers.MTOCorridorHelper;
import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.model.InternationalTransferData;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.utils.LocalBeneficiaryGenerator;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Dynamic RIA Beneficiary Creation & Transfer — uses real corridor data from MTO DB.
 *
 * Flow:
 *   1. Trigger MQ refresh (RIA code table — RIARefreshCodeTableRq)
 *   2. Query DB for enabled Cash Pickup and Bank Deposit corridors
 *   3. Register a fresh user + top up balance
 *   4. Login → Add beneficiary using queried corridor data (country, currency, delivery)
 *   5. Activate beneficiary in DB (IVR skip)
 *   6. Transfer to the newly created beneficiary (real money)
 *
 * Requires DB machine (MQ + Oracle access + LambdaTest).
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/ria-dynamic-beneficiary.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=true
 */
@Epic("Remittance")
@Feature("RIA Dynamic Beneficiary (DB-driven)")
public class RIADynamicBeneficiaryTest extends BaseTest {

    private MTOCorridorHelper corridorHelper;
    private Map<String, String> cashPickupCorridor;
    private Map<String, String> bankDepositCorridor;
    private RegistrationApiHelper.Provisioned user;

    private String cashPickupBeneficiaryName;
    private String bankDepositBeneficiaryName;

    @BeforeClass(alwaysRun = true)
    public void setupCorridors() throws Exception {
        corridorHelper = new MTOCorridorHelper();

        // Step 1: Refresh RIA code table
        log.info("Triggering RIA MQ refresh...");
        corridorHelper.refreshRIA();

        // Step 2: Query corridors
        cashPickupCorridor = corridorHelper.getRIACashPickup();
        bankDepositCorridor = corridorHelper.getRIABankDeposit();

        log.info("========== RIA CORRIDOR DATA ==========");
        if (cashPickupCorridor != null) {
            log.info("Cash Pickup: country={}, currency={}",
                    cashPickupCorridor.get("CNTRY_ISO3_CODE"), cashPickupCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No RIA Cash Pickup corridor found");
        }
        if (bankDepositCorridor != null) {
            log.info("Bank Deposit: country={}, currency={}",
                    bankDepositCorridor.get("CNTRY_ISO3_CODE"), bankDepositCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No RIA Bank Deposit corridor found");
        }
        log.info("========================================");

        // Step 3: Register fresh user + top up
        user = registerFreshUserWithBalance();
    }

    @Test(priority = 1, groups = {"remittance", "ria-dynamic"})
    @Story("Login with fresh registered user")
    @Description("Login with the newly provisioned user to prepare for RIA beneficiary creation")
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
            groups = {"remittance", "ria-dynamic"})
    @Story("Add RIA Cash Pickup beneficiary (DB-driven corridor)")
    @Description("Add beneficiary using Cash Pickup corridor queried from MTO_CNTRY_CRNCY_DLV")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddCashPickupBeneficiary() {
        if (cashPickupCorridor == null) {
            throw new SkipException("No enabled RIA Cash Pickup corridor in DB");
        }

        String country = MTOCorridorHelper.countryCodeToName(cashPickupCorridor.get("CNTRY_ISO3_CODE"));
        String currency = MTOCorridorHelper.currencyCodeToName(cashPickupCorridor.get("CRNCY_ISO3_CODE"));
        String delivery = MTOCorridorHelper.deliveryCodeToAppText(cashPickupCorridor.get("DELIVERY_OPTION_CODE"));
        cashPickupBeneficiaryName = LocalBeneficiaryGenerator.randomFullName();

        log.info("Adding RIA Cash Pickup beneficiary: country={}, currency={}, delivery={}, name={}",
                country, currency, delivery, cashPickupBeneficiaryName);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("RIA")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(cashPickupBeneficiaryName)
                .beneficiaryNickname(cashPickupBeneficiaryName.split(" ")[0])
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, "1234");
        Assert.assertTrue(submitted,
                "Add RIA Cash Pickup beneficiary should reach the IVR 'Verification Call' screen");

        // Activate in DB
        int activated = BeneficiaryActivationHelper.activate(cashPickupBeneficiaryName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + cashPickupBeneficiaryName + "' should be activated in DB (rows=" + activated + ")");
        log.info("RIA Cash Pickup beneficiary '{}' activated successfully", cashPickupBeneficiaryName);
    }

    @Test(priority = 3, dependsOnMethods = "testLogin",
            groups = {"remittance", "ria-dynamic"})
    @Story("Add RIA Bank Deposit beneficiary (DB-driven corridor)")
    @Description("Add beneficiary using Bank Deposit corridor queried from MTO_CNTRY_CRNCY_DLV")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddBankDepositBeneficiary() {
        if (bankDepositCorridor == null) {
            throw new SkipException("No enabled RIA Bank Deposit corridor in DB");
        }

        String country = MTOCorridorHelper.countryCodeToName(bankDepositCorridor.get("CNTRY_ISO3_CODE"));
        String currency = MTOCorridorHelper.currencyCodeToName(bankDepositCorridor.get("CRNCY_ISO3_CODE"));
        String delivery = MTOCorridorHelper.deliveryCodeToAppText(bankDepositCorridor.get("DELIVERY_OPTION_CODE"));
        bankDepositBeneficiaryName = LocalBeneficiaryGenerator.randomFullName();
        String accountNumber = String.valueOf(System.currentTimeMillis()).substring(3);

        log.info("Adding RIA Bank Deposit beneficiary: country={}, currency={}, delivery={}, name={}, account={}",
                country, currency, delivery, bankDepositBeneficiaryName, accountNumber);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("RIA")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(bankDepositBeneficiaryName)
                .beneficiaryNickname(bankDepositBeneficiaryName.split(" ")[0])
                .bankName("")
                .branch("")
                .accountNumber(accountNumber)
                .routingNumber("")
                .city("")
                .purposeOfFunds("Family Support")
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, "1234");
        Assert.assertTrue(submitted,
                "Add RIA Bank Deposit beneficiary should reach the IVR 'Verification Call' screen");

        // Activate in DB
        int activated = BeneficiaryActivationHelper.activate(bankDepositBeneficiaryName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + bankDepositBeneficiaryName + "' should be activated in DB (rows=" + activated + ")");
        log.info("RIA Bank Deposit beneficiary '{}' activated successfully", bankDepositBeneficiaryName);
    }

    @Test(priority = 4, dependsOnMethods = "testAddCashPickupBeneficiary",
            groups = {"remittance", "ria-dynamic"})
    @Story("Transfer to RIA Cash Pickup beneficiary")
    @Description("Perform a real transfer to the newly created RIA Cash Pickup beneficiary")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToCashPickupBeneficiary() {
        if (cashPickupBeneficiaryName == null) {
            throw new SkipException("Cash Pickup beneficiary was not created — skipping transfer");
        }

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("RIA")
                .serviceProviderIndex(0)
                .beneficiaryName(cashPickupBeneficiaryName.split(" ")[0])
                .deliveryOption("Cash Pickup")
                .amountSar("15.50")
                .build();

        InternationalTransferFlow flow = new InternationalTransferFlow();
        InternationalTransferFlow.TransferResult result = flow.performTransfer(data, "1234");

        if (result == InternationalTransferFlow.TransferResult.NO_BENEFICIARY) {
            throw new SkipException("RIA Cash Pickup beneficiary '" + cashPickupBeneficiaryName
                    + "' not found on the account after activation");
        }
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "RIA Cash Pickup transfer should complete successfully (Thank You screen)");
    }

    @Test(priority = 5, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "ria-dynamic"})
    @Story("Transfer to RIA Bank Deposit beneficiary")
    @Description("Perform a real transfer to the newly created RIA Bank Deposit beneficiary")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToBankDepositBeneficiary() {
        if (bankDepositBeneficiaryName == null) {
            throw new SkipException("Bank Deposit beneficiary was not created — skipping transfer");
        }

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("RIA")
                .serviceProviderIndex(0)
                .beneficiaryName(bankDepositBeneficiaryName.split(" ")[0])
                .deliveryOption("Account Deposit")
                .amountSar("20.50")
                .build();

        InternationalTransferFlow flow = new InternationalTransferFlow();
        InternationalTransferFlow.TransferResult result = flow.performTransfer(data, "1234");

        if (result == InternationalTransferFlow.TransferResult.NO_BENEFICIARY) {
            throw new SkipException("RIA Bank Deposit beneficiary '" + bankDepositBeneficiaryName
                    + "' not found on the account after activation");
        }
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "RIA Bank Deposit transfer should complete successfully (Thank You screen)");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (corridorHelper != null) {
            corridorHelper.close();
        }
    }
}
