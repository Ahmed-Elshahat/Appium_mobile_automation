package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.InternationalTransferFlow;
import com.urpay.flows.LoginFlow;
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
 * Add International (MoneyGram) Beneficiary — corridor & scenario coverage (Remittance squad).
 *
 * Migrated from Katalon Scripts/Remittance/InternationalTran/Add international Beneficiary/:
 *   - Add Bank Deposit / Cash Pickup / Send-to-Wallet beneficiary (PKR / INR / EGP corridors)
 *   - Add with "Myself" beneficiary type
 *   - Edit an added beneficiary (ToValidateEditAddedInternationalBeneficiary)
 *   - Remove an added beneficiary (ToValidateRemoveAddedInternationalBeneficiary)
 *
 * Adding an MTO beneficiary ends on an IVR "Verification Call" — the beneficiary is created
 * PENDING and must be activated in the wallet DB (IVR skip) before it can receive a transfer.
 * These cases assert the add UI flow reaches the Verification Call screen; the DB activation +
 * transfer-to-new is validated in a DB-connected environment (AddMoneyGramBeneficiaryTest).
 *
 * Single LambdaTest session: priority-1 logs in; the rest reuse it (dependsOnMethods). Each add
 * uses a freshly generated name so runs don't collide. Remove runs last (it deletes a beneficiary).
 */
@Epic("Remittance")
@Feature("Add International Beneficiary - MoneyGram")
public class AddInternationalBeneficiaryTest extends BaseTest {

    private static final String OTP_KEY = "moneygram.user.verificationCode";

    private DashboardPage login() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("moneygram.user.mobileNumber"),
                c.get("moneygram.user.id"),
                c.get(OTP_KEY, "1234"),
                c.get("moneygram.user.passCode", "2233"));
    }

    /** Build add-beneficiary data for a corridor prefix, with a freshly generated unique name. */
    private InternationalTransferData addData(String prefix, String beneficiaryType) {
        ConfigManager c = ConfigManager.getInstance();
        String fullName = LocalBeneficiaryGenerator.randomFullName();
        // Account / wallet number MUST be unique per run — the app rejects a duplicate ("This IBAN
        // is already in use for another beneficiary"). Derive a unique numeric of the same length
        // as the configured template from the current timestamp.
        String acctTemplate = c.get(prefix + ".accountNumber", "");
        String accountNumber = acctTemplate.isEmpty() ? "" : uniqueDigits(acctTemplate.length());
        return InternationalTransferData.builder()
                .serviceProvider(c.get(prefix + ".serviceProvider", "MoneyGram"))
                .receiverCountry(c.get(prefix + ".receiverCountry"))
                .deliveryOption(c.get(prefix + ".deliveryOption"))
                .currency(c.get(prefix + ".currency", ""))
                .beneficiaryType(beneficiaryType)
                .beneficiaryName(fullName)
                .beneficiaryNickname(fullName.split(" ")[0])
                .bankName(c.get(prefix + ".bankName", ""))
                .branch(c.get(prefix + ".branch", ""))
                .accountNumber(accountNumber)
                .routingNumber(c.get(prefix + ".routingNumber", ""))
                .city(c.get(prefix + ".city", ""))
                .purposeOfFunds(c.get(prefix + ".purposeOfFunds", ""))
                .build();
    }

    /** A unique numeric string of the given length (timestamp-derived) — avoids duplicate-IBAN/
     *  duplicate-wallet rejection when adding a beneficiary. */
    private static String uniqueDigits(int length) {
        String base = String.valueOf(System.currentTimeMillis()) + "000";
        return base.length() >= length ? base.substring(base.length() - length) : base;
    }

    @Test(priority = 1, groups = {"remittance", "international-transfer", "add-international-beneficiary"})
    @Story("Add MoneyGram Bank Deposit beneficiary (Pakistan)")
    @Description("Login → Add beneficiary → Pakistan → Bank Deposit → currency → name → bank/branch/"
            + "account → relationship → confirm → OTP → IVR Verification Call. (DB-activate in DB env.)")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddBankDepositBeneficiary() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(addData("moneygram.addBankDeposit", "Others"),
                        ConfigManager.getInstance().get(OTP_KEY, "1234"));

        Assert.assertTrue(submitted,
                "Add Bank Deposit beneficiary should reach the IVR 'Verification Call' screen");
    }

    @Test(priority = 2, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "international-transfer", "add-international-beneficiary"})
    @Story("Add MoneyGram Cash Pickup beneficiary (India)")
    @Description("Add beneficiary → India → Cash Pickup → currency → name → relationship → confirm → "
            + "OTP → IVR Verification Call.")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddCashPickupBeneficiary() {
        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(addData("moneygram.addCashPickup", "Others"),
                        ConfigManager.getInstance().get(OTP_KEY, "1234"));
        Assert.assertTrue(submitted,
                "Add Cash Pickup beneficiary should reach the IVR 'Verification Call' screen");
    }

    @Test(priority = 3, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "international-transfer", "add-international-beneficiary"})
    @Story("Add MoneyGram Send-to-Wallet beneficiary (Egypt)")
    @Description("Add beneficiary → Egypt → Send to Wallet → currency → name → relationship → "
            + "confirm → OTP → IVR Verification Call.")
    @Severity(SeverityLevel.NORMAL)
    public void testAddSendToWalletBeneficiary() {
        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(addData("moneygram.addSendToWallet", "Others"),
                        ConfigManager.getInstance().get(OTP_KEY, "1234"));
        Assert.assertTrue(submitted,
                "Add Send-to-Wallet beneficiary should reach the IVR 'Verification Call' screen");
    }

    @Test(priority = 4, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "international-transfer", "add-international-beneficiary"})
    @Story("Add beneficiary with 'Myself' type")
    @Description("Add beneficiary → select 'Myself' type → corridor → currency → name → relationship "
            + "→ confirm → OTP → IVR Verification Call.")
    @Severity(SeverityLevel.NORMAL)
    public void testAddMyselfBeneficiary() {
        boolean submitted = new InternationalTransferFlow()
                .addBeneficiary(addData("moneygram.addCashPickup", "Myself"),
                        ConfigManager.getInstance().get(OTP_KEY, "1234"));
        Assert.assertTrue(submitted,
                "Add 'Myself' beneficiary should reach the IVR 'Verification Call' screen");
    }

    @Test(priority = 5, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "international-transfer", "add-international-beneficiary"})
    @Story("Edit an added beneficiary")
    @Description("Open the first saved beneficiary → edit name(s) → Save.")
    @Severity(SeverityLevel.NORMAL)
    public void testEditBeneficiary() {
        InternationalTransferData edit = InternationalTransferData.builder()
                .firstName("Edited")
                .beneficiaryNickname("Edited" + (System.currentTimeMillis() % 100000))
                .build();
        boolean edited = new InternationalTransferFlow().editBeneficiary(edit);
        Assert.assertTrue(edited, "Editing the first beneficiary should reach & save the edit screen");
    }

    @Test(priority = 6, dependsOnMethods = "testAddBankDepositBeneficiary",
            groups = {"remittance", "international-transfer", "add-international-beneficiary"})
    @Story("Remove an added beneficiary")
    @Description("Open the first saved beneficiary → Delete → confirm.")
    @Severity(SeverityLevel.NORMAL)
    public void testRemoveBeneficiary() {
        boolean removed = new InternationalTransferFlow().removeBeneficiary();
        Assert.assertTrue(removed, "Removing the first beneficiary should confirm the deletion");
    }
}
