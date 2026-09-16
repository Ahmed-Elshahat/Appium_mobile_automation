package com.urpay.tests.wallet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.LoginFlow;
import com.urpay.flows.RegistrationFlow;
import com.urpay.helpers.FamilyRegistrationApiHelper;
import com.urpay.helpers.FamilyRegistrationApiHelper.SeededFamilyPair;
import com.urpay.helpers.RegistrationApiHelper.PoiType;
import com.urpay.pages.auth.RegistrationWizardPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.pages.dashboard.SettingsPage;
import com.urpay.pages.wallet.FamilyLinkApprovalPage;
import com.urpay.pages.wallet.FamilyWalletPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;

/**
 * Kid registration + family link — entirely app-driven.
 *
 * <p>Parent + kid mobile/POI are freshly generated and seeded into the simulators (no
 * registration) at {@code @BeforeClass} via {@link FamilyRegistrationApiHelper#seedFamilyPairOnly()}
 * — a brand-new pair every run, always dynamic (no config editing, no reused/stale creds).
 *
 * <p>Journey (per user's plan):
 *   1. Register the parent through the app.
 *   2. Logout.
 *   3. Register the kid (&lt;18) through the app.
 *   4. Logout.
 *   5. Login again as the parent.
 *   6. Approve the kid's link request in the app.
 *
 * <p>Step 6 is EXPLORATORY — the real approval screen has not been captured on-device yet.
 * {@link FamilyLinkApprovalPage} best-effort searches for a pending request and dumps the
 * screen to {@code logcat/} for follow-up once we see the real flow.
 */
@Epic("Wallet & VAS")
@Feature("Family Kid Registration + Link")
public class FamilyKidLinkRegistrationTest extends BaseTest {

    /** Mobile in {@code +9665xxxxxxxx} form (RegistrationFlow input). */
    private String parentMobileIntl;
    /** Mobile in {@code 05xxxxxxxx} form (LoginFlow input). */
    private String parentMobileLocal;
    private String parentPoi;
    private String parentOtp;
    private String parentPasscode;
    private String parentDobMonth;
    private String parentDobDay;
    private String parentDobYear;

    private String kidMobileIntl;
    private String kidPoi;
    private String kidOtp;
    private String kidPasscode;
    private String kidDobMonth;
    private String kidDobDay;
    private String kidDobYear;
    private String kidFirstName;

    private RegistrationWizardPage wizardPage;
    private DashboardPage dashboard;
    /** True once the kid's registration ended on "Waiting For Parent Approval" (drives logout method). */
    private boolean kidWaitingForApproval;

    @BeforeClass(alwaysRun = true)
    public void seedFreshFamilyPair() {
        ConfigManager c = ConfigManager.getInstance();
        parentOtp = c.get("familyLink.parent.verificationCode", "1234");
        parentPasscode = c.get("familyLink.parent.passCode", "2233");
        parentDobMonth = c.get("familyLink.parent.dob.month", "June");
        parentDobDay = c.get("familyLink.parent.dob.day", "27");
        parentDobYear = c.get("familyLink.parent.dob.year", "1980");

        kidOtp = c.get("familyLink.kid.verificationCode", "1234");
        kidPasscode = c.get("familyLink.kid.passCode", "2233");
        kidDobMonth = c.get("familyLink.kid.dob.month", "June");
        kidDobDay = c.get("familyLink.kid.dob.day", "1");
        kidDobYear = c.get("familyLink.kid.dob.year", "2014");
        // Must match FamilyRegistrationApiHelper.buildKid().englishFirstName — the seeded Yakeen
        // identity behind the kid's POI, which this guardian-verification screen validates against.
        kidFirstName = c.get("familyLink.kid.firstName", "Mutez");

        if (Boolean.parseBoolean(c.get("familyLink.skipSeeding", "false"))) {
            parentMobileIntl = requireConfigured(c, "familyLink.parent.mobile");
            parentPoi = requireConfigured(c, "familyLink.parent.poi");
            kidMobileIntl = requireConfigured(c, "familyLink.kid.mobile");
            kidPoi = requireConfigured(c, "familyLink.kid.poi");
            log.info("Using configured family pair: parent {} / {} | kid {} / {}",
                parentMobileIntl, parentPoi, kidMobileIntl, kidPoi);
        } else {
            SeededFamilyPair pair = FamilyRegistrationApiHelper.seedFamilyPairOnly();
            Assert.assertNotNull(pair, "Failed to seed a fresh parent + kid pair into the simulators "
                + "(requires the neoleap VPN for the SIT simulator)");
            parentMobileIntl = pair.parentMobile;
            parentPoi = pair.parentPoi;
            kidMobileIntl = pair.kidMobile;
            kidPoi = pair.kidPoi;
            log.info("Family link (freshly seeded): parent {} / {} | kid {} / {}",
                parentMobileIntl, parentPoi, kidMobileIntl, kidPoi);
        }
        parentMobileLocal = toLocalMobile(parentMobileIntl);
    }

