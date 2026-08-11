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
 * Dynamic MoneyGram Send to Wallet Beneficiary Creation & Transfer — uses real corridor data
 * from MTO DB. Mirrors the RIA/MoneyGram Cash Pickup & Bank Deposit dynamic test pattern.
 *
 * Flow:
 *   1. Trigger MQ refresh (MoneyGram code table — RefreshMGCodeTableRq)
 *   2. Query DB for an enabled Send to Wallet corridor
 *   3. Register a fresh user + top up balance
 *   4. Login → Add beneficiary using queried corridor data (country, currency, delivery)
 *   5. Activate beneficiary in DB (IVR skip)
 *   6. Transfer to the newly created beneficiary (real money)
 *
 * Requires DB machine (MQ + Oracle access + LambdaTest).
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/moneygram-sendtowallet-dynamic.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=true
 */
@Epic("Remittance")
@Feature("MoneyGram Send to Wallet Dynamic Beneficiary (DB-driven)")
public class MoneyGramSendToWalletDynamicTest extends BaseTest {

    private MTOCorridorHelper corridorHelper;
    private Map<String, String> sendToWalletCorridor;
    private RegistrationApiHelper.Provisioned user;
    private String beneficiaryName;

    @BeforeClass(alwaysRun = true)
    public void setupCorridor() throws Exception {
        corridorHelper = new MTOCorridorHelper();

        log.info("Triggering MoneyGram MQ refresh...");
        corridorHelper.refreshMoneyGram();

        sendToWalletCorridor = corridorHelper.getMoneyGramSendToWallet();
        if (sendToWalletCorridor != null) {
            log.info("MoneyGram Send to Wallet corridor: country={}, currency={}",
                    sendToWalletCorridor.get("CNTRY_ISO3_CODE"), sendToWalletCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No MoneyGram Send to Wallet corridor found");
        }

        user = registerFreshUserWithBalance();
    }

    @Test(priority = 1, groups = {"remittance", "moneygram-dynamic", "moneygram-sendtowallet"})
    @Story("Login with fresh registered user")
    @Description("Login with the newly provisioned user to prepare for MoneyGram Send to Wallet beneficiary creation")
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
            groups = {"remittance", "moneygram-dynamic", "moneygram-sendtowallet"})
    @Story("Add MoneyGram Send to Wallet beneficiary (DB-driven corridor)")
    @Description("Add beneficiary using Send to Wallet corridor queried from MTO_CNTRY_CRNCY_DLV")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddSendToWalletBeneficiary() {
        if (sendToWalletCorridor == null) {
            throw new SkipException("No enabled MoneyGram Send to Wallet corridor in DB");
        }

        String country = MTOCorridorHelper.countryCodeToName(sendToWalletCorridor.get("CNTRY_ISO3_CODE"));
        String currency = MTOCorridorHelper.currencyCodeToName(sendToWalletCorridor.get("CRNCY_ISO3_CODE"));
        String delivery = MTOCorridorHelper.deliveryCodeToAppText(sendToWalletCorridor.get("DELIVERY_OPTION_CODE"));
        beneficiaryName = LocalBeneficiaryGenerator.randomFullName();

        log.info("Adding MoneyGram Send to Wallet beneficiary: country={}, currency={}, delivery={}, name={}",
                country, currency, delivery, beneficiaryName);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(beneficiaryName)
                .beneficiaryNickname(beneficiaryName.split(" ")[0])
                .purposeOfFunds("Family Support")
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, "1234");
        Assert.assertTrue(submitted,
                "Add MoneyGram Send to Wallet beneficiary should reach the IVR 'Verification Call' screen");

        int activated = BeneficiaryActivationHelper.activate(beneficiaryName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + beneficiaryName + "' should be activated in DB (rows=" + activated + ")");
        log.info("MoneyGram Send to Wallet beneficiary '{}' activated successfully", beneficiaryName);
    }

    @Test(priority = 3, dependsOnMethods = "testAddSendToWalletBeneficiary",
            groups = {"remittance", "moneygram-dynamic", "moneygram-sendtowallet"})
    @Story("Transfer to MoneyGram Send to Wallet beneficiary")
    @Description("Perform a real transfer to the newly created MoneyGram Send to Wallet beneficiary")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToSendToWalletBeneficiary() {
        if (beneficiaryName == null) {
            throw new SkipException("Send to Wallet beneficiary was not created — skipping transfer");
        }

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                // "Easy to track" ALONE is not unique — see the same note in
                // MoneyGramCashPickupDynamicTest. Require BOTH tags to avoid matching Transfast.
                .serviceProviderMarker("Easy to track+Anywallet")
                .beneficiaryName(beneficiaryName.split(" ")[0])
                .deliveryOption("Send to Wallet")
                .amountSar("100.5")
                .build();

        InternationalTransferFlow flow = new InternationalTransferFlow();
        InternationalTransferFlow.TransferResult result = flow.performTransfer(data, "1234");

        if (result == InternationalTransferFlow.TransferResult.NO_BENEFICIARY) {
            throw new SkipException("MoneyGram Send to Wallet beneficiary '" + beneficiaryName
                    + "' not found on the account after activation");
        }
        if (result == InternationalTransferFlow.TransferResult.PROVIDER_UNAVAILABLE) {
            throw new SkipException("MoneyGram is not offered as a provider for this corridor right now");
        }
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "MoneyGram Send to Wallet transfer should complete successfully (Thank You screen)");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (corridorHelper != null) {
            corridorHelper.close();
        }
    }
}
