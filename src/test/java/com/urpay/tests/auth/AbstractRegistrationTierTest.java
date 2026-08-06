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

    /** @return {@code true} if the DOB screen appears for this tier (BOR/Visitor only) */
    protected boolean hasDobStep() {
        return false;
    }

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
    @Description("Enter the verification OTP and proceed to the next registration step")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterOtpProceeds() {
        String otp = ConfigManager.getInstance().get("registration.otp", "1234");
        flow.enterOtp(otp);
        if (hasDobStep()) {
            Assert.assertTrue(wizardPage.isDobScreenDisplayed(20),
                    "Date of birth screen should appear after OTP for Visitor/BOR");
        } else {
            Assert.assertTrue(wizardPage.isPasscodeScreenDisplayed(25),
                    "Create Passcode screen should appear after OTP for NAT/IQA");
        }
    }

    // ══════════════════════════════════════════════════
    //  TEST 4 — DATE OF BIRTH (VISITOR ONLY)
    // ══════════════════════════════════════════════════

    @Test(groups = {"registration"}, priority = 4,
            dependsOnMethods = "testEnterOtpProceeds")
    @Story("Registration — date of birth (Visitor)")
    @Description("Enter date of birth on the DOB validation screen (Visitor/BOR tier only)")
    @Severity(SeverityLevel.CRITICAL)
    public void testEnterDateOfBirthProceeds() {
        if (!hasDobStep()) {
            return; // no DOB screen for NAT / IQA
        }
        String dob = ConfigManager.getInstance().get("registration.visitor.dob", "1994-01-22");
        flow.enterDateOfBirth(dob);
        Assert.assertTrue(wizardPage.isPasscodeScreenDisplayed(25),
                "Create Passcode screen should appear after DOB validation for Visitor/BOR");
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
    @Story("Registration — confirm passcode")
    @Description("Confirm the passcode on the Confirm Passcode screen")
    @Severity(SeverityLevel.CRITICAL)
    public void testConfirmPasscodeProceedsToTerms() {
        String passcode = ConfigManager.getInstance().get("registration.passcode", "2233");
        flow.confirmPasscode(passcode);
        // After confirming passcode the app may show a terms screen (some builds) or go straight
        // to the finish screen — accept terms if present, then wait for finish/done.
        flow.acceptTermsAndSubmit();
        Assert.assertTrue(wizardPage.isFinishScreenDisplayed(30),
                "Registration finish/done screen should appear after confirming passcode");
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
