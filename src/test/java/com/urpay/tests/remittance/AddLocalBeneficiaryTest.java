package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LocalTransferFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.BeneficiaryActivationHelper;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.utils.LocalBeneficiaryGenerator;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Add Local Beneficiary + transfer to it (Remittance squad).
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/LocalTran/AddLocalBeneficiary
 *   Scripts/Remittance/LocalTran/LocalTransferNewTransfer
 *
 * Adding a new local beneficiary triggers an IVR phone verification that cannot be driven on
 * LambdaTest; instead the beneficiary is activated directly in the wallet DB (IVR skip) exactly
 * as Katalon does (BeneficiaryKeyword.activateBeneficiary).
 *
 * THIS SUITE REQUIRES NETWORK ACCESS TO THE SIT WALLET DB — run it in an environment that can
 * reach {@code beneficiary.db.url}. It lives in its own suite (suites/add-local-beneficiary.xml),
 * separate from the DB-independent suites/local-transfer.xml.
 */
@Epic("Remittance")
@Feature("Add Local Beneficiary")
public class AddLocalBeneficiaryTest extends BaseTest {

    /** Generated in testAddLocalBeneficiary, reused by testLocalTransferToNewBeneficiary. */
    private String beneficiaryFullName;

    @Test(priority = 1, groups = {"remittance", "add-local-beneficiary"})
    @Story("Add Local Beneficiary")
    @Description("Login → (cleanup) → Local Transfer → Add new beneficiary (IBAN/name/nickname) → "
            + "Next → Confirm → OTP → activate in DB (IVR skip)")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddLocalBeneficiary() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("urpayUser.mobileNumber"),
                c.get("urpayUser.id"),
                c.get("urpayUser.verificationCode", "1234"),
                c.get("urpayUser.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        LocalTransferFlow flow = new LocalTransferFlow();

        // Remove a leftover beneficiary with the same nickname (best-effort, like Katalon).
        flow.deleteExistingLocalBeneficiary(c.get("addBeneficiary.nickname", "zaza"));

        // Each run adds a freshly named beneficiary so the DB activation targets a unique PENDING row.
        beneficiaryFullName = LocalBeneficiaryGenerator.randomFullName();

        flow.addLocalBeneficiary(
                c.get("addBeneficiary.amount", "60"),
                c.get("addBeneficiary.iban"),
                beneficiaryFullName,
                c.get("addBeneficiary.nickname", "zaza"),
                c.get("urpayUser.verificationCode", "1234"));

        // IVR skip: activate the just-created PENDING beneficiary directly in the wallet DB.
        int activated = BeneficiaryActivationHelper.activate(beneficiaryFullName);
        Assert.assertTrue(activated > 0,
                "Beneficiary '" + beneficiaryFullName + "' should be activated in the DB "
                + "(rows affected=" + activated + "). Requires DB access to "
                + c.get("beneficiary.db.url", "the SIT wallet DB") + ".");
    }

    @Test(priority = 2, dependsOnMethods = "testAddLocalBeneficiary",
            groups = {"remittance", "add-local-beneficiary"})
    @Story("Transfer to the new local beneficiary")
    @Description("Local Transfer → amount → select the newly added & activated beneficiary by name → "
            + "purpose → confirm → OTP → success")
    @Severity(SeverityLevel.CRITICAL)
    public void testLocalTransferToNewBeneficiary() {
        ConfigManager c = ConfigManager.getInstance();

        boolean ok = new LocalTransferFlow().transferToNewBeneficiary(
                c.get("addBeneficiary.amount", "60"),
                beneficiaryFullName,
                c.get("urpayUser.verificationCode", "1234"));

        Assert.assertTrue(ok,
                "Transfer to the new beneficiary '" + beneficiaryFullName + "' should reach the "
                + "success screen (the beneficiary must be ACTIVE — verify the DB activation ran).");
    }
}
