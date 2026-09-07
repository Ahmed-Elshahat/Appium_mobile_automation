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
 * registration) at {@code @BeforeClass} via
 * {@link FamilyRegistrationApiHelper#seedFamilyPairOnly()} — a brand-new pair every run, no
 * config editing needed.
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

    private RegistrationWizardPage wizardPage;
    private DashboardPage dashboard;

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

        SeededFamilyPair pair = FamilyRegistrationApiHelper.seedFamilyPairOnly();
        Assert.assertNotNull(pair, "Failed to seed a fresh parent + kid pair into the simulators "
                + "(requires the neoleap VPN for the SIT simulator)");
        parentMobileIntl = pair.parentMobile;
        parentMobileLocal = toLocalMobile(pair.parentMobile);
        parentPoi = pair.parentPoi;
        kidMobileIntl = pair.kidMobile;
        kidPoi = pair.kidPoi;

        log.info("Family link (freshly seeded): parent {} / {} | kid {} / {}",
                parentMobileIntl, parentPoi, kidMobileIntl, kidPoi);
    }

    @Test(groups = {"wallet", "family"}, priority = 1)
    @Story("Register the parent through the app")
    @Description("Drive the full in-app registration journey for the seeded parent")
    @Severity(SeverityLevel.BLOCKER)
    public void testRegisterParent() {
        RegistrationFlow flow = new RegistrationFlow(PoiType.NAT, parentMobileIntl, parentPoi);
        wizardPage = new RegistrationWizardPage();
        dashboard = completeNatRegistration(flow, parentOtp, parentPasscode,
                parentDobMonth, parentDobDay, parentDobYear);
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
        dashboard = completeNatRegistration(flow, kidOtp, kidPasscode, kidDobMonth, kidDobDay, kidDobYear);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after kid registration");
    }

    @Test(groups = {"wallet", "family"}, priority = 4, dependsOnMethods = "testRegisterKid")
    @Story("Logout the kid")
    @Description("Logout via Settings so the parent can log back in")
    @Severity(SeverityLevel.CRITICAL)
    public void testLogoutKid() {
        logout();
    }

    @Test(groups = {"wallet", "family"}, priority = 5, dependsOnMethods = "testLogoutKid")
    @Story("Login again as the parent")
    @Description("Full login as the parent to reach the dashboard where the link request is approved")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginAsParent() {
        dashboard = new LoginFlow().loginWith(parentMobileLocal, parentPoi, parentOtp, parentPasscode);
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be visible after parent re-login");
    }

    @Test(groups = {"wallet", "family"}, priority = 6, dependsOnMethods = "testLoginAsParent")
    @Story("Approve the kid's link request")
    @Description("EXPLORATORY: best-effort search for the pending family-link request; dumps the "
            + "screen to logcat/ for follow-up if the request isn't found by the current locators")
    @Severity(SeverityLevel.NORMAL)
    public void testApproveKidLinkRequest() {
        FamilyLinkApprovalPage approval = new FamilyLinkApprovalPage();
        if (approval.isPendingLinkRequestVisible(15)) {
            approval.openPendingLinkRequest();
            approval.tapApprove();
            log.info("Approved the kid's family-link request");
        } else {
            approval.dumpForInvestigation();
            log.warn("No pending family-link request found by the current locators — inspect "
                    + "logcat/family-link-approval-investigation.xml to locate the real approval UI");
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────

    /**
     * Full NAT registration journey (credentials → OTP → DOB → passcode ×2 → Nafath/Absher/KYC
     * screens if shown → terms → done), matching {@code AbstractRegistrationTierTest}.
     */
    @Step("Complete NAT registration end-to-end")
    private DashboardPage completeNatRegistration(RegistrationFlow flow, String otp, String passcode,
            String dobMonth, String dobDay, String dobYear) {
        flow.tapRegisterOnLanding();
        flow.enterCredentialsAndNext();
        flow.enterOtp(otp);
        flow.enterDateOfBirth(dobMonth, dobDay, dobYear);
        flow.createPasscode(passcode);
        flow.confirmPasscode(passcode);

        if (wizardPage.isNafathVerificationScreenDisplayed(15)) {
            wizardPage.tapNext();
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
        Assert.assertTrue(wizardPage.isFinishScreenDisplayed(45),
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
