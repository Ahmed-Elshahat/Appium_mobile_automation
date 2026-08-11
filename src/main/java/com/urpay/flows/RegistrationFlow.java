package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.helpers.RegistrationApiHelper.PoiType;
import com.urpay.pages.auth.LandingPage;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.auth.RegistrationWizardPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Registration Flow — drives the full in-app registration journey for any POI type
 * (National / IQA / Visitor).
 *
 * Prerequisites handled here:
 *   1. Generate a fresh random mobile + POI number.
 *   2. Seed the Tahaqoq (ID-verification) simulator.
 *   3. Seed the Yakeen/Nafath (national-identity) simulator (NAT/IQA only; BOR skips).
 *
 * UI journey:
 *   Landing → Register → enter mobile + POI → OTP → [BOR: DOB] →
 *   Create Passcode → Confirm Passcode → Accept Terms + Privacy → Done → Dashboard.
 *
 * Caller pattern:
 * <pre>
 *   RegistrationFlow flow = new RegistrationFlow(PoiType.NAT);
 *   flow.seedSimulators();         // @BeforeClass or first @Test
 *   DashboardPage dash = flow.completeRegistration();
 * </pre>
 *
 * Returned {@link RegisteredUser} carries the generated mobile + POI so the test
 * can assert them or reuse them for a follow-up login.
 */
public class RegistrationFlow {

    private static final Logger log = LoggerFactory.getLogger(RegistrationFlow.class);

    private final PoiType poiType;
    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final LandingPage landingPage;
    private final RegistrationWizardPage wizardPage;
    private final OtpPage otpPage;

    /** Mobile number generated for this run (format: +966520XXXXXX). */
    private String generatedMobile;
    /** POI number generated for this run (10-digit, Luhn-valid). */
    private String generatedPoi;

    // Onboarding skippable elements and the landing-ready marker (mirrors LoginFlow / InvitationCodeFlow)
    private static final By SKIP_BTN = AppiumBy.xpath(
            "//*[@text='Skip' or @text='SKIP' or @label='Skip']");

    private static final By LANDING_READY = AppiumBy.xpath(
            "//*[@content-desc='testID-secondary-login-main'"
            + " or @content-desc='testID-primary-register-main'"
            + " or @content-desc='testID-primary--main' or @text='Register']");

    private static final By DASHBOARD_MARKER = AppiumBy.xpath(
            "//*[@content-desc='testID-master-amount-main'"
            + " or @content-desc='testID-DashboardHome']");

    public RegistrationFlow(PoiType poiType) {
        this.poiType = poiType;
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.landingPage = new LandingPage();
        this.wizardPage = new RegistrationWizardPage();
        this.otpPage = new OtpPage();
    }

    /** Constructor that accepts pre-generated credentials (used when seeding happened before driver init). */
    public RegistrationFlow(PoiType poiType, String preGeneratedMobile, String preGeneratedPoi) {
        this(poiType);
        this.generatedMobile = preGeneratedMobile;
        this.generatedPoi    = preGeneratedPoi;
    }

    // ── Public API ─────────────────────────────────────────────────

    /** Return the mobile number that was generated and used for this run. */
    public String getGeneratedMobile() {
        return generatedMobile;
    }

    /** Return the POI number that was generated and used for this run. */
    public String getGeneratedPoi() {
        return generatedPoi;
    }

    /**
     * Generate credentials and seed both simulators.
     * Must be called before {@link #completeRegistration()}.
     *
     * @return {@code true} if seeding succeeded (non-2xx responses are logged as warnings)
     */
    @Step("Generate credentials and seed simulators for {poiType} registration")
    public boolean seedSimulators() {
        generatedMobile = RegistrationApiHelper.generateMobileNumber();
        generatedPoi    = RegistrationApiHelper.generatePoiNumber(poiType.prefix());

        log.info("=== Seeding simulators for {} registration ===", poiType.code());
        log.info("  mobile : {}", generatedMobile);
        log.info("  poi    : {}", generatedPoi);

        // Tahaqoq (ID-verification) simulator — required for all POI types
        var tahaqoqResp = RegistrationApiHelper.seedTahaqoqInfo(generatedPoi, generatedMobile);
        log.info("Tahaqoq seed: status {}", tahaqoqResp.getStatusCode());

        // Yakeen/Nafath (national-identity) simulator — NAT and IQA only
        if (poiType != PoiType.BOR) {
            var yakeenResp = RegistrationApiHelper.seedYakeenInfo(generatedPoi);
            log.info("Yakeen seed: status {}", yakeenResp.getStatusCode());
        }

        return true;
    }

    /**
     * Tap the Register button on the landing screen.
     * Handles onboarding skip screens that may appear before the landing page.
     */
    @Step("Navigate to landing screen and tap Register")
    public void tapRegisterOnLanding() {
        skipOnboardingToLanding();
        landingPage.clickRegister();
        log.info("Tapped Register on landing screen");
    }

    /**
     * Enter mobile + POI credentials and tap Next on the registration form.
     * Uses the credentials generated by {@link #seedSimulators()}.
     * Mobile is converted from +966XXXXXXXXXX to local 0XXXXXXXXX format for the UI field.
     */
    @Step("Enter registration credentials (mobile + POI) and tap Next")
    public void enterCredentialsAndNext() {
        String uiMobile = toLocalMobile(generatedMobile);
        log.info("Entering mobile '{}' and POI '{}' for {} registration", uiMobile, generatedPoi, poiType.code());
        wizardPage.enterMobileNumber(uiMobile);
        wizardPage.enterIdNumber(generatedPoi);
        hideKeyboard();
        // Terms + Privacy checkboxes sit on the same credentials form — accept them if present
        wizardPage.acceptTermsAndPrivacyIfPresent();
        dumpPageSourceLocally("registration-credentials-screen");
        wizardPage.tapNext();
        boolean advanced = otpPage.isVisible(5)
                || wizardPage.isDobScreenDisplayed(3)
                || wizardPage.isPasscodeScreenDisplayed(3);
        if (!advanced && wizardPage.isRegistrationFormLoaded(5)) {
            log.warn("Registration form still visible after first Next tap — retrying submit once");
            wizardPage.tapNext();
        }
    }

