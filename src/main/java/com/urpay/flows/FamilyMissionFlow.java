package com.urpay.flows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.DriverFactory;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.FamilyMissionPage;
import com.urpay.pages.wallet.FamilyWalletPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Family Mission flow — orchestrates the complete family mission journey.
 *
 * Parent flow:  Dashboard → Family Wallet → Kid Profile → Missions → Create Mission
 * Kid flow:     Dashboard → Missions → Complete Mission → Ask Reward
 * Parent reward: Dashboard → Family Wallet → Kid Profile → Missions → Send Reward
 *
 * Katalon source: Test Suites/Test Suite Collections/WMVSuites/FamilyMissionSuite
 *   Scripts/Wallet_VAS/FamilyMission/
 */
public class FamilyMissionFlow {

    private static final Logger log = LoggerFactory.getLogger(FamilyMissionFlow.class);
    private final MobilePlatformActions platformActions;

    public FamilyMissionFlow() {
        AppiumDriver driver = DriverFactory.getInstance().getDriver();
        this.platformActions = PlatformActionsFactory.create(driver);
    }

    // ══════════════════════════════════════════════════
    //  PARENT: NAVIGATE TO MISSIONS
    // ══════════════════════════════════════════════════

    @Step("Navigate to Family Missions via Dashboard → Family Wallet → Kid Profile → Missions")
    public FamilyMissionPage navigateToMissionsAsParent() {
        FamilyWalletPage familyWallet = new FamilyWalletPage();

        familyWallet.tapFamilyWallet();
        familyWallet.waitUntilLoaded();
        log.info("Family Wallet screen loaded");

        familyWallet.tapFirstFamilyMember();
        FamilyMissionPage missionPage = new FamilyMissionPage();
        missionPage.tapMissions();
        log.info("Missions section opened");

        missionPage.tapGetStartedIfPresent();
        return missionPage;
    }

    // ══════════════════════════════════════════════════
    //  PARENT: CREATE NEW MISSION
    // ══════════════════════════════════════════════════

    @Step("Create new mission: name={name}, target={target}, amount={amount}")
    public void createMission(FamilyMissionPage page, String name, String target, String amount) {
        page.tapAddNewMission();
        log.info("Adding new mission: {}", name);

        // Step 1: Category + Name + Target
        page.selectEntertainment();
        page.enterMissionName(name);
        page.enterMissionTarget(target);
        page.tapNext();
        log.info("Step 1 complete: category & name");

        // Step 2: Amount
        page.enterAmount(amount);
        page.tapSecondNext();
        log.info("Step 2 complete: amount = {}", amount);

        // Step 3: End date
        page.selectEndDate();
        page.tapPrimaryNext();
        log.info("Step 3 complete: end date set");

        // Step 4: Confirmation
        page.scrollAndConfirm();
        log.info("Mission confirmed");
    }

    // ══════════════════════════════════════════════════
    //  KID: NAVIGATE TO MISSIONS
    // ══════════════════════════════════════════════════

    @Step("Navigate to Missions from Kid's dashboard")
    public FamilyMissionPage navigateToMissionsAsKid() {
        FamilyMissionPage missionPage = new FamilyMissionPage();
        missionPage.navigateToMissionsFromKidDashboard();
        log.info("Kid navigated to Missions section");
        return missionPage;
    }

    // ══════════════════════════════════════════════════
    //  KID: COMPLETE MISSION
    // ══════════════════════════════════════════════════

    @Step("Kid completes mission and asks for reward")
    public void completeMissionAsKid(FamilyMissionPage page) {
        page.waitForMissionsList();
        page.tapMissionNameKid();
        page.tapImDone();
        page.tapAskReward();
        log.info("Kid completed mission and asked for reward");
    }

    // ══════════════════════════════════════════════════
    //  PARENT: SEND REWARD
    // ══════════════════════════════════════════════════

    @Step("Parent sends reward for completed mission")
    public void sendRewardAsParent(FamilyMissionPage page, String verificationCode) {
        page.waitForMissionsList();
        page.tapMissionStatus();
        page.tapSendReward();
        page.tapValidateReward();

        // Enter passcode/OTP for reward validation
        enterVerificationCode(verificationCode);
        log.info("Reward sent successfully");
    }

    @Step("Verify mission shows 'Rewarded' status")
    public String verifyRewardedStatus(FamilyMissionPage page) {
        page.tapBack();
        page.tapClosedTab();
        String status = page.getRewardedStatus();
        log.info("Mission final status: {}", status);
        return status;
    }

    // ══════════════════════════════════════════════════    //  LOGOUT (switch user without restarting the app)
    // ═════════════════════════════════════════════════

    @Step("Logout: return to dashboard → More → Settings → Logout → confirm")
    public void logout() {
        new FamilyMissionPage().returnToDashboard();
        DashboardPage dashboard = new DashboardPage();
        dashboard.navigateToMore();
        SettingsPage settings = new SettingsPage();
        settings.openSettings();
        settings.tapLogout();
        settings.confirmLogout();
        log.info("Logged out — ready for a new user login");
    }

    // ═════════════════════════════════════════════════    //  HELPERS
    // ══════════════════════════════════════════════════

    private void enterVerificationCode(String code) {
        platformActions.enterDigits(code);
    }
}