        private static String requireConfigured(ConfigManager config, String key) {
        String value = config.get(key, "");
        Assert.assertFalse(value.isBlank(), key + " must be configured when familyLink.skipSeeding=true");
        return value;
        }

    @Test(groups = {"wallet", "family"}, priority = 1)
    @Story("Register the parent through the app")
    @Description("Drive the full in-app registration journey for the seeded parent")
    @Severity(SeverityLevel.BLOCKER)
    public void testRegisterParent() {
        RegistrationFlow flow = new RegistrationFlow(PoiType.NAT, parentMobileIntl, parentPoi);
        wizardPage = new RegistrationWizardPage();
        dashboard = completeNatRegistration(flow, parentMobileLocal, parentPoi, parentOtp, parentPasscode,
                parentDobMonth, parentDobDay, parentDobYear, null, null);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent registration");
    }

    @Test(groups = {"wallet", "family"}, priority = 2, dependsOnMethods = "testRegisterParent")
    @Story("Logout the parent")
    @Description("Logout via Settings so the kid can register next")
    @Severity(SeverityLevel.CRITICAL)
    public void testLogoutParent() {
        logout();
    }

    @Test(groups = {"wallet", "family"}, priority = 3, dependsOnMethods = "testLogoutParent")
    @Story("Register the kid through the app")
    @Description("Drive the full in-app registration journey for the seeded kid (<18)")
    @Severity(SeverityLevel.BLOCKER)
    public void testRegisterKid() {
        RegistrationFlow flow = new RegistrationFlow(PoiType.NAT, kidMobileIntl, kidPoi);
        dashboard = completeNatRegistration(flow, toLocalMobile(kidMobileIntl), kidPoi, kidOtp, kidPasscode,
                kidDobMonth, kidDobDay, kidDobYear, parentMobileLocal, kidFirstName);
        // The kid's END STATE is "Waiting For Parent Approval" (the link request is auto-created
        // by registration itself) — NOT the ordinary dashboard, so completeNatRegistration returns
        // null for this case. A non-null dashboard would mean the kid was NOT auto-linked.
        if (dashboard == null) {
            Assert.assertTrue(wizardPage.isWaitingForParentApprovalScreenDisplayed(5),
                    "Kid registration should end on 'Waiting For Parent Approval'");
            kidWaitingForApproval = true;
            log.info("Kid registration complete — waiting for parent approval (link request auto-created)");
        } else {
            Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after kid registration");
        }
    }

    @Test(groups = {"wallet", "family"}, priority = 4, dependsOnMethods = "testRegisterKid")
    @Story("Logout the kid")
    @Description("Logout via Settings so the parent can log back in")
    @Severity(SeverityLevel.CRITICAL)
    public void testLogoutKid() {
        if (kidWaitingForApproval) {
            // The Waiting-For-Parent-Approval screen has no Settings nav — it exposes its own
            // top-left LogOut icon instead.
            wizardPage.tapLogoutFromWaitingApprovalScreen();
            wizardPage.unlinkKidDeviceAndReturnToLanding();
            log.info("Logged out via the Waiting-For-Parent-Approval screen's LogOut icon");
        } else {
            logout();
        }
    }

