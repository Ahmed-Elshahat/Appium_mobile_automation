package com.urpay.tests.wallet;

import java.util.concurrent.ThreadLocalRandom;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.QattaFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.QattaGroupPage;
import com.urpay.pages.wallet.QattaPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Edit Group Qatta Details.
 *
 * Migrated from Katalon suite:
 *   Test Suites/Test Suite Collections/WMVSuites/QattaSuites/EditGroupQattaDetails
 *
 * Katalon test cases → mapping:
 *   1. Setup Test Data For EditGroup Qatta            → qatta.* properties (sit-cards.properties)
 *   2. ToValidateLoginForRemoteDevicesOnly            → LoginFlow.loginWith() in step 1
 *   3. toValidateMoveToDashboardServices + ToNavigateToQatta → testNavigateToQattaGroups()
 *   4. ToCreateNewQattaGroup                          → testCreateNewQattaGroup()
 *   5. editFirstQattaDetails                          → testEditGroupQattaName()
 *   6. ValidateGroupQattaNameFromGroupDetailsTab      → testValidateUpdatedGroupName()
 */
@Epic("Wallet & VAS")
@Feature("Qatta Group")
public class EditGroupQattaDetailsTest extends BaseTest {

    // Test data generated at runtime and shared across the chained tests.
    private String groupName;
    private String editedGroupName;

    // ══════════════════════════════════════════════════
    //  1) NAVIGATE TO QATTA GROUPS
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta", "smoke"}, priority = 1)
    @Story("Navigate to Qatta Group")
    @Description("Login and navigate Dashboard → services → Qatta → group option")
    @Severity(SeverityLevel.CRITICAL)
    public void testNavigateToQattaGroups() {
        DashboardPage dashboard = login();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after login");

        QattaPage qatta = new QattaFlow().navigateToQattaGroups();

        Assert.assertTrue(qatta.isLoaded(), "Qatta group option should be displayed");
    }

    // ══════════════════════════════════════════════════
    //  2) CREATE NEW GROUP QATTA
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 2,
            dependsOnMethods = "testNavigateToQattaGroups")
    @Story("Create Group Qatta")
    @Description("Create a new qatta group with two members")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreateNewQattaGroup() {
        ConfigManager config = ConfigManager.getInstance();
        groupName = "Group" + randomDigits(4);

        QattaGroupPage page = new QattaFlow().createGroup(
                groupName,
                config.get("qatta.firstMemberMobile"),
                config.get("qatta.firstMemberName"),
                config.get("qatta.secondMemberMobile"),
                config.get("qatta.secondMemberName"));

        Assert.assertTrue(page.isGroupNameDisplayed(groupName),
                "Newly created group should appear in the qatta group list");
    }

    // ══════════════════════════════════════════════════
    //  3) EDIT GROUP QATTA NAME
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 3,
            dependsOnMethods = "testCreateNewQattaGroup")
    @Story("Edit Group Qatta Name")
    @Description("Open the first group, edit its name and verify the success message")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditGroupQattaName() {
        ConfigManager config = ConfigManager.getInstance();
        editedGroupName = "Group" + randomDigits(6);

        QattaGroupPage page = new QattaFlow().editFirstGroupName(editedGroupName);

        Assert.assertTrue(page.isNameUpdatedMessageDisplayed(),
                "A success notification should appear after saving the new name");
        Assert.assertEquals(page.getUpdatedMessage(),
                config.get("qatta.nameUpdatedMsg"),
                "Update success message should match");
    }

    // ══════════════════════════════════════════════════
    //  4) VALIDATE UPDATED GROUP NAME
    // ══════════════════════════════════════════════════

    @Test(groups = {"wallet", "qatta"}, priority = 4,
            dependsOnMethods = "testEditGroupQattaName")
    @Story("Validate Group Qatta Name")
    @Description("Verify the updated group name is displayed on the group details tab")
    @Severity(SeverityLevel.NORMAL)
    public void testValidateUpdatedGroupName() {
        QattaGroupPage page = new QattaGroupPage();
        page.returnToGroupDetails();

        Assert.assertTrue(page.isGroupDetailsHeaderDisplayed(),
                "Group details header should be displayed");
        Assert.assertEquals(page.getDisplayedGroupName(editedGroupName), editedGroupName,
                "Group details tab should show the updated group name");
    }

    // ══════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════

    @Step("Login with Qatta test user")
    private DashboardPage login() {
        ConfigManager config = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                config.get("qatta.mobileNumber"),
                config.get("qatta.id"),
                config.get("qatta.verificationCode", "1234"),
                config.get("qatta.passCode", "2233"));
    }

    private String randomDigits(int count) {
        int bound = (int) Math.pow(10, count);
        int min = (int) Math.pow(10, count - 1);
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, bound));
    }
}
