package com.urpay.tests.wallet;

import java.util.concurrent.ThreadLocalRandom;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.QattaFlow;
import com.urpay.flows.RejectQuickQattaFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.QuickQattaPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Reject Quick (single) Qatta Suite (two users).
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/QattaSuites/RejectQuickQattaSuite
 *
 * Phase 1 (sender) creates a single qatta to one contact, then logs out.
 * Phase 2 (receiver) logs in, rejects the received qatta, and verifies the rejection.
 *
 * Katalon mapping:
 *   SetupTestDataForRejectQuickQatta / Member2 → quickQatta.* properties (shared with PayQuick)
 *   ToValidateCreatenewQuickQatta              → testSenderCreatesQuickQatta()
 *   ToRejectQuickQattaInReceivedTab            → testReceiverRejectsQuickQatta()
 */
@Epic("Wallet & VAS")
@Feature("Reject Quick Qatta")
public class RejectQuickQattaTest extends BaseTest {

    // ══════════════════════════════════════════════════
    //  1) SENDER: CREATE SINGLE QATTA + LOGOUT
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 1)
    @Story("Sender creates a quick qatta")
    @Description("Login as sender, create a single qatta to one contact, then log out")
    @Severity(SeverityLevel.CRITICAL)
    public void testSenderCreatesQuickQatta() {
        ConfigManager config = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsSender();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after sender login");

        String qattaName = config.get("quickQatta.qattaName") + randomDigits(4);
        new QattaFlow().navigateToSingleQatta();

        RejectQuickQattaFlow flow = new RejectQuickQattaFlow();
        QuickQattaPage page = flow.createQuickQatta(
                qattaName,
                config.get("quickQatta.recipientMobile"),
                config.get("quickQatta.recipientName"));

        Assert.assertTrue(page.isQattaCreatedSuccess(), "Quick qatta should be created successfully");
        page.tapDone();

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  2) RECEIVER: REJECT THE QATTA
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 2,
            dependsOnMethods = "testSenderCreatesQuickQatta")
    @Story("Receiver rejects the quick qatta")
    @Description("Login as receiver, reject the received single qatta, verify rejection")
    @Severity(SeverityLevel.CRITICAL)
    public void testReceiverRejectsQuickQatta() {
        DashboardPage dashboard = loginAsReceiver();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after receiver login");

        new QattaFlow().navigateToSingleQatta();

        QuickQattaPage page = new RejectQuickQattaFlow().rejectReceivedQatta();

        Assert.assertTrue(page.isRejectSuccess(), "Qatta rejection success message should be displayed");
        page.tapBack();
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as Quick Qatta sender user")
    private DashboardPage loginAsSender() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("quickQatta.sender.mobileNumber"),
                config.get("quickQatta.sender.id"),
                config.get("quickQatta.sender.verificationCode", "1234"),
                config.get("quickQatta.sender.passCode", "2233"));
    }

    @Step("Login as Quick Qatta receiver user")
    private DashboardPage loginAsReceiver() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("quickQatta.receiver.mobileNumber"),
                config.get("quickQatta.receiver.id"),
                config.get("quickQatta.receiver.verificationCode", "1234"),
                config.get("quickQatta.receiver.passCode", "2233"));
    }

    private String randomDigits(int count) {
        int bound = (int) Math.pow(10, count);
        int min = (int) Math.pow(10, count - 1);
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, bound));
    }
}
