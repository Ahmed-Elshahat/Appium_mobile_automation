package com.urpay.tests.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.RegistrationFlow;
import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.helpers.RegistrationApiHelper.PoiType;
import com.urpay.pages.auth.RegistrationWizardPage;
import com.urpay.pages.dashboard.DashboardPage;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;

/**
 * Abstract base for the three tier registration tests (National / IQA / Visitor).
 *
 * Shared journey (in order):
 *   1. Seed Tahaqoq + Yakeen/Nafath simulators via API (BeforeClass)
 *   2. Tap Register on the landing screen → verify registration form loads
 *   3. Enter mobile + POI, tap Next → verify OTP screen
 *   4. Enter OTP → verify next screen
 *   5. [Visitor only] Enter date of birth → verify passcode screen
 *   6. Create passcode → verify confirm screen
 *   7. Confirm passcode → verify terms screen
 *   8. Accept Terms + Privacy Policy, submit → verify done/finish screen
 *   9. Tap Done → verify dashboard
 *
 * Each concrete subclass supplies:
 *   - {@link #poiType()}      — NAT / IQA / BOR
 *   - {@link #hasDobStep()}   — true only for BOR/Visitor
 */
public abstract class AbstractRegistrationTierTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractRegistrationTierTest.class);

    /** @return the POI type under test */
    protected abstract PoiType poiType();

    // Credentials generated during @BeforeClass (pure API — no driver needed at that point)
    private String seededMobile;
    private String seededPoi;

    // Flow + page created lazily in testRegisterButtonOpensRegistrationForm (driver available then)
    private RegistrationFlow flow;
    private RegistrationWizardPage wizardPage;

    // ══════════════════════════════════════════════════
    //  PRE-SUITE: SEED SIMULATORS (API only — no driver)
    // ══════════════════════════════════════════════════

    @BeforeClass(alwaysRun = true)
    public void seedSimulators() {
        RestAssured.useRelaxedHTTPSValidation(); // simulator uses internal CA not trusted by default JVM
        ConfigManager config = ConfigManager.getInstance();
        PoiType type = poiType();
        String tierKey = type.name().toLowerCase(); // "nat", "iqa", "bor"

        // Skip API seeding when pre-provisioned creds are configured (diagnostic / offline runs)
        if ("true".equalsIgnoreCase(config.get("registration.skipSeeding", "false"))) {
            seededMobile = config.get("registration.preseeded." + tierKey + ".mobile", "");
            seededPoi    = config.get("registration.preseeded." + tierKey + ".poi", "");
            if (!seededMobile.isEmpty() && !seededPoi.isEmpty()) {
                log.info("Skipping API seeding — using pre-seeded creds: mobile={}, poi={}", seededMobile, seededPoi);
                return;
            }
            log.warn("skipSeeding=true but no preseeded creds found for {} — falling through to API seeding", tierKey);
        }

        seededMobile = RegistrationApiHelper.generateMobileNumber();
        seededPoi    = RegistrationApiHelper.generatePoiNumber(type.prefix());

        log.info("=== Seeding simulators for {} registration ===", type.code());
        log.info("  mobile : {}", seededMobile);
        log.info("  poi    : {}", seededPoi);

        var tahaqoq = RegistrationApiHelper.seedTahaqoqInfo(seededPoi, seededMobile);
        log.info("Tahaqoq seed: status {}", tahaqoq.getStatusCode());

        var nafath = RegistrationApiHelper.seedNafathInfo(seededPoi);
        log.info("Nafath seed:  status {}", nafath.getStatusCode());

        if (type != PoiType.BOR) {
            var yakeen = RegistrationApiHelper.seedYakeenInfo(seededPoi);
            log.info("Yakeen seed:  status {}", yakeen.getStatusCode());
        }
    }

    // ══════════════════════════════════════════════════
    //  TEST 1 — TAP REGISTER ON LANDING SCREEN
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 1)
    @Story("Registration — tap Register button")
    @Description("Tap the Register button on the landing screen and verify the registration form loads")
    @Severity(SeverityLevel.BLOCKER)
    public void testRegisterButtonOpensRegistrationForm() {
        // Driver is now available — create flow with credentials seeded in @BeforeClass
        flow       = new RegistrationFlow(poiType(), seededMobile, seededPoi);
        wizardPage = new RegistrationWizardPage();
        flow.tapRegisterOnLanding();
        Assert.assertTrue(wizardPage.isRegistrationFormLoaded(),
                "Registration form should be visible after tapping Register on the landing screen");
    }

    // ══════════════════════════════════════════════════
    //  TEST 2 — ENTER CREDENTIALS AND PROCEED TO OTP
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 2,
            dependsOnMethods = "testRegisterButtonOpensRegistrationForm")
    @Story("Registration — enter mobile and ID")
    @Description("Enter the generated mobile number and POI, tap Next, verify OTP screen appears")
    @Severity(SeverityLevel.BLOCKER)
    public void testEnterCredentialsOpensOtpScreen() {
        flow.enterCredentialsAndNext();
        // OtpPage reuses the OTP field shared with login; wait for it here via WizardPage
        Assert.assertTrue(wizardPage.isDobScreenDisplayed(30)
                || isOtpScreenDisplayed()
                || wizardPage.isPasscodeScreenDisplayed(5),
                "OTP screen (or DOB/passcode for auto-submitted OTP) should appear after entering credentials");
    }

    // ══════════════════════════════════════════════════
    //  TEST 3 — ENTER OTP
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 3,
            dependsOnMethods = "testEnterCredentialsOpensOtpScreen")
    @Story("Registration — enter OTP")
    @Description("Enter the verification OTP and proceed to the Date of Birth screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterOtpProceeds() {
        String otp = ConfigManager.getInstance().get("registration.otp", "1234");
        flow.enterOtp(otp);
        Assert.assertTrue(wizardPage.isDobScreenDisplayed(25),
                "Date of Birth screen (Step 3/5) should appear after OTP for all tiers");
    }

    // ══════════════════════════════════════════════════
    //  TEST 4 — DATE OF BIRTH (ALL TIERS)
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 4,
            dependsOnMethods = "testEnterOtpProceeds")
    @Story("Registration — date of birth")
    @Description("Enter date of birth on the DOB screen (Step 3/5 — shown for all tiers)")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterDateOfBirthProceeds() {
        ConfigManager cfg = ConfigManager.getInstance();
        String month = cfg.get("registration.dob.month", "May");
        String day   = cfg.get("registration.dob.day",   "30");
        String year  = cfg.get("registration.dob.year",  "1999");
        flow.enterDateOfBirth(month, day, year);
        Assert.assertTrue(wizardPage.isPasscodeScreenDisplayed(25),
                "Create Passcode screen should appear after DOB entry");
    }

    // ══════════════════════════════════════════════════
    //  TEST 5 — CREATE PASSCODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 5,
            dependsOnMethods = "testEnterDateOfBirthProceeds")
    @Story("Registration — create passcode")
    @Description("Create a new passcode on the Create Passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePasscodeShowsConfirmScreen() {
        String passcode = ConfigManager.getInstance().get("registration.passcode", "2233");
        flow.createPasscode(passcode);
        Assert.assertTrue(wizardPage.isPasscodeScreenDisplayed(20),
                "Confirm Passcode screen should appear after creating the passcode");
    }

    // ══════════════════════════════════════════════════
    //  TEST 6 — CONFIRM PASSCODE
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 6,
            dependsOnMethods = "testCreatePasscodeShowsConfirmScreen")
    @Story("Registration — confirm passcode and reach finish screen")
    @Description("Confirm passcode, dismiss the Nafath verification screen, verify the Done/Finish screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfirmPasscodeProceedsToTerms() {
        String passcode = ConfigManager.getInstance().get("registration.passcode", "2233");
        flow.confirmPasscode(passcode);
        // After confirming passcode the app shows a Nafath verification screen ("You're all set!")
        // before the final Done screen — tap Next, then wait up to 90s for Nafath auto-verification.
        if (wizardPage.isNafathVerificationScreenDisplayed(15)) {
            log.info("Nafath verification screen detected — tapping Next (Nafath auto-verifies in ~30s)");
            wizardPage.tapNext();
        }
        Assert.assertTrue(wizardPage.isFinishScreenDisplayed(90),
                "Registration Done/Finish screen should appear after Nafath auto-verification");
    }

    // ══════════════════════════════════════════════════
    //  TEST 7 — TAP DONE → DASHBOARD
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 7,
            dependsOnMethods = "testConfirmPasscodeProceedsToTerms")
    @Story("Registration — complete and reach dashboard")
    @Description("Tap Done on the finish screen and verify the dashboard is displayed")
    @Severity(SeverityLevel.BLOCKER)
    public void testDoneButtonNavigatesToDashboard() {
        DashboardPage dashboard = flow.tapDoneAndWaitForDashboard();
        Assert.assertTrue(dashboard.isLoaded(),
                "Dashboard should be visible after completing " + poiType().code() + " registration");
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private boolean isOtpScreenDisplayed() {
        // OTP field testID — same across all flows (login + registration)
        return new com.urpay.pages.auth.OtpPage().isVisible(15);
    }
}
