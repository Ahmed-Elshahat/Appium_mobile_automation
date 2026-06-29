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
 *   SetupTestDataForRejectQuickQatta / Member2 → rejectQuick.* properties (own accounts)
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

        String qattaName = config.get("rejectQuick.qattaName") + randomDigits(4);
        new QattaFlow().navigateToSingleQatta();

        RejectQuickQattaFlow flow = new RejectQuickQattaFlow();
        QuickQattaPage page = flow.createQuickQatta(
                qattaName,
                config.get("rejectQuick.recipientMobile"),
                config.get("rejectQuick.recipientName"));

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
                config.get("rejectQuick.sender.mobileNumber"),
                config.get("rejectQuick.sender.id"),
                config.get("rejectQuick.sender.verificationCode", "1234"),
                config.get("rejectQuick.sender.passCode", "2233"));
    }

    @Step("Login as Quick Qatta receiver user")
    private DashboardPage loginAsReceiver() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("rejectQuick.receiver.mobileNumber"),
                config.get("rejectQuick.receiver.id"),
                config.get("rejectQuick.receiver.verificationCode", "1234"),
                config.get("rejectQuick.receiver.passCode", "2233"));
    }

    private String randomDigits(int count) {
        int bound = (int) Math.pow(10, count);
        int min = (int) Math.pow(10, count - 1);
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, bound));
    }
}
