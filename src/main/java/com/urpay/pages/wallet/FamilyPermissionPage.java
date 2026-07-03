package com.urpay.pages.wallet;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Family Permission Control — parent freezes / unfreezes a kid wallet from the kid profile settings.
 *
 * <p>Migrated from Katalon suite:
 *   Test Suites/.../WMVSuites/Family Wallet Permission Control Test Suite
 *   ({@code Wallet_VAS/FamilyWallet/Permission-Screen/*}). This migration covers the core
 *   Freeze / Unfreeze permission; the granular card / wallet / DMP permission toggles are extended
 *   cases not yet migrated.
 *
 * <p>Navigation to the family wallet and opening the kid profile is handled by
 * {@link FamilyWalletPage}.
 */
public class FamilyPermissionPage extends BasePage {

    // Kid-profile settings gear (Katalon settingScreen/settingsBtn).
    private static final By SETTINGS_BTN =
            AppiumBy.accessibilityId("testID-right-icon-0");
    private static final By FREEZE_TOGGLE =
            AppiumBy.accessibilityId("testID-switcher-switch_1");
    private static final By FREEZE_WARNING_TEXT =
            AppiumBy.accessibilityId("testID-total-transfer-message");
    private static final By FREEZE_CONFIRM_BTN =
            AppiumBy.accessibilityId("testID-primary-freezeAccount-main");

    @Step("Open the kid profile settings")
    public void openSettings() {
        tap(SETTINGS_BTN);
    }

    @Step("Toggle the Freeze Account switch")
    public void toggleFreeze() {
        tap(FREEZE_TOGGLE);
    }

    @Step("Read the freeze-wallet warning text")
    public String getFreezeWarning() {
        return getText(FREEZE_WARNING_TEXT);
    }

    @Step("Confirm the freeze / unfreeze action")
    public void confirmFreeze() {
        tap(FREEZE_CONFIRM_BTN);
    }
}
