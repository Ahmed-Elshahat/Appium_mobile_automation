package com.urpay.tests.wallet;

import java.util.concurrent.ThreadLocalRandom;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.PayGroupQattaFlow;
import com.urpay.flows.QattaFlow;
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
 * Pay Group Qatta Suite (two users).
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/QattaSuites/PayGroupQattaSuite
 *
 * Phase 1 (admin) creates a group with two members and adds two qattas, then validates and
 * logs out. Phase 2 (member) logs in, pays both qattas, and logs out.
 *
 * Katalon mapping:
 *   SetupTest DataforPayGroupQattaGroup / Setup …-Member1 → qattaPay.* properties
 *   ToaddNewFirstQatta / ToaddNewSecondQatta              → testAdminAddsQattas()
 *   ToValidateMultiGroupQattaDetails                      → testValidateGroupDetailsAndLogout()
 *   ToPayMultiQattaWithinGroup                            → testMemberPaysQattas()
 */
@Epic("Wallet & VAS")
@Feature("Pay Group Qatta")
public class PayGroupQattaTest extends BaseTest {

    private String groupName;

    // ══════════════════════════════════════════════════
    //  1) ADMIN: CREATE GROUP + ADD TWO QATTAS
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta", "smoke"}, priority = 1)
    @Story("Admin adds qattas to a group")
    @Description("Login as admin, create a group with two members, add two qattas")
    @Severity(SeverityLevel.CRITICAL)
    public void testAdminAddsQattas() {
        ConfigManager config = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsAdmin();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after admin login");

        groupName = "Group" + randomDigits(4);
        new QattaFlow().navigateToQattaGroups();
        new QattaFlow().createGroup(
                groupName,
                config.get("qattaPay.firstMemberMobile"),
                config.get("qattaPay.firstMemberName"),
                config.get("qattaPay.secondMemberMobile"),
                config.get("qattaPay.secondMemberName"));

        PayGroupQattaFlow flow = new PayGroupQattaFlow();
        GroupQattaPage page = flow.addQattaToLatestGroup(config.get("qattaPay.firstQattaName"), true);
        Assert.assertTrue(page.isQattaCreatedSuccess(), "First qatta should be created successfully");
        page.tapDone();

        page = flow.addQattaToLatestGroup(config.get("qattaPay.secondQattaName"), false);
        Assert.assertTrue(page.isQattaCreatedSuccess(), "Second qatta should be created successfully");
        page.tapDone();
    }

    // ══════════════════════════════════════════════════
    //  2) ADMIN: VALIDATE DETAILS + LOGOUT
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 2,
            dependsOnMethods = "testAdminAddsQattas")
    @Story("Validate group qatta details")
    @Description("Open the group, verify status is Active, then log out the admin")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateGroupDetailsAndLogout() {
        ConfigManager config = ConfigManager.getInstance();
        GroupQattaPage page = new GroupQattaPage();
        page.openLatestGroup();

        Assert.assertEquals(page.getGroupStatus(), config.get("qattaPay.groupStatus"),
                "Group status should be Active");

        new PayGroupQattaFlow().logout();
    }

    // ══════════════════════════════════════════════════
    //  3) MEMBER: PAY BOTH QATTAS
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 3,
            dependsOnMethods = "testValidateGroupDetailsAndLogout")
    @Story("Member pays group qattas")
    @Description("Login as member, pay both qattas within the group, verify success")
    @Severity(SeverityLevel.CRITICAL)
    public void testMemberPaysQattas() {
        ConfigManager config = ConfigManager.getInstance();
        DashboardPage dashboard = loginAsMember();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after member login");

        new QattaFlow().navigateToQattaGroups();

        GroupQattaPage page = new PayGroupQattaFlow()
                .payBothQattas(config.get("qattaPay.member.verificationCode", "1234"));

        Assert.assertTrue(page.isPaymentSuccess(), "Payment success message should be displayed");
        page.tapPayDone();
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login as Qatta admin user")
    private DashboardPage loginAsAdmin() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("qattaPay.admin.mobileNumber"),
                config.get("qattaPay.admin.id"),
                config.get("qattaPay.admin.verificationCode", "1234"),
                config.get("qattaPay.admin.passCode", "2233"));
    }

    @Step("Login as Qatta member user")
    private DashboardPage loginAsMember() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("qattaPay.member.mobileNumber"),
                config.get("qattaPay.member.id"),
                config.get("qattaPay.member.verificationCode", "1234"),
                config.get("qattaPay.member.passCode", "2233"));
    }

    private String randomDigits(int count) {
        int bound = (int) Math.pow(10, count);
        int min = (int) Math.pow(10, count - 1);
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, bound));
    }
}
