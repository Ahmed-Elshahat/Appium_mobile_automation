package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.helpers.FamilyRegistrationApiHelper;
import com.urpay.helpers.FamilyRegistrationApiHelper.LinkedPair;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.auth.RegistrationWizardPage;
import com.urpay.pages.wallet.FamilyLinkApprovalPage;
import com.urpay.pages.wallet.FamilyWalletPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Wallet & VAS")
@Feature("Parent Initiated Family Link")
public class ParentInitiatedFamilyLinkTest extends BaseTest {

    private String parentMobile;
    private String parentPoi;
    private String kidMobile;
    private String kidPoi;
    private String otp;
    private String passcode;
    private String kidHijriDob;

    @BeforeClass(alwaysRun = true)
    public void registerParentAndKidByApi() {
        ConfigManager config = ConfigManager.getInstance();
        otp = config.get("parentInitiatedFamilyLink.verificationCode", "1234");
        passcode = config.get("parentInitiatedFamilyLink.passCode", "2233");
        kidHijriDob = config.get("parentInitiatedFamilyLink.kid.hijriDob", "03.08.1435");

        if (Boolean.parseBoolean(config.get("parentInitiatedFamilyLink.useConfiguredPair", "false"))) {
            parentMobile = toLocalMobile(requireConfigured(config, "parentInitiatedFamilyLink.parent.mobile"));
            parentPoi = requireConfigured(config, "parentInitiatedFamilyLink.parent.poi");
            kidMobile = toLocalMobile(requireConfigured(config, "parentInitiatedFamilyLink.kid.mobile"));
            kidPoi = requireConfigured(config, "parentInitiatedFamilyLink.kid.poi");
            log.info("Using configured parent-initiated family link pair: parent {} / {} | kid {} / {}",
                    parentMobile, parentPoi, kidMobile, kidPoi);
        } else {
            LinkedPair pair = FamilyRegistrationApiHelper.provisionRegisteredFamilyPairForManualLink();
            Assert.assertNotNull(pair, "API setup should register a parent and kid without linking them");
            parentMobile = toLocalMobile(pair.parentMobile());
            parentPoi = pair.parentPoi();
            kidMobile = toLocalMobile(pair.kidMobile());
            kidPoi = pair.kidPoi();
            log.info("Parent-initiated family link pair: parent {} / {} | kid {} / {}",
                    pair.parentMobile(), parentPoi, pair.kidMobile(), kidPoi);
        }
    }

    @Test(groups = {"wallet", "family"}, priority = 1)
    @Story("Parent sends a family-link request to the kid")
    @Description("Login as the API-registered parent and send a family request to the API-registered kid by POI")
    @Severity(SeverityLevel.CRITICAL)
    public void testParentSendsFamilyRequestToKid() {
        DashboardPage dashboard = new LoginFlow().loginWith(parentMobile, parentPoi, otp, passcode);
        Assert.assertTrue(dashboard.isLoaded(), "Parent dashboard should be visible after login");
        FamilyWalletPage familyWallet = new FamilyWalletPage();
        familyWallet.tapFamilyWallet();
        Assert.assertTrue(familyWallet.sendFamilyRequestToKid(kidPoi, kidMobile, kidHijriDob),
                "Parent should send a family-link request to the kid by POI");
        logout();
    }

    @Test(groups = {"wallet", "family"}, priority = 2, dependsOnMethods = "testParentSendsFamilyRequestToKid")
    @Story("Kid completes the parent family-link request")
        @Description("Login as the API-registered kid, accept terms, open the pending family request, "
            + "complete required data, and validate the kid dashboard")
    @Severity(SeverityLevel.CRITICAL)
    public void testKidCompletesParentFamilyRequest() {
        DashboardPage dashboard = new LoginFlow().loginWith(kidMobile, kidPoi, otp, passcode);
        RegistrationWizardPage terms = new RegistrationWizardPage();
        terms.acceptTermsAndPrivacyIfPresent();
        terms.acceptStandalonePrivacyOrTermsIfPresent();
        FamilyLinkApprovalPage request = new FamilyLinkApprovalPage();
        boolean kidKycVisible = request.isKidKycFormDisplayed(10);
        Assert.assertTrue(kidKycVisible || dashboard.isLoaded() || request.isFamilyRequestsScreenDisplayed(10),
            "Kid login should reach dashboard, Family Requests, or kid KYC");
        if (kidKycVisible) {
            Assert.assertTrue(request.completeKidFamilyMemberKyc(20),
                "Kid should complete required personal information after login");
            DashboardPage kidDashboard = new LoginFlow().restartAppAndEnterPasscode(passcode);
            Assert.assertTrue(kidDashboard.isLoaded(),
                "Kid dashboard should be visible after completing required data");
            return;
        }
        if (request.isYesTakeMeTherePromptVisible(10)) {
            request.tapYesTakeMeThere();
            request.dismissNotificationsPopupIfDisplayed();
        }
        if (!request.isFamilyRequestsScreenDisplayed(3)) {
            request.openFamilyRequests();
        }
        Assert.assertTrue(request.isPendingLinkRequestVisible(15),
                "Kid should see the pending family-link request from the parent");
        request.dismissNotificationsPopupIfDisplayed();
        request.tapApprove();
        if (request.isKidHijriDobInputDisplayed(10)) {
            request.enterKidHijriDateOfBirthAndConfirm(kidHijriDob);
        }
        request.dismissNotificationsPopupIfDisplayed();
        Assert.assertTrue(request.completeKidFamilyMemberKyc(20),
                "Kid should complete all required family-link data");
        DashboardPage kidDashboard = new LoginFlow().restartAppAndEnterPasscode(passcode);
        Assert.assertTrue(kidDashboard.isLoaded(),
            "Kid dashboard should be visible after completing required data");
    }

    private void logout() {
        DashboardPage dashboard = new DashboardPage();
        dashboard.navigateToMore();
        SettingsPage settings = new SettingsPage();
        settings.openSettings();
        settings.tapLogout();
        settings.confirmLogout();
    }

    private static String toLocalMobile(String mobile) {
        return mobile.startsWith("+966") ? "0" + mobile.substring(4) : mobile;
    }

    private static String requireConfigured(ConfigManager config, String key) {
        String value = config.get(key, "");
        Assert.assertFalse(value.isBlank(), key + " must be configured");
        return value;
    }
}
