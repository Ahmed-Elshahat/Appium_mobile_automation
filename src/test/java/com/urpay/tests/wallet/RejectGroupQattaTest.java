package com.urpay.tests.wallet;

import java.util.concurrent.ThreadLocalRandom;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.QattaFlow;
import com.urpay.flows.RejectGroupQattaFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.GroupQattaPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Reject Group Qatta Suite (two users).
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/QattaSuites/RejectQroupQattaSuite
 *
 * Phase 1 (admin) creates a group with two members and adds two qattas, then logs out.
 * Phase 2 (member) logs in and rejects both qattas from their detail screens.
 *
 * Katalon mapping:
 *   SetupTest Data for Reject Qatta Group / -Member1 → rejectGroup.* properties (own accounts)
 *   ToaddNewFirstQatta / ToaddNewSecondQatta         → testAdminAddsQattas()
 *   ToRejectFirstQattainGroup / ToRejectSecondQattainGroup → testMemberRejectsQattas()
 */
@Epic("Wallet & VAS")
@Feature("Reject Group Qatta")
public class RejectGroupQattaTest extends BaseTest {

    private String groupName;

    // ══════════════════════════════════════════════════
    //  1) ADMIN: CREATE GROUP + ADD TWO QATTAS + LOGOUT
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 1)
    @Story("Admin adds qattas to a group")
    @Description("Login as admin, create a group with two members, add two qattas, then log out")
    @Severity(SeverityLevel.CRITICAL)
    public void testAdminAddsQattas() {
        ConfigManager config = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsAdmin();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after admin login");

        groupName = "Group" + randomDigits(4);
        new QattaFlow().navigateToQattaGroups();
        new QattaFlow().createGroup(
                groupName,
                config.get("rejectGroup.firstMemberMobile"),
                config.get("rejectGroup.firstMemberName"),
                config.get("rejectGroup.secondMemberMobile"),
                config.get("rejectGroup.secondMemberName"));

        RejectGroupQattaFlow flow = new RejectGroupQattaFlow();
        GroupQattaPage page = flow.addQattaToLatestGroup(config.get("rejectGroup.firstQattaName"), true);
        Assert.assertTrue(page.isQattaCreatedSuccess(), "First qatta should be created successfully");
        page.tapDone();

        page = flow.addQattaToLatestGroup(config.get("rejectGroup.secondQattaName"), false);
        Assert.assertTrue(page.isQattaCreatedSuccess(), "Second qatta should be created successfully");
        page.tapDone();

        flow.logout();
    }

    // ══════════════════════════════════════════════════
    //  2) MEMBER: REJECT BOTH QATTAS
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 2,
            dependsOnMethods = "testAdminAddsQattas")
    @Story("Member rejects group qattas")
    @Description("Login as member, reject both qattas within the group, verify rejection")
    @Severity(SeverityLevel.CRITICAL)
    public void testMemberRejectsQattas() {
        DashboardPage dashboard = loginAsMember();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after member login");

        new QattaFlow().navigateToQattaGroups();

        RejectGroupQattaFlow flow = new RejectGroupQattaFlow();
        flow.openGroupUnpaidQattas();

        GroupQattaPage page = flow.rejectTopUnpaidQatta();
        Assert.assertTrue(page.isRejectSuccess(), "First qatta should be rejected successfully");
        page.tapBack();

        page = flow.rejectTopUnpaidQatta();
        Assert.assertTrue(page.isRejectSuccess(), "Second qatta should be rejected successfully");
        page.tapBack();
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as Qatta admin user")
    private DashboardPage loginAsAdmin() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("rejectGroup.admin.mobileNumber"),
                config.get("rejectGroup.admin.id"),
                config.get("rejectGroup.admin.verificationCode", "1234"),
                config.get("rejectGroup.admin.passCode", "2233"));
    }

    @Step("Login as Qatta member user")
    private DashboardPage loginAsMember() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("rejectGroup.member.mobileNumber"),
                config.get("rejectGroup.member.id"),
                config.get("rejectGroup.member.verificationCode", "1234"),
                config.get("rejectGroup.member.passCode", "2233"));
    }

    private String randomDigits(int count) {
        int bound = (int) Math.pow(10, count);
        int min = (int) Math.pow(10, count - 1);
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, bound));
    }
}
