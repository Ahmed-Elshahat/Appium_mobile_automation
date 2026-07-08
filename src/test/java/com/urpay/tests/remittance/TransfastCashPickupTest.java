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
 * Transfast International Transfer (Remittance squad) — Cash Pickup.
 *
 * Reuses the operator-agnostic plumbing (InternationalTransferPage / InternationalTransferFlow /
 * InternationalTransferData); Transfast only supplies its data. Transfast is provider card 3 on
 * this India route and has NO payout-bank step (unlike Tahweel/H2H), so the standard flow applies.
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/Perform International Transfer/
 *     - To Validate Perform Transfast Cach Pickup International Transfer for Indian Beneficiary
 *       (serviceProvider "Transfast", serviceProviderIndex 3, deliveryOption "Cash Pickup",
 *        India / Indian Rupee / 15.50 SAR, beneficiary "Saif Hussein")
 *
 * Strategy: the login account (transfast.cashPickup.user.*) is expected to have an active Transfast
 * Cash Pickup beneficiary; the case searches + selects it. If the account has no such beneficiary
 * the case SKIPS cleanly, and the flow logs the account's beneficiaries so the real name can be
 * pinned in config.
 *
 * ⚠ REAL MONEY: the case moves real funds (small amount from config). It drives the flow ONCE to
 * the confirmation screen, validates the confirmation details there (no money yet), and only then
 * confirms — so the beneficiary→confirm steps are NOT executed twice.
 */
@Epic("Remittance")
@Feature("International Transfer - Transfast")
public class TransfastCashPickupTest extends BaseTest {

    private static final String OTP_KEY = "transfast.cashPickup.user.verificationCode";
    private static final String PREFIX = "transfast.cashPickup";

    @Test(priority = 1, groups = {"remittance", "international-transfer", "transfast",
            "transfast-cashpickup", "smoke"})
    @Story("Transfast Cash Pickup transfer")
    @Description("Login → International Transfer → search + select the Transfast Cash Pickup "
            + "beneficiary → amount → select Transfast (card 3) → purpose → CONFIRMATION page: "
            + "verify it renders & the delivery option matches, THEN confirm → OTP → success (Thank "
            + "You). The confirmation details are validated on the same screen just before "
            + "confirming, so the flow runs ONCE. REAL MONEY.")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransfastCashPickupTransfer() {
        ConfigManager c = ConfigManager.getInstance();

        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get("transfast.cashPickup.user.mobileNumber"),
                c.get("transfast.cashPickup.user.id"),
                c.get(OTP_KEY, "1234"),
                c.get("transfast.cashPickup.user.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        InternationalTransferData data = InternationalTransferData.fromConfig(c, PREFIX);
        InternationalTransferFlow flow = new InternationalTransferFlow();

        // 1) Drive to the confirmation screen (no money spent yet).
        InternationalTransferPage page = flow.openConfirmation(data);
        if (page == null) {
            throw new SkipException("No Transfast " + data.getDeliveryOption() + " beneficiary '"
                    + data.getBeneficiaryName() + "' on account "
                    + c.get("transfast.cashPickup.user.mobileNumber")
                    + " — check the logged 'beneficiaries on the account' and set "
                    + PREFIX + ".beneficiaryName.");
        }

        // 2) Validate the confirmation details BEFORE confirming (no money yet).
        Assert.assertTrue(page.isConfirmationVisible(15),
                "The Transfast confirmation screen should render before confirming");
        String delivery = page.getConfirmationValue("Delivery Option");
        String expectedKeyword = lastWord(data.getDeliveryOption()).toLowerCase();
        Assert.assertTrue(delivery.toLowerCase().contains(expectedKeyword),
                "Confirmation 'Delivery Option' should reflect " + data.getDeliveryOption()
                + " (was: '" + delivery + "')");

        // 3) Confirm the SAME transfer → OTP → success (REAL MONEY). No re-running the flow.
        InternationalTransferFlow.TransferResult result =
                flow.confirmAndFinish(page, c.get(OTP_KEY, "1234"));
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "Success (Thank You) screen should be visible after the Transfast "
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