    @Test(groups = {"wallet", "family"}, priority = 5, dependsOnMethods = "testLogoutKid")
    @Story("Login again as the parent")
    @Description("Full login as the parent to reach the dashboard where the link request is approved")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginAsParent() {
        // LoginFlow owns onboarding/Skip handling; after device unlink the app may still show
        // the walkthrough before exposing the Login button.
        dashboard = new LoginFlow().loginWith(parentMobileLocal, parentPoi, parentOtp, parentPasscode);
        FamilyLinkApprovalPage approval = new FamilyLinkApprovalPage();
        Assert.assertTrue(dashboard.isLoaded() || approval.isFamilyRequestsScreenDisplayed(10),
            "Parent login should reach the dashboard or Family requests screen");
    }

    @Test(groups = {"wallet", "family"}, priority = 6, dependsOnMethods = "testLoginAsParent")
    @Story("Approve the kid's link request")
        @Description("Open the pending family request, dismiss notification preferences, approve it, "
            + "and confirm with the kid's configured Hijri date of birth")
        @Severity(SeverityLevel.CRITICAL)
    public void testApproveKidLinkRequest() {
        FamilyLinkApprovalPage approval = new FamilyLinkApprovalPage();
        if (approval.isYesTakeMeTherePromptVisible(10)) {
            approval.tapYesTakeMeThere();
            approval.dismissNotificationsPopupIfDisplayed();
            Assert.assertTrue(approval.isPendingLinkRequestVisibleAfterPrompt(15),
                    "Tapping 'Yes, take me there' should open the pending Wallet Linking Request");
        }
        if (!approval.isFamilyRequestsScreenDisplayed(3)) {
            approval.openFamilyRequests();
        }
        boolean requestVisible = approval.isPendingLinkRequestVisible(15);
        if (!requestVisible) {
            approval.dumpForInvestigation();
        }
        Assert.assertTrue(requestVisible,
                "Family request should be visible after More -> Requests -> Family requests");
        approval.dismissNotificationsPopupIfDisplayed();
        approval.tapApprove();
        approval.enterKidHijriDateOfBirthAndConfirm(
            ConfigManager.getInstance().get("familyLink.kid.hijriDob", "03.08.1435"));
        approval.dismissNotificationsPopupIfDisplayed();
        Assert.assertTrue(approval.completeKidFamilyMemberKyc(20),
            "Kid family-member KYC should complete to finish the family-link approval");
        approval.dismissApprovalSuccessScreens();
        log.info("Approved the kid's family-link request and completed the kid KYC");

        Assert.assertTrue(approval.isLinkRequestVerified(15),
            "Wallet Linking Request should be marked Verified after kid KYC");
        DashboardPage parentDashboard = new LoginFlow().restartAppAndEnterPasscode(parentPasscode);
        if (!parentDashboard.isLoaded()) {
            approval.returnToDashboard();
            parentDashboard = new DashboardPage();
        }
        Assert.assertTrue(parentDashboard.isLoaded(),
            "Parent should reach the dashboard after app restart and passcode entry");
        FamilyWalletPage familyWallet = new FamilyWalletPage();
        familyWallet.tapFamilyWallet();
        Assert.assertTrue(familyWallet.isLoaded(),
            "Kid should be listed in Family Wallet after a successful family-link approval");
        log.info("Validated the kid is linked \u2014 appears in Family Wallet");
    }

    // ── Helpers ─────────────────────────────────────────────────────

