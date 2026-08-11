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
 * Dynamic MoneyGram Bank Deposit Beneficiary Creation & Transfer — uses real corridor data from
 * MTO DB. Mirrors the RIABankDepositDynamicTest pattern.
 *
 * Flow:
 *   1. Trigger MQ refresh (MoneyGram code table — RefreshMGCodeTableRq)
 *   2. Query DB for an enabled Bank Deposit corridor
 *   3. Register a fresh user + top up balance
 *   4. Login → Add beneficiary using queried corridor data (country, currency, delivery)
 *   5. Activate beneficiary in DB (IVR skip)
 *   6. Transfer to the newly created beneficiary (real money)
 *
 * Requires DB machine (MQ + Oracle access + LambdaTest).
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/moneygram-bankdeposit-dynamic.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=true
 */
@Epic("Remittance")
@Feature("MoneyGram Bank Deposit Dynamic Beneficiary (DB-driven)")
public class MoneyGramBankDepositDynamicTest extends BaseTest {

    private MTOCorridorHelper corridorHelper;
    private Map<String, String> bankDepositCorridor;
    private RegistrationApiHelper.Provisioned user;
    private String beneficiaryName;

    @BeforeClass(alwaysRun = true)
    public void setupCorridor() throws Exception {
        corridorHelper = new MTOCorridorHelper();

        log.info("Triggering MoneyGram MQ refresh...");
        corridorHelper.refreshMoneyGram();

        bankDepositCorridor = corridorHelper.getMoneyGramBankDeposit();
        if (bankDepositCorridor != null) {
            log.info("MoneyGram Bank Deposit corridor: country={}, currency={}",
                    bankDepositCorridor.get("CNTRY_ISO3_CODE"), bankDepositCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No MoneyGram Bank Deposit corridor found");
        }

        user = registerFreshUserWithBalance();
    }

    @Test(priority = 1, groups = {"remittance", "moneygram-dynamic", "moneygram-bankdeposit"})
    @Story("Login with fresh registered user")
    @Description("Login with the newly provisioned user to prepare for MoneyGram Bank Deposit beneficiary creation")
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
            groups = {"remittance", "moneygram-dynamic", "moneygram-bankdeposit"})
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
        beneficiaryName = LocalBeneficiaryGenerator.randomFullName();
        String accountNumber = String.valueOf(System.currentTimeMillis()).substring(3);

        log.info("Adding MoneyGram Bank Deposit beneficiary: country={}, currency={}, delivery={}, name={}, account={}",
                country, currency, delivery, beneficiaryName, accountNumber);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(beneficiaryName)
                .beneficiaryNickname(beneficiaryName.split(" ")[0])
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
                "Add MoneyGram Bank Deposit beneficiary should reach the IVR 'Verification Call' screen");

        int activated = BeneficiaryActivationHelper.activate(beneficiaryName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + beneficiaryName + "' should be activated in DB (rows=" + activated + ")");
        log.info("MoneyGram Bank Deposit beneficiary '{}' activated successfully", beneficiaryName);
    }

    @Test(priority = 3, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "moneygram-dynamic", "moneygram-bankdeposit"})
    @Story("Transfer to MoneyGram Bank Deposit beneficiary")
    @Description("Perform a real transfer to the newly created MoneyGram Bank Deposit beneficiary")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToBankDepositBeneficiary() {
        if (beneficiaryName == null) {
            throw new SkipException("Bank Deposit beneficiary was not created — skipping transfer");
        }

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                .serviceProviderMarker("Easy to track")
                .beneficiaryName(beneficiaryName.split(" ")[0])
                // Confirmation screen shows this MTO's Bank Deposit as "Account Deposit" branding
                // (differs from the generic "Bank Deposit" add-wizard option text).
                .deliveryOption("Account Deposit")
                .amountSar("20.50")
                .build();

        InternationalTransferFlow flow = new InternationalTransferFlow();
        InternationalTransferFlow.TransferResult result = flow.performTransfer(data, "1234");

        if (result == InternationalTransferFlow.TransferResult.NO_BENEFICIARY) {
            throw new SkipException("MoneyGram Bank Deposit beneficiary '" + beneficiaryName
                    + "' not found on the account after activation");
        }
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "MoneyGram Bank Deposit transfer should complete successfully (Thank You screen)");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (corridorHelper != null) {
            corridorHelper.close();
        }
    }
}