    /** Dismiss the software keyboard if visible — best-effort. */
    private void hideKeyboard() {
        try {
            ((io.appium.java_client.HidesKeyboard) driver).hideKeyboard();
            log.info("Keyboard hidden");
        } catch (Exception e) {
            log.debug("hideKeyboard no-op: {}", e.getMessage());
        }
    }

    /** Write driver.getPageSource() to logcat/ for local inspection. */
    private void dumpPageSourceLocally(String tag) {
        try {
            String xml = driver.getPageSource();
            String path = "logcat/" + tag + ".xml";
            new java.io.File("logcat").mkdirs();
            java.nio.file.Files.writeString(java.nio.file.Path.of(path), xml);
            log.info("Page source dumped to {}", path);
        } catch (Exception e) {
            log.warn("Page source dump failed: {}", e.getMessage());
        }
    }

    /** Enter the OTP on the OTP screen. */
    @Step("Enter OTP: {otp}")
    public void enterOtp(String otp) {
        otpPage.enterOtp(otp);
        log.info("OTP '{}' entered", otp);
    }

    /**
     * Enter date of birth on the DOB screen (Step 3/5 — shown for ALL tiers).
     * Drives the native Android date spinner.
     */
    @Step("Enter date of birth: {month} {day} {year}")
    public void enterDateOfBirth(String month, String day, String year) {
        wizardPage.enterDateOfBirth(month, day, year);
        wizardPage.tapDobNext();
        log.info("DOB {}/{}/{} entered and Next tapped", month, day, year);
    }

    /** Set a new passcode on the Create Passcode screen. */
    @Step("Create passcode")
    public void createPasscode(String passcode) {
        wizardPage.enterNewPasscode(passcode);
    }

    /** Confirm the passcode on the Confirm Passcode screen. */
    @Step("Confirm passcode")
    public void confirmPasscode(String passcode) {
        wizardPage.confirmPasscode(passcode);
    }

    /** Accept Terms + Privacy Policy checkboxes and submit registration. */
    @Step("Accept Terms and Privacy Policy, submit registration")
    public void acceptTermsAndSubmit() {
        wizardPage.acceptTermsAndSubmit();
    }

    /** Tap Done on the finish screen to reach the dashboard. */
    @Step("Tap Done on registration finish screen")
    public DashboardPage tapDoneAndWaitForDashboard() {
        wizardPage.tapDone();
        waits.isPresent(DASHBOARD_MARKER, 30);
        return new DashboardPage();
    }

    /**
     * Run the complete registration journey end-to-end:
     * seed → landing → credentials → OTP → [DOB] → passcode × 2 → terms → done → dashboard.
     *
     * @param otp       the OTP to enter (usually "1234" in SIT)
     * @param passcode  the passcode to create (usually "2233")
     * @param dob       the DOB string for BOR/Visitor (e.g. "1994-01-22"); ignored for NAT/IQA
     * @return the dashboard page
     */
    @Step("Complete {poiType} registration end-to-end")
    public DashboardPage completeRegistration(String otp, String passcode,
                                              String dobMonth, String dobDay, String dobYear) {
        seedSimulators();
        tapRegisterOnLanding();
        enterCredentialsAndNext();
        enterOtp(otp);
        enterDateOfBirth(dobMonth, dobDay, dobYear);
        createPasscode(passcode);
        confirmPasscode(passcode);
        acceptTermsAndSubmit();
        return tapDoneAndWaitForDashboard();
    }

    // ── Private helpers ────────────────────────────────────────────

    /** Convert +966XXXXXXXXXX → 0XXXXXXXXX for the mobile number UI input. */
    private static String toLocalMobile(String international) {
        if (international != null && international.startsWith("+966")) {
            return "0" + international.substring(4);
        }
        return international;
    }

    /** Dismiss onboarding walkthrough screens to reach the landing page. */
    private void skipOnboardingToLanding() {
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        for (int i = 0; i < 10; i++) {
            if (!waits.findQuick(LANDING_READY, 2).isEmpty()) {
                break;
            }
            var skips = waits.findQuick(SKIP_BTN, 1);
            if (!skips.isEmpty()) {
                try {
                    skips.get(0).click();
                    log.info("Tapped Skip on onboarding screen (iteration {})", i);
                } catch (Exception ignored) {
                }
            }
        }
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(10));
        log.info("Reached landing screen");
    }

    // ── Immutable credential holder ────────────────────────────────

    /** Credentials generated during this registration run. */
    public static final class RegisteredUser {
        public final String mobile;
        public final String poi;
        public final String poiType;
        public final String passcode;

        public RegisteredUser(String mobile, String poi, String poiType, String passcode) {
            this.mobile   = mobile;
            this.poi      = poi;
            this.poiType  = poiType;
            this.passcode = passcode;
        }

        @Override
        public String toString() {
            return "RegisteredUser{mobile=" + mobile + ", poi=" + poi + ", poiType=" + poiType + "}";
        }
    }
}
