package com.urpay.pages.auth;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Registration page — accessed from Landing → Register.
 * Handles invitation code toggle and input during registration.
 *
 * Katalon source: Object Repository/android/WalletVas/InviteFriends/registerPage/
 */
public class RegistrationPage extends BasePage {

    // ── Invitation Code section ──────────────────────
    private static final By INVITATION_CODE_TOGGLE =
            AppiumBy.accessibilityId("testID-switcher-undefined");

    private static final By INVITATION_CODE_INPUT =
            AppiumBy.accessibilityId("testID-input-direct-referralCode");

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Enable invitation code toggle")
    public void enableInvitationCodeToggle() {
        waitUtils.waitForClickable(INVITATION_CODE_TOGGLE, 10);
        tap(INVITATION_CODE_TOGGLE);
    }

    @Step("Enter invitation code: {code}")
    public void enterInvitationCode(String code) {
        waitUtils.waitForClickable(INVITATION_CODE_INPUT, 10);
        type(INVITATION_CODE_INPUT, code);
    }

    public boolean isInvitationCodeInputVisible() {
        return waitUtils.isPresent(INVITATION_CODE_INPUT, 10);
    }

    public boolean isLoaded() {
        return waitUtils.isPresent(INVITATION_CODE_TOGGLE, 15);
    }
}
