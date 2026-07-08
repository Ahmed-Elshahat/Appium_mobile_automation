package com.urpay.tests.remittance;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.InternationalTransferFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.model.InternationalTransferData;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.remittance.InternationalTransferPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Tahweel AlRajhi (H2H) International Transfer (Remittance squad) — Account Deposit (Bank Deposit).
 *
 * Reuses the operator-agnostic plumbing (InternationalTransferPage / InternationalTransferFlow /
 * InternationalTransferData). This is the H2H "Bank Deposit" corridor — its delivery option is
 * branded "Account Deposit" in the app, and Tahweel is provider card 2 on this route (Cash Pickup
 * is card 1).
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/Perform International Transfer/Test Data Setup .../
 *     Setup H2H Bank Deposit for Indian Beneficiary  (serviceProvider "Tahweel Alrajhi",
 *     serviceProviderIndex 2, deliveryOption "Account Deposit")
 *
 * Separate account from the Cash Pickup case (own login), so this is its own test class rather
 * than a second method — two different accounts cannot share one session.
 *
 * Strategy: the login account (tahweel.bankDeposit.user.* = 0520926735, poi 1308451788 NAT) is
 * expected to have an active Tahweel AlRajhi Account-Deposit beneficiary; the case searches +
 * selects it. If the account has no such beneficiary the case SKIPS cleanly, and the flow logs the
 * account's beneficiaries so the real name can be pinned in config.
 *
 * ⚠ REAL MONEY: the case moves real funds (small amount from config). It drives the flow ONCE to
 * the confirmation screen, validates the confirmation details there (no money yet), and only then
 * confirms — so the beneficiary→confirm steps are NOT executed twice.
 */
@Epic("Remittance")
@Feature("International Transfer - Tahweel AlRajhi (H2H)")
public class TahweelBankDepositTest extends BaseTest {

    private static final String OTP_KEY = "tahweel.bankDeposit.user.verificationCode";
    private static final String PREFIX = "tahweel.bankDeposit";

    @Test(priority = 1, groups = {"remittance", "international-transfer", "tahweel", "h2h",
            "tahweel-bankdeposit", "smoke"})
    @Story("Tahweel AlRajhi (H2H) Account Deposit transfer")
    @Description("Login → International Transfer → search + select the Tahweel AlRajhi Account "
            + "Deposit beneficiary → amount → select Tahweel AlRajhi (card 2) → payout bank → "
            + "purpose → CONFIRMATION page: verify it renders & the delivery option matches, THEN "
            + "confirm → OTP → success (Thank You). The confirmation details are validated on the "
            + "same screen just before confirming, so the flow runs ONCE. REAL MONEY.")
    @Severity(SeverityLevel.CRITICAL)
    public void testTahweelBankDepositTransfer() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("tahweel.bankDeposit.user.mobileNumber"),
                c.get("tahweel.bankDeposit.user.id"),
                c.get(OTP_KEY, "1234"),
                c.get("tahweel.bankDeposit.user.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        InternationalTransferData data = InternationalTransferData.fromConfig(c, PREFIX);
        InternationalTransferFlow flow = new InternationalTransferFlow();

        // 1) Drive to the confirmation screen (no money spent yet).
        InternationalTransferPage page = flow.openConfirmation(data);
        if (page == null) {
            throw new SkipException("No Tahweel AlRajhi " + data.getDeliveryOption() + " beneficiary '"
                    + data.getBeneficiaryName() + "' on account "
                    + c.get("tahweel.bankDeposit.user.mobileNumber")
                    + " — check the logged 'beneficiaries on the account' and set "
                    + PREFIX + ".beneficiaryName.");
        }

        // 2) Validate the confirmation details BEFORE confirming (no money yet).
        Assert.assertTrue(page.isConfirmationVisible(15),
                "The Tahweel AlRajhi confirmation screen should render before confirming");
        String delivery = page.getConfirmationValue("Delivery Option");
        String expectedKeyword = lastWord(data.getDeliveryOption()).toLowerCase();
        Assert.assertTrue(delivery.toLowerCase().contains(expectedKeyword),
                "Confirmation 'Delivery Option' should reflect " + data.getDeliveryOption()
                + " (was: '" + delivery + "')");

        // 3) Confirm the SAME transfer → OTP → success (REAL MONEY). No re-running the flow.
        InternationalTransferFlow.TransferResult result =
                flow.confirmAndFinish(page, c.get(OTP_KEY, "1234"));
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "Success (Thank You) screen should be visible after the Tahweel AlRajhi "
                + data.getDeliveryOption() + " transfer to " + data.getBeneficiaryName());
    }

    private static String lastWord(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "";
        }
        String trimmed = s.trim();
        int idx = trimmed.lastIndexOf(' ');
        return idx < 0 ? trimmed : trimmed.substring(idx + 1);
    }
}
