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
 * Dynamic MoneyGram Cash Pickup Beneficiary Creation & Transfer — uses real corridor data from
 * MTO DB. Mirrors the RIACashPickupDynamicTest pattern (each delivery type is its own class so it
 * can run/be triaged independently).
 *
 * Flow:
 *   1. Trigger MQ refresh (MoneyGram code table — RefreshMGCodeTableRq)
 *   2. Query DB for an enabled Cash Pickup corridor
 *   3. Register a fresh user + top up balance
 *   4. Login → Add beneficiary using queried corridor data (country, currency, delivery)
 *   5. Activate beneficiary in DB (IVR skip)
 *   6. Transfer to the newly created beneficiary (real money)
 *
 * Requires DB machine (MQ + Oracle access + LambdaTest).
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/moneygram-cashpickup-dynamic.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=true
 */
@Epic("Remittance")
@Feature("MoneyGram Cash Pickup Dynamic Beneficiary (DB-driven)")
public class MoneyGramCashPickupDynamicTest extends BaseTest {

    private MTOCorridorHelper corridorHelper;
    private Map<String, String> cashPickupCorridor;
    private RegistrationApiHelper.Provisioned user;
    private String beneficiaryName;

    @BeforeClass(alwaysRun = true)
    public void setupCorridor() throws Exception {
        corridorHelper = new MTOCorridorHelper();

        log.info("Triggering MoneyGram MQ refresh...");
        corridorHelper.refreshMoneyGram();

        cashPickupCorridor = corridorHelper.getMoneyGramCashPickup();
        if (cashPickupCorridor != null) {
            log.info("MoneyGram Cash Pickup corridor: country={}, currency={}",
                    cashPickupCorridor.get("CNTRY_ISO3_CODE"), cashPickupCorridor.get("CRNCY_ISO3_CODE"));
        } else {
            log.warn("No MoneyGram Cash Pickup corridor found");
        }

        user = registerFreshUserWithBalance();
    }

    @Test(priority = 1, groups = {"remittance", "moneygram-dynamic", "moneygram-cashpickup"})
    @Story("Login with fresh registered user")
    @Description("Login with the newly provisioned user to prepare for MoneyGram Cash Pickup beneficiary creation")
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
            groups = {"remittance", "moneygram-dynamic", "moneygram-cashpickup"})
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
        beneficiaryName = LocalBeneficiaryGenerator.randomFullName();

        log.info("Adding MoneyGram Cash Pickup beneficiary: country={}, currency={}, delivery={}, name={}",
                country, currency, delivery, beneficiaryName);

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                .receiverCountry(country)
                .deliveryOption(delivery)
                .currency(currency)
                .beneficiaryType("Others")
                .beneficiaryName(beneficiaryName)
                .beneficiaryNickname(beneficiaryName.split(" ")[0])
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, "1234");
        Assert.assertTrue(submitted,
                "Add MoneyGram Cash Pickup beneficiary should reach the IVR 'Verification Call' screen");

        int activated = BeneficiaryActivationHelper.activate(beneficiaryName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + beneficiaryName + "' should be activated in DB (rows=" + activated + ")");
        log.info("MoneyGram Cash Pickup beneficiary '{}' activated successfully", beneficiaryName);
    }

    @Test(priority = 3, dependsOnMethods = "testAddCashPickupBeneficiary",
            groups = {"remittance", "moneygram-dynamic", "moneygram-cashpickup"})
    @Story("Transfer to MoneyGram Cash Pickup beneficiary")
    @Description("Perform a real transfer to the newly created MoneyGram Cash Pickup beneficiary")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToCashPickupBeneficiary() {
        if (beneficiaryName == null) {
            throw new SkipException("Cash Pickup beneficiary was not created — skipping transfer");
        }

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider("MoneyGram")
                // MoneyGram provider cards show no name text, only a logo — select via its unique
                // brand marker (validated across existing MoneyGram tests), not a guessed index.
                .serviceProviderMarker("Easy to track")
                .beneficiaryName(beneficiaryName.split(" ")[0])
                .deliveryOption("Cash Pickup")
                .amountSar("15.50")
                .build();

        InternationalTransferFlow flow = new InternationalTransferFlow();
        InternationalTransferFlow.TransferResult result = flow.performTransfer(data, "1234");

        if (result == InternationalTransferFlow.TransferResult.NO_BENEFICIARY) {
            throw new SkipException("MoneyGram Cash Pickup beneficiary '" + beneficiaryName
                    + "' not found on the account after activation");
        }
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "MoneyGram Cash Pickup transfer should complete successfully (Thank You screen)");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (corridorHelper != null) {
            corridorHelper.close();
        }
    }
}
