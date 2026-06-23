package com.urpay.pages.wallet;

import org.openqa.selenium.By;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.qameta.allure.Step;

/**
 * Invite Friends page — accessed from More → Invite Friends.
 * Displays referral code, invitation code, and registered users count.
 *
 * Katalon source: Object Repository/android/WalletVas/InviteFriends/
 */
public class InviteFriendsPage extends BasePage {

    // ── Navigation ───────────────────────────────────
    private static final By MORE_NAV =
            AppiumBy.accessibilityId("testID-MORENAV");

    private static final By INVITE_FRIENDS_BTN =
            AppiumBy.accessibilityId("testID-viewElemenRewardStar");

    // ── Tabs ─────────────────────────────────────────
    private static final By INVITE_FRIENDS_TAB =
            AppiumBy.accessibilityId("testID-Tabs.c013129a-a22b-4e3e-b9e4-b7d3d1b93736.0");

    private static final By REFERRAL_CODE_TAB =
            AppiumBy.accessibilityId("testID-Tabs.c013129a-a22b-4e3e-b9e4-b7d3d1b93736.1");

    // ── Content ──────────────────────────────────────
    private static final By REFERRAL_CODE_TEXT =
            AppiumBy.accessibilityId("testID-Text.ac2391aa-a64b-431f-beb7-ddfc9c70b07c");

    private static final By INVITATION_CODE_TEXT =
            AppiumBy.accessibilityId("testID-Text.ac2391aa-a64b-431f-beb7-ddfc9c70b07c");

    private static final By AMOUNT_TEXT =
            AppiumBy.accessibilityId("testID-Text.193dd286-8bac-421f-bd8c-b934e58279a0");

    private static final By REGISTERED_USERS_TEXT =
            AppiumBy.accessibilityId("testID-Text.7876fa59-e42a-449d-b40b-f41cf5a7f2f3");

    // ══════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════

    @Step("Navigate to Invite Friends page (More → Invite Friends)")
    public void navigateToInviteFriends() {
        tap(MORE_NAV);
        waitUtils.waitForVisible(INVITE_FRIENDS_BTN, 15);
        tap(INVITE_FRIENDS_BTN);
        waitUtils.waitForVisible(REFERRAL_CODE_TEXT, 15);
    }

    // ══════════════════════════════════════════════════
    //  TAB ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Invite Friends tab")
    public void tapInviteFriendsTab() {
        waitUtils.waitForClickable(INVITE_FRIENDS_TAB, 10);
        tap(INVITE_FRIENDS_TAB);
    }

    @Step("Tap Referral Code tab")
    public void tapReferralCodeTab() {
        waitUtils.waitForClickable(REFERRAL_CODE_TAB, 10);
        tap(REFERRAL_CODE_TAB);
    }

    // ══════════════════════════════════════════════════
    //  QUERIES
    // ══════════════════════════════════════════════════

    @Step("Get referral code text")
    public String getReferralCodeText() {
        waitUtils.waitForVisible(REFERRAL_CODE_TEXT, 10);
        return getText(REFERRAL_CODE_TEXT);
    }

    @Step("Get invitation code text")
    public String getInvitationCodeText() {
        waitUtils.waitForVisible(INVITATION_CODE_TEXT, 10);
        return getText(INVITATION_CODE_TEXT);
    }

    @Step("Get amount text")
    public String getAmountText() {
        waitUtils.waitForVisible(AMOUNT_TEXT, 10);
        return getText(AMOUNT_TEXT);
    }

    @Step("Get registered users text")
    public String getRegisteredUsersText() {
        waitUtils.waitForVisible(REGISTERED_USERS_TEXT, 10);
        return getText(REGISTERED_USERS_TEXT);
    }

    public boolean isLoaded() {
        return waitUtils.isPresent(REFERRAL_CODE_TEXT, 15);
    }
}
