package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.FamilyPermissionPage;
import com.urpay.pages.wallet.FamilyWalletPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Family Permission Control — freeze / unfreeze a kid wallet.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/Family Wallet Permission Control Test Suite
 *   ({@code Wallet_VAS/FamilyWallet/Permission-Screen/FreezeKid/UnFreezeKid}).
 *
 * <p>Phase 1: parent opens the kid profile settings, toggles Freeze, verifies the warning text, and
 * confirms the freeze. Phase 2: parent toggles Freeze again to unfreeze and confirms.
 */
@Epic("Wallet & VAS")
@Feature("Family Permission Control")
public class PermissionControlTest extends BaseTest {

    @Test(groups = {"wallet", "permission-control", "family"}, priority = 1)
    @Story("Parent freezes a kid wallet")
    @Description("Parent opens the kid profile settings, toggles Freeze, verifies the freeze warning "
            + "text, and confirms the freeze")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentFreezesKid() {
        ConfigManager c = ConfigManager.getInstance();
        FamilyPermissionPage permission = openKidSettings();

        permission.toggleFreeze();
        Assert.assertTrue(
                permission.getFreezeWarning().contains(c.get("permissionControl.freezeWarningPrefix")),
                "The freeze-wallet warning text should be displayed");
        permission.confirmFreeze();
    }

    @Test(groups = {"wallet", "permission-control", "family"}, priority = 2,
            dependsOnMethods = "testParentFreezesKid")
    @Story("Parent unfreezes the kid wallet")
    @Description("Parent toggles Freeze again to reactivate the kid wallet and confirms")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentUnfreezesKid() {
        FamilyPermissionPage permission = new FamilyPermissionPage();
        permission.openSettings();
        permission.toggleFreeze();
        permission.confirmFreeze();
    }

    @Step("Login as parent, open the family wallet, the kid profile and its settings")
    private FamilyPermissionPage openKidSettings() {
        DashboardPage dashboard = loginAsParent();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent login");

        FamilyWalletPage family = new FamilyWalletPage();
        family.tapFamilyWallet();
        family.waitUntilLoaded();
        family.tapFirstFamilyMember();

        FamilyPermissionPage permission = new FamilyPermissionPage();
        permission.openSettings();
        return permission;
    }

    @Step("Login as the permission-control parent")
    private DashboardPage loginAsParent() {
        ConfigManager c = ConfigManager.getInstance();
        return new LoginFlow().loginWith(
                c.get("permissionControl.parent.mobileNumber"),
                c.get("permissionControl.parent.id"),
                c.get("permissionControl.parent.verificationCode", "1234"),
                c.get("permissionControl.parent.passCode", "2233"));
    }
}
