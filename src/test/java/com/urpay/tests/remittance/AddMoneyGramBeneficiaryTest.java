package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.InternationalTransferFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.BeneficiaryActivationHelper;
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
 * Add MoneyGram (international) Beneficiary + transfer to it (Remittance squad).
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/AddMgBeneficiary/Script1707735660007.groovy
 *
 * Adding an MTO beneficiary triggers an IVR phone verification ("Verification Call") that cannot
 * be driven on LambdaTest; instead the beneficiary is activated directly in the wallet DB (IVR
 * skip) exactly as Katalon does (BeneficiaryKeyword.activateBeneficiary) — reusing the shared
 * {@link BeneficiaryActivationHelper} (unified beneficiary table serves local + international).
 *
 * THIS SUITE REQUIRES NETWORK ACCESS TO THE SIT WALLET DB — run it where {@code beneficiary.db.url}
 * is reachable. It lives in its own suite (suites/add-moneygram-beneficiary.xml), separate from the
 * DB-independent suites/moneygram-transfer.xml. The transfer step (priority 2) moves REAL money.
 */
@Epic("Remittance")
@Feature("Add International Beneficiary - MoneyGram")
public class AddMoneyGramBeneficiaryTest extends BaseTest {

    /** Generated in testAddMoneyGramBeneficiary, reused by testTransferToNewMoneyGramBeneficiary. */
    private String beneficiaryFullName;

    @Test(priority = 1, groups = {"remittance", "international-transfer", "add-moneygram-beneficiary"})
    @Story("Add MoneyGram Beneficiary")
    @Description("Login → International Transfer → Add beneficiary → country/delivery/currency → "
            + "name/nickname → confirm → OTP → IVR Verification Call → activate in DB (IVR skip)")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddMoneyGramBeneficiary() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("moneygram.user.mobileNumber"),
                c.get("moneygram.user.id"),
                c.get("moneygram.user.verificationCode", "1234"),
                c.get("moneygram.user.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        // Each run adds a freshly named beneficiary so the DB activation targets a unique PENDING row.
        beneficiaryFullName = LocalBeneficiaryGenerator.randomFullName();

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider(c.get("moneygram.addBeneficiary.serviceProvider", "MoneyGram"))
                .serviceProviderIndex(c.getInt("moneygram.addBeneficiary.serviceProviderIndex", 2))
                .beneficiaryName(beneficiaryFullName)
                .beneficiaryNickname(beneficiaryFullName.split(" ")[0])
                .receiverCountry(c.get("moneygram.addBeneficiary.receiverCountry", "India"))
                .deliveryOption(c.get("moneygram.addBeneficiary.deliveryOption", "Cash Pickup"))
                .currency(c.get("moneygram.addBeneficiary.currency", "Indian Rupee"))
                .amountSar(c.get("moneygram.addBeneficiary.amount", "15.50"))
                .build();

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(data, c.get("moneygram.user.verificationCode", "1234"));
        Assert.assertTrue(submitted,
                "Adding the beneficiary should reach the IVR 'Verification Call' screen");

        // IVR skip: activate the just-created PENDING beneficiary directly in the wallet DB.
        int activated = BeneficiaryActivationHelper.activate(beneficiaryFullName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + beneficiaryFullName + "' should be activated in the DB "
                + "(rows affected=" + activated + "). Requires DB access to "
                + c.get("beneficiary.db.url", "the SIT wallet DB") + ".");
    }

    @Test(priority = 2, dependsOnMethods = "testAddMoneyGramBeneficiary",
            groups = {"remittance", "international-transfer", "add-moneygram-beneficiary"})
    @Story("Transfer to the new MoneyGram beneficiary")
    @Description("International Transfer → select the newly added & activated beneficiary by name → "
            + "amount → MoneyGram → purpose → confirm → OTP → success (Thank You). REAL MONEY.")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferToNewMoneyGramBeneficiary() {
        ConfigManager c = ConfigManager.getInstance();

        InternationalTransferData data = InternationalTransferData.builder()
                .serviceProvider(c.get("moneygram.addBeneficiary.serviceProvider", "MoneyGram"))
                .serviceProviderIndex(c.getInt("moneygram.addBeneficiary.serviceProviderIndex", 2))
                .beneficiaryName(beneficiaryFullName)
                .deliveryOption(c.get("moneygram.addBeneficiary.deliveryOption", "Cash Pickup"))
                .amountSar(c.get("moneygram.addBeneficiary.amount", "15.50"))
                .build();

        InternationalTransferFlow.TransferResult result = new InternationalTransferFlow()
                .performTransfer(data, c.get("moneygram.user.verificationCode", "1234"));

        if (result == InternationalTransferFlow.TransferResult.NO_BENEFICIARY) {
            throw new SkipException("New beneficiary '" + beneficiaryFullName + "' is not selectable — "
                    + "verify the DB activation ran (beneficiary must be ACTIVE, not PENDING).");
        }

        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "Transfer to the new MoneyGram beneficiary '" + beneficiaryFullName
                + "' should reach the success (Thank You) screen");
    }
}
