package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.FamilyMissionFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.wallet.FamilyMissionPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Family Mission Test — single end-to-end flow:
 *   1. Parent login → Family Wallet → Kid Profile → Missions → Create New Mission → Verify success
 *   2. Restart app → Kid login → Missions → Complete Mission → Ask Reward
 *   3. Restart app → Parent login → Family Wallet → Kid Profile → Missions → Send Reward → Verify "Rewarded"
 *
 * Katalon source: Test Suites/Test Suite Collections/WMVSuites/FamilyMissionSuite
 *   - FamilyMission-Setup Test data for family mission
 *   - VerifyNewMissionAddedSuccessfully
 *   - VerifylatestMissionStatusbeforeCompleted
 *   - KidTCs/VerifythatKidAbletoCompleteMissionSuccessfully
 *   - VerifythatParentSendrewardAfterMissionCompleted
 *   - KidTCs/VerifyKidBalanceAfterbeingRewarded
 */
@Epic("Wallet & VAS")
@Feature("Family Mission")
public class FamilyMissionTest extends BaseTest {

    @Test(groups = {"wallet", "family-mission", "smoke"})
    @Story("Family Mission Complete Lifecycle")
    @Description("Parent creates mission → Kid completes mission → Parent sends reward → Verify 'Rewarded' status")
    @Severity(SeverityLevel.CRITICAL)
    public void testFamilyMissionFullFlow() {
        ConfigManager config = ConfigManager.getInstance();

        // ──────────────────────────────────────────────
        // PHASE 1: Parent creates a new mission
        // ──────────────────────────────────────────────
        loginAsParent(config);
        FamilyMissionFlow flow = new FamilyMissionFlow();

        FamilyMissionPage missionPage = navigateToMissionsAsParent(flow);
        createNewMission(flow, missionPage, config);
        verifyMissionCreated(missionPage);

        // ──────────────────────────────────────────────
        // PHASE 2: Kid completes the mission
        // ──────────────────────────────────────────────
        flow.logout();
        loginAsKid(config);
        flow = new FamilyMissionFlow();

        FamilyMissionPage kidMissionPage = navigateToMissionsAsKid(flow);
        completeMissionAsKid(flow, kidMissionPage);

        // ──────────────────────────────────────────────
        // PHASE 3: Parent sends reward
        // ──────────────────────────────────────────────
        flow.logout();
        loginAsParent(config);
        flow = new FamilyMissionFlow();

        FamilyMissionPage rewardPage = navigateToMissionsAsParent(flow);
        verifyMissionCompleted(rewardPage);
        sendReward(flow, rewardPage, config);
        verifyRewarded(flow, rewardPage);

        log.info("Family Mission full flow completed successfully");
    }

    // ══════════════════════════════════════════════════
    //  ALLURE STEP METHODS
    // ══════════════════════════════════════════════════

    @Step("Login as Parent")
    private DashboardPage loginAsParent(ConfigManager config) {
        DashboardPage dashboard = new LoginFlow().loginWith(
                config.get("familyMission.parent.mobileNumber"),
                config.get("familyMission.parent.id"),
                config.get("familyMission.parent.verificationCode", "1234"),
                config.get("familyMission.parent.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Parent dashboard should be visible after login");
        return dashboard;
    }

    @Step("Login as Kid")
    private DashboardPage loginAsKid(ConfigManager config) {
        DashboardPage dashboard = new LoginFlow().loginWith(
                config.get("familyMission.kid.mobileNumber"),
                config.get("familyMission.kid.id"),
                config.get("familyMission.kid.verificationCode", "1234"),
                config.get("familyMission.kid.passCode", "2233"));
        Assert.assertTrue(dashboard.isLoaded(), "Kid dashboard should be visible after login");
        return dashboard;
    }

    @Step("Navigate to Missions as Parent (Dashboard → Family Wallet → Kid Profile → Missions)")
    private FamilyMissionPage navigateToMissionsAsParent(FamilyMissionFlow flow) {
        FamilyMissionPage page = flow.navigateToMissionsAsParent();
        Assert.assertTrue(page.isMissionsPageLoaded(),
                "Missions page should be loaded after navigation");
        return page;
    }

    @Step("Create new mission with test data")
    private void createNewMission(FamilyMissionFlow flow, FamilyMissionPage page, ConfigManager config) {
        String missionName = config.get("familyMission.missionName", "mission")
                + System.currentTimeMillis() % 100000;
        String missionTarget = config.get("familyMission.missionTarget", "Target of the mission");
        String missionMoney = config.get("familyMission.missionMoney", "5");
        flow.createMission(page, missionName, missionTarget, missionMoney);
    }

    @Step("Verify mission created — 'Thank you' screen displayed")
    private void verifyMissionCreated(FamilyMissionPage page) {
        Assert.assertTrue(page.isThankYouVisible(),
                "Thank you screen should be visible after mission creation");
        page.tapDone();
        log.info("Mission created successfully");
    }

    @Step("Navigate to Missions as Kid (Dashboard → Missions)")
    private FamilyMissionPage navigateToMissionsAsKid(FamilyMissionFlow flow) {
        return flow.navigateToMissionsAsKid();
    }

    @Step("Kid completes mission and asks for reward")
    private void completeMissionAsKid(FamilyMissionFlow flow, FamilyMissionPage page) {
        flow.completeMissionAsKid(page);
        log.info("Kid has completed the mission");
    }

    @Step("Verify mission status is 'Completed'")
    private void verifyMissionCompleted(FamilyMissionPage page) {
        page.waitForMissionsList();
        String status = page.getMissionStatus();
        Assert.assertEquals(status, "Completed",
                "Mission status should be 'Completed'. Actual: " + status);
        log.info("Mission status verified: Completed");
    }

    @Step("Parent sends reward for completed mission")
    private void sendReward(FamilyMissionFlow flow, FamilyMissionPage page, ConfigManager config) {
        String verificationCode = config.get("familyMission.parent.verificationCode", "1234");
        flow.sendRewardAsParent(page, verificationCode);
    }

    @Step("Verify mission status is 'Rewarded'")
    private void verifyRewarded(FamilyMissionFlow flow, FamilyMissionPage page) {
        String status = flow.verifyRewardedStatus(page);
        Assert.assertEquals(status, "Rewarded",
                "Mission status should be 'Rewarded'. Actual: " + status);
        log.info("Mission reward verified: Rewarded");
    }
}
