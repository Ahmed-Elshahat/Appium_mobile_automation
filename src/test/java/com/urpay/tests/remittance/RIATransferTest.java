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
 * RIA International Transfer (Remittance squad) — Cash Pickup & Bank Deposit.
 *
 * Reuses the operator-agnostic plumbing built for MoneyGram / Western Union / Tahweel / Transfast
 * (InternationalTransferPage / InternationalTransferFlow / InternationalTransferData); RIA only
 * supplies its data.
 *
 * Strategy: each corridor logs in with its OWN account (ria.<corridor>.user.*) so the corridors
 * can run in parallel. Each case searches + selects its beneficiary; if the account has no such
 * beneficiary the case SKIPS cleanly (no failure).
 *
 * ⚠ REAL MONEY: the transfer cases move real funds (small amounts from config). Each case drives
 * to the confirmation screen ONCE, validates the confirmation details, then confirms → OTP →
 * success (Thank You).
 *
 * Command:
 *   mvn clean test "-Dsuite=suites/ria-transfer.xml" "-Dprofile=sit-remittance"
 *       -Denv=SIT -Dremote=true
 */
@Epic("Remittance")
@Feature("International Transfer - RIA")
public class RIATransferTest extends BaseTest {

    @Test(priority = 1, groups = {"remittance", "international-transfer", "ria", "smoke"})
    @Story("RIA Cash Pickup")
    @Description("Login (Cash Pickup account) → International Transfer → Cash Pickup beneficiary → "
            + "amount → RIA → purpose → CONFIRMATION: verify delivery option → confirm → OTP → "
            + "success (Thank You). REAL MONEY. Skips if no such beneficiary.")
    @Severity(SeverityLevel.CRITICAL)
    public void testRIACashPickupTransfer() {
        loginAndTransfer("ria.cashPickup");
    }

    @Test(priority = 2, groups = {"remittance", "international-transfer", "ria"})
    @Story("RIA Bank Deposit")
    @Description("Login (Bank Deposit account) → International Transfer → Bank Deposit beneficiary → "
            + "amount → RIA → purpose → CONFIRMATION: verify delivery option → confirm → OTP → "
            + "success (Thank You). REAL MONEY. Skips if no such beneficiary.")
    @Severity(SeverityLevel.CRITICAL)
    public void testRIABankDepositTransfer() {
        loginAndTransfer("ria.bankDeposit");
    }

    /**
     * Log in with the corridor's OWN account ({@code <prefix>.user.*}) then drive + validate
     * the transfer.
     */
    private void loginAndTransfer(String prefix) {
        ConfigManager c = ConfigManager.getInstance();
        DashboardPage dashboard = new LoginFlow().loginWith(
                c.get(prefix + ".user.mobileNumber"),
                c.get(prefix + ".user.id"),
                c.get(prefix + ".user.verificationCode", "1234"),
                c.get(prefix + ".user.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");
        transferAndValidate(prefix);
    }

    /**
     * Drive the transfer to the confirmation screen, validate delivery option, then confirm →
     * OTP → success. The confirmation is validated on the same screen right before confirming,
     * so the beneficiary→confirm steps are never executed twice.
     */
    private void transferAndValidate(String configPrefix) {
        ConfigManager c = ConfigManager.getInstance();
        InternationalTransferData data = InternationalTransferData.fromConfig(c, configPrefix);
        InternationalTransferFlow flow = new InternationalTransferFlow();

        InternationalTransferPage page = flow.openConfirmation(data);
        if (page == null) {
            throw new SkipException("No " + data.getServiceProvider() + " " + data.getDeliveryOption()
                    + " beneficiary '" + data.getBeneficiaryName() + "' on the account — "
                    + "add & activate it before running this case.");
        }

        // Validate the confirmation details BEFORE confirming (no money yet).
        Assert.assertTrue(page.isConfirmationVisible(15),
                "The RIA confirmation screen should render before confirming");
        String delivery = page.getConfirmationValue("Delivery Option");
        String expectedKeyword = lastWord(data.getDeliveryOption()).toLowerCase();
        Assert.assertTrue(delivery.toLowerCase().contains(expectedKeyword),
                "Confirmation 'Delivery Option' should reflect " + data.getDeliveryOption()
                + " (was: '" + delivery + "')");

        // Confirm the SAME transfer → OTP → success (REAL MONEY).
        InternationalTransferFlow.TransferResult result =
                flow.confirmAndFinish(page, c.get(configPrefix + ".user.verificationCode", "1234"));
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "Success (Thank You) screen should be visible after the RIA "
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
