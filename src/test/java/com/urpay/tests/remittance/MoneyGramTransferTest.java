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
 * MoneyGram International Transfer (Remittance squad) — the first MTO (Money Transfer Operator)
 * migrated. All the plumbing (page objects, flow, data model) is operator-agnostic, so
 * Western Union / Tahweel AlRajhi / Transfast / H2H reuse it by supplying their own data.
 *
 * Migrated from Katalon:
 *   Scripts/Remittance/InternationalTran/Perform International Transfer/
 *     - To Validate Perform Moneygram Cach Pickup International Transfer for Indian Beneficiary
 *     - To Validate Perform Moneygram Bank Deposit International Transfer for Indian Beneficiary
 *     - To Validate Perform Moneygram Send To Wallet International Transfer for Philippine Beneficiary
 *     - To Validate International Transfer information at confirmation Page (asserted here)
 *
 * Strategy: the login account (moneygram.user.* = 0520378665) already has the three MoneyGram
 * beneficiaries (Cash Pickup / Bank Deposit / Send to Wallet). Each case searches and selects its
 * beneficiary; if the account has no such beneficiary the case SKIPS cleanly (no failure).
 *
 * ⚠ REAL MONEY: the three transfer cases move real funds (small amounts from config). Each case
 * drives its corridor ONCE to the confirmation screen, validates the confirmation details there
 * (no money yet), then confirms — so the beneficiary→confirm steps are never executed twice.
 *
 * Single LambdaTest session: priority-1 is a login anchor; the corridor transfers reuse the
 * session (dependsOnMethods) and navigate via the urpay://international deep link, so they run
 * independently (one corridor failing does not skip the others).
 */
@Epic("Remittance")
@Feature("International Transfer - MoneyGram")
public class MoneyGramTransferTest extends BaseTest {

    @Test(priority = 1, groups = {"remittance", "international-transfer", "moneygram", "smoke"})
    @Story("MoneyGram Bank Deposit")
    @Description("Login (Bank Deposit account) → International Transfer → Bank Deposit beneficiary → "
            + "amount → MoneyGram → purpose → CONFIRMATION: verify delivery option → confirm → OTP → "
            + "success (Thank You). REAL MONEY. Skips if no such beneficiary.")
    @Severity(SeverityLevel.CRITICAL)
    public void testMoneyGramBankDepositToIndia() {
        loginAndTransfer("moneygram.bankDeposit");
    }

    @Test(priority = 2, groups = {"remittance", "international-transfer", "moneygram"})
    @Story("MoneyGram Cash Pickup")
    @Description("Login (Cash Pickup account) → International Transfer → Cash Pickup beneficiary → "
            + "amount → MoneyGram → purpose → CONFIRMATION → confirm → OTP → success (Thank You). "
            + "REAL MONEY. Skips if no such beneficiary.")
    @Severity(SeverityLevel.CRITICAL)
    public void testMoneyGramCashPickupToIndia() {
        loginAndTransfer("moneygram.cashPickup");
    }

    @Test(priority = 3, groups = {"remittance", "international-transfer", "moneygram"})
    @Story("MoneyGram Send to Wallet")
    @Description("Login (Send-to-Wallet account) → International Transfer → Send-to-Wallet "
            + "beneficiary → amount → MoneyGram → purpose → CONFIRMATION → confirm → OTP → success "
            + "(Thank You). REAL MONEY. Skips if no such beneficiary.")
    @Severity(SeverityLevel.NORMAL)
    public void testMoneyGramSendToWalletTransfer() {
        loginAndTransfer("moneygram.sendToWallet");
    }

    /**
     * Log in with the corridor's OWN account ({@code <prefix>.user.*}) so each MoneyGram corridor
     * can run on a SEPARATE account in parallel (Bank Deposit / Cash Pickup / Send-to-Wallet each
     * have their own beneficiary on their own account), then drive + validate the transfer.
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
     * Shared body for the real-money MoneyGram transfers (data driven by config prefix): drive to
     * the confirmation screen ONCE, validate the delivery option there, then confirm → OTP →
     * success. The confirmation is validated on the same screen right before confirming, so the
     * beneficiary→confirm steps are never executed twice.
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

        // Validate the confirmation details BEFORE confirming (this was the old no-money case).
        Assert.assertTrue(page.isConfirmationVisible(15),
                "The MoneyGram confirmation screen should render before confirming");
        String delivery = page.getConfirmationValue("Delivery Option");
        String expectedKeyword = lastWord(data.getDeliveryOption()).toLowerCase();
        Assert.assertTrue(delivery.toLowerCase().contains(expectedKeyword),
                "Confirmation 'Delivery Option' should reflect " + data.getDeliveryOption()
                + " (was: '" + delivery + "')");

        // Confirm the SAME transfer → OTP → success (REAL MONEY). No re-running the flow.
        InternationalTransferFlow.TransferResult result =
                flow.confirmAndFinish(page, c.get(configPrefix + ".user.verificationCode", "1234"));
        Assert.assertEquals(result, InternationalTransferFlow.TransferResult.SUCCESS,
                "Success (Thank You) screen should be visible after the MoneyGram "
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
