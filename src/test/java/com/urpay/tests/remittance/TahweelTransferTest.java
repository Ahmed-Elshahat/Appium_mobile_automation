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
 * Tahweel AlRajhi (H2H) International Transfer (Remittance squad) — Cash Pickup.
 *
 * Reuses the operator-agnostic plumbing built for MoneyGram / Western Union
 * (InternationalTransferPage / InternationalTransferFlow / InternationalTransferData); Tahweel
 * AlRajhi only supplies its data. The provider is branded "Tahweel Alrajhi" and is the H2H
 * (house-to-house) MTO in Katalon — the two names are the same operator.
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/Perform International Transfer/
 *     - To Validate Perform H2H Cach Pickup International Transfer for Indian Beneficiary
 *
 * Tahweel-specific wrinkle: after selecting the provider, its Cash Pickup route shows a
 * payout-bank dropdown on the provider screen that must be set before Next (migrated as
 * InternationalTransferPage.selectFirstBank, driven by the flow's provider-name gate). MoneyGram /
 * Western Union have no such step.
 *
 * Strategy: the login account (tahweel.user.* = 0520876253, poi 2557857444 IQA) is expected to
 * have an active Tahweel AlRajhi Cash Pickup beneficiary; the case searches + selects it. If the
 * account has no such beneficiary the case SKIPS cleanly. On a not-found beneficiary the flow logs
 * the account's beneficiaries so the real beneficiary name can be pinned in config.
 *
 * ⚠ REAL MONEY: the case moves real funds (small amount from config). It drives the flow ONCE to
 * the confirmation screen, validates the confirmation details there (no money yet), and only then
 * confirms — so the beneficiary→confirm steps are NOT executed twice.
 */
@Epic("Remittance")
@Feature("International Transfer - Tahweel AlRajhi (H2H)")
public class TahweelTransferTest extends BaseTest {

    private static final String OTP_KEY = "tahweel.user.verificationCode";
    private static final String PREFIX = "tahweel.cashPickup";

    @Test(priority = 1, groups = {"remittance", "international-transfer", "tahweel", "h2h", "smoke"})
    @Story("Tahweel AlRajhi (H2H) Cash Pickup transfer")
    @Description("Login → International Transfer → search + select the Tahweel AlRajhi Cash Pickup "
            + "beneficiary → amount → select Tahweel AlRajhi → payout bank → purpose → CONFIRMATION "
            + "page: verify it renders & the delivery option matches, THEN confirm → OTP → success "
            + "(Thank You). The confirmation details are validated on the same screen just before "
            + "confirming, so the flow runs ONCE (no re-doing the steps). REAL MONEY.")
    @Severity(SeverityLevel.CRITICAL)
    public void testTahweelCashPickupTransfer() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("tahweel.user.mobileNumber"),
                c.get("tahweel.user.id"),
                c.get(OTP_KEY, "1234"),
                c.get("tahweel.user.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        InternationalTransferData data = InternationalTransferData.fromConfig(c, PREFIX);
        InternationalTransferFlow flow = new InternationalTransferFlow();

        // 1) Drive to the confirmation screen (no money spent yet).
        InternationalTransferPage page = flow.openConfirmation(data);
        if (page == null) {
            throw new SkipException("No Tahweel AlRajhi " + data.getDeliveryOption() + " beneficiary '"
                    + data.getBeneficiaryName() + "' on account " + c.get("tahweel.user.mobileNumber")
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