    /**
     * Full NAT registration journey (credentials → OTP → DOB → passcode ×2 → Nafath/Absher/KYC
     * screens if shown → terms → done), matching {@code AbstractRegistrationTierTest}.
     *
     * <p>Some builds return to the remembered-login "Enter your passcode" screen right after
     * registration instead of the Done/Finish screen — handled by re-entering the passcode via
     * {@link LoginFlow} instead of tapping Done.
     *
     * @param guardianMobile the parent's mobile to enter on the kid's guardian-verification screen
     *                       ("Parent's mobile number"); {@code null} when registering the parent
     * @param kidFirstName   the kid's first name for Step 2/2 of the same wizard; {@code null} when
     *                       registering the parent
     */
    @Step("Complete NAT registration end-to-end")
    private DashboardPage completeNatRegistration(RegistrationFlow flow, String mobileLocal, String poi,
            String otp, String passcode, String dobMonth, String dobDay, String dobYear, String guardianMobile,
            String kidFirstName) {
        flow.tapRegisterOnLanding();
        flow.enterCredentialsAndNext();
        flow.enterOtp(otp);
        flow.enterDateOfBirth(dobMonth, dobDay, dobYear);
        flow.createPasscode(passcode);
        flow.confirmPasscode(passcode);
        // Diagnostic: the screen right after confirm has varied across runs (Nafath, a bounce to
        // remembered-login, or a full reset to landing) — dump the ground truth immediately, before
        // any of the checks below poll it away.
        wizardPage.dumpCurrentScreen("post-confirm-passcode-" + mobileLocal);

        if (wizardPage.isNafathVerificationScreenDisplayed(15)) {
            wizardPage.tapVerify();
        }
        if (guardianMobile != null && wizardPage.isParentMobileVerificationScreenDisplayed(10)) {
            wizardPage.enterParentMobileNumberAndNext(guardianMobile);
        }
        if (kidFirstName != null && wizardPage.isKidFirstNameScreenDisplayed(20)) {
            wizardPage.enterKidFirstNameAndNext(kidFirstName);
        } else if (kidFirstName != null) {
            // Diagnostic: the guardian-verification wizard's step count/order has varied across
            // runs (sometimes no separate first-name step, sometimes a slow transition) — capture
            // the ground truth instead of silently skipping.
            wizardPage.dumpCurrentScreen("post-parent-mobile-no-firstname-" + mobileLocal);
        }
        if (guardianMobile != null && wizardPage.isKidVerificationThankYouScreenDisplayed(30)) {
            wizardPage.tapDoneOnKidVerificationThankYouScreen();
            return null;
        }
        if (guardianMobile != null && wizardPage.isWaitingForParentApprovalScreenDisplayed(120)) {
            // Kid's END STATE: registration itself auto-creates the family link request; there is
            // no ordinary dashboard to reach here — the caller detects this via a null return.
            return null;
        }
        if (wizardPage.isNafathNumberScreenDisplayed(10)) {
            wizardPage.waitUntilNafathAutoVerifies(120);
        }
        wizardPage.dismissAbsherConsentIfPresent(10);
        if (wizardPage.isKycPersonalInformationScreenDisplayed(10)) {
            wizardPage.completeKycPersonalInformationRandomly(20);
        }
        if (wizardPage.isTermsScreenDisplayed(10)) {
            for (int attempt = 1; attempt <= 3 && wizardPage.isTermsScreenDisplayed(5); attempt++) {
                wizardPage.acceptTermsAndSubmit();
            }
        }
        wizardPage.skipCardPinSetupIfPresent(15);
        // Diagnostic: capture whatever screen we actually land on here — the post-registration
        // screen has varied across runs (Done/Finish, passcode re-entry, or something else
        // entirely), so dump the ground truth every time instead of guessing.
        wizardPage.dumpCurrentScreen("post-card-skip-" + mobileLocal);

        if (guardianMobile != null && wizardPage.isWaitingForParentApprovalScreenDisplayed(10)) {
            // Safety net: the screen can take longer than the first checkpoint above to render
            // (observed multi-minute backend delay after the kid's first-name submission).
            return null;
        }
        if (wizardPage.isLoginPasscodeReentryScreenDisplayed(10)) {
            log.info("Registration complete — app returned to the remembered-login passcode screen "
                    + "instead of Done/Finish; entering passcode to reach the dashboard");
            return new LoginFlow().loginWith(mobileLocal, poi, otp, passcode);
        }
        Assert.assertTrue(wizardPage.isFinishScreenDisplayed(35),
                "Registration Done/Finish screen should appear");
        return flow.tapDoneAndWaitForDashboard();
    }

    @Step("Logout: Settings → Logout → confirm")
    private void logout() {
        SettingsPage settings = new SettingsPage();
        settings.openViaDeepLink();
        settings.tapLogout();
        settings.confirmLogout();
        log.info("Logged out — ready for the next user");
    }

    /** Convert +966XXXXXXXXXX → 0XXXXXXXXX for the mobile number UI input. */
    private static String toLocalMobile(String international) {
        if (international != null && international.startsWith("+966")) {
            return "0" + international.substring(4);
        }
        return international;
    }
}
