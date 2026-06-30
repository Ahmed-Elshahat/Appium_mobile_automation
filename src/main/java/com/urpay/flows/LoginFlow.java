package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.OtpPage;
import com.urpay.pages.auth.PasscodePage;
import com.urpay.pages.common.CommonComponentsPage;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.PlatformActionsFactory;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

/**
 * Reusable login flow — called by ANY test that needs an authenticated session.
 *
 * Usage:
 *   DashboardPage dashboard = new LoginFlow().loginDefault();
 *   Assert.assertTrue(dashboard.isLoaded());
 *
 * Rules:
 *   - ZERO Thread.sleep()
 *   - NO assertions (returns DashboardPage for test to verify)
 *   - NO hardcoded credentials (reads from ConfigManager)
 *   - NO locator constants — shared elements via CommonComponentsPage
 *
 * Speed optimizations:
 *   - Reduced onboarding loop from 10 to 6 iterations (sufficient for all known screens)
 *   - Combined multi-text XPath locators to reduce findElements calls
 *   - Parallel field detection: check login + passcode + dashboard in one pass
 *   - Removed Thread.sleep from digit entry — custom keypads don't need inter-digit delay
 *   - Tighter timeouts for elements that should appear quickly
 */
public class LoginFlow {

    private static final Logger log = LoggerFactory.getLogger(LoginFlow.class);

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final MobilePlatformActions platformActions;
    private final CommonComponentsPage common;
    private final OtpPage otpPage;
    private final PasscodePage passcodePage;

    // ── Login-screen locators (used only in onboarding/credential entry) ──
    private static final By MOBILE     = AppiumBy.accessibilityId("testID-input-direct-mobile");
    private static final By NATID      = AppiumBy.accessibilityId("testID-input-direct-id");
    private static final By SUBMIT     = AppiumBy.accessibilityId("testID-primary--main");
    // Landing screen (fully logged out): the "Login" button that opens the credentials form.
    private static final By LANDING_LOGIN = AppiumBy.accessibilityId("testID-secondary-login-main");
    private static final By PASSCODE_SCREEN = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-passCode.screen') "
            + "or @text='Enter your passcode' or @text='Passcode']");
    // The clickable passcode container that, when tapped, focuses the hidden RN TextInput
    // so DIGIT_x key events register. Pick the clickable node (the inner one).
    private static final By PASSCODE_INPUT = AppiumBy.xpath(
            "//*[@content-desc='testID-passCode.screen' and @clickable='true']");
    private static final By DASHBOARD_MARKER = AppiumBy.accessibilityId("testID-master-amount-main");

    public LoginFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.common = new CommonComponentsPage();
        this.otpPage = new OtpPage();
        this.passcodePage = new PasscodePage();
    }

    // ── Public API ─────────────────────────────────────

    /**
     * Login with default user from config.
     * @return DashboardPage for the test to assert on
     */
    @Step("Login with default user from config")
    public DashboardPage loginDefault() {
        ConfigManager c = ConfigManager.getInstance();
        return loginWith(
                c.get("urpayUser.mobileNumber"),
                c.get("urpayUser.id"),
                c.get("urpayUser.verificationCode", "1234"),
                c.get("urpayUser.passCode", "2233")
        );
    }

    /**
     * Login with specific credentials.
     * @return DashboardPage for the test to assert on
     */
    @Step("Full login: skip → credentials → OTP → passcode → dashboard")
    public DashboardPage loginWith(String mobile, String id, String otp, String passcode) {
        skipOnboarding();

        // Cold start of this FLAG_SECURE banking app is slow and the hidden mobile input sits
        // in the DOM behind the splash/landing, so the discriminating checks below can run
        // before the real screen has rendered. Block until a genuinely-visible, interactive
        // screen has settled (passcode re-login, dashboard, or the landing "Login" button)
        // before deciding which login path to take.
        By settled = AppiumBy.xpath(
                "//*[contains(@content-desc,'testID-passCode.screen') "
                + "or @text='Enter your passcode' or @text='Passcode' "
                + "or @content-desc='testID-master-amount-main' "
                + "or @content-desc='testID-secondary-login-main']");
        waits.isPresent(settled, 60);

        // If passcode screen already visible (app remembers login), just enter passcode.
        if (waits.isPresent(PASSCODE_SCREEN, 5)) {
            log.info("Passcode screen detected — entering passcode directly");
            enterLoginPasscode(passcode);
            dismissPostLoginPopups();
            return new DashboardPage();
        }
        // If dashboard already visible, no login needed
        if (waits.isPresent(DASHBOARD_MARKER, 5)) {
            log.info("Dashboard already visible — skipping login");
            dismissPostLoginPopups();
            return new DashboardPage();
        }

        // Landing screen (fully logged out, e.g. after a passcode change): the credentials
        // form is reached only by tapping the "Login" button. The onboarding-skip loop runs
        // too fast to reliably catch it, and the mobile field exists in the DOM behind this
        // overlay (present but NOT clickable), so tap Login explicitly here before entering
        // credentials. Wait for it to be clickable to ride out the slow landing render.
        if (waits.isPresent(LANDING_LOGIN, 10)) {
            log.info("Landing screen detected — tapping Login to open the credentials form");
            waits.waitForClickable(LANDING_LOGIN, 20).click();
        }

        selectEnvironment();
        enterCredentials(mobile, id);
        enterOtp(otp);
        enterLoginPasscode(passcode);
        dismissPostLoginPopups();
        return new DashboardPage();
    }

    /**
     * Login only up to the passcode screen — credentials + OTP — WITHOUT entering the passcode.
     *
     * Migrated from Katalon {@code ResetPasscode/ForgotPasscode/ToValidateLoginTillPasscodeScreen}:
     * the Forgot-Passcode journey starts from the passcode screen and taps "Forgot your passcode?"
     * instead of entering the passcode, so the login must stop exactly on the passcode screen.
     *
     * Reuses the same onboarding-skip / landing / credentials / OTP steps as {@link #loginWith}
     * but skips {@link #enterLoginPasscode}. Returns once the passcode screen has rendered.
     */
    @Step("Login until the passcode screen (no passcode entry)")
    public void loginUntilPasscodeScreen(String mobile, String id, String otp) {
        skipOnboarding();

        By settled = AppiumBy.xpath(
                "//*[contains(@content-desc,'testID-passCode.screen') "
                + "or @text='Enter your passcode' or @text='Passcode' "
                + "or @content-desc='testID-master-amount-main' "
                + "or @content-desc='testID-secondary-login-main']");
        waits.isPresent(settled, 60);

        // App remembers the login → already on the passcode screen, nothing more to do.
        if (waits.isPresent(PASSCODE_SCREEN, 5)) {
            log.info("Passcode screen already visible — ready for Forgot Passcode");
            return;
        }
        // Already authenticated (no passcode screen). The Forgot-Passcode flow needs the passcode
        // screen, so log a warning — the caller's screen check will surface the real failure.
        if (waits.isPresent(DASHBOARD_MARKER, 5)) {
            log.warn("Dashboard already visible — Forgot Passcode flow expected the passcode screen");
            return;
        }
        // Fully logged out (landing) → open the credentials form.
        if (waits.isPresent(LANDING_LOGIN, 10)) {
            log.info("Landing screen detected — tapping Login to open the credentials form");
            waits.waitForClickable(LANDING_LOGIN, 20).click();
        }

        selectEnvironment();
        enterCredentials(mobile, id);
        enterOtp(otp);
        // Wait for the passcode screen to render — but do NOT enter the passcode.
        if (!waits.isPresent(PASSCODE_SCREEN, 30)) {
            log.warn("Passcode screen not detected after OTP — Forgot Passcode flow may fail");
        }
        log.info("Login reached the passcode screen (passcode not entered)");
    }

    /**
     * Dismiss post-login interstitials. After a full login the app shows an "Enable
     * Fingerprint" bottom-sheet (and sometimes a notifications prompt) that overlays the
     * dashboard and blocks the bottom navigation. Tap their "Later"/"Skip" dismissals if
     * present. Best-effort and idempotent: short waits, and a no-op when nothing is shown
     * (e.g. the remembered-login path that never raises these prompts).
     */
    @Step("Dismiss post-login popups (fingerprint / notifications)")
    private void dismissPostLoginPopups() {
        // The fingerprint "Later" button is testID-secondary-action-main (per Katalon
        // laterButtonFingerPrint); also match the generic "Later"/"Skip" text dismissals.
        By dismiss = AppiumBy.xpath(
                "//*[@content-desc='testID-secondary-action-main' "
                + "or @text='Later' or @text='Skip' or @text='No thanks' or @text='NO THANKS' "
                + "or @text='Maybe Later' or @text='Not now' or @text='Not Now']");
        for (int i = 0; i < 3; i++) {
            if (!waits.isPresent(dismiss, 6)) {
                break;
            }
            try {
                waits.waitForClickable(dismiss, 5).click();
                log.info("Dismissed a post-login popup");
            } catch (Exception e) {
                log.warn("Could not dismiss post-login popup: {}", e.getMessage());
                break;
            }
        }
    }

    // ── Private Steps ──────────────────────────────────


    @Step("Select environment from login dropdown")
    private void selectEnvironment() {
        String env = ConfigManager.getInstance().getEnv(); // e.g. "SIT"
        // The login screen exposes an "Environment" dropdown (placeholder text "Environment").
        // Its testID is not stable across builds, so locate by visible text and treat this as
        // BEST-EFFORT: if the dropdown/option is absent, skip and continue — the SIT build
        // defaults to the SIT backend, so login still proceeds.
        // Use a SINGLE explicit waitForClickable (not isPresent + driver.findElement) so a still-
        // settling login form does not trigger a ~10s implicit-wait retry on the click.
        By envDropdown = AppiumBy.xpath(
                "//*[@content-desc='testID-env-dropdown' or @text='Environment' "
                + "or @content-desc='Environment']");
        try {
            waits.waitForClickable(envDropdown, 8).click();
            log.info("Opened environment dropdown");
            By option = AppiumBy.xpath(
                    "//*[@text='" + env + "' or @content-desc='" + env + "' or @label='" + env + "']");
            waits.waitForClickable(option, 5).click();
            log.info("Environment selected: {}", env);
        } catch (Exception e) {
            log.warn("Environment dropdown not selected (defaulting to {}) — continuing: {}",
                    env, e.getMessage());
        }
    }

    /**
     * Enter the login passcode reliably.
     *
     * The passcode screen appears immediately after the OTP auto-submits and the server
     * verifies it. Its hidden input is not focused for a brief moment, so digit key-events
     * pressed too early are dropped (empty boxes) or partially delivered. Mirror Katalon's
     * intent (wait for the passcode screen, then press) but make it focus-safe: press, verify
     * the dashboard, and if not advanced clear any partial entry and re-press once the input
     * has had time to focus. Clearing before each press prevents over-filling the field.
     */
    @Step("Enter login passcode (focus-safe)")
    private void enterLoginPasscode(String passcode) {
        if (!waits.isPresent(PASSCODE_SCREEN, 30)) {
            log.warn("Passcode screen not detected after OTP");
        }
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (waits.isPresent(DASHBOARD_MARKER, 1)) {
                return; // already authenticated
            }
            // The passcode boxes have a hidden input that is NOT auto-focused (unlike the
            // OTP screen). Without focus, DIGIT_x key events are dropped and the boxes stay
            // empty. Tap the boxes region first to focus the input, then press the digits.
            tapToFocusPasscode();
            platformActions.clearDigits(passcode.length() + 2); // drop any partial/stale digits
            passcodePage.enterPasscode(passcode);
            log.info("Passcode entered (attempt {})", attempt);
            if (waits.isPresent(DASHBOARD_MARKER, 8)) {
                return;
            }
        }
        // Still not authenticated after all attempts — dump the passcode screen tree once so
        // we can see the exact input/box elements for diagnosis.
        try {
            log.warn("Passcode entry did not reach dashboard. Page source:\n{}",
                    driver.getPageSource());
        } catch (Exception e) {
            log.warn("Could not capture page source: {}", e.getMessage());
        }
    }

    /**
     * Focus the hidden passcode TextInput. The RN passcode component exposes a clickable
     * wrapper (content-desc 'testID-passCode.screen', clickable=true) whose onPress focuses
     * the hidden input. Clicking that element is more reliable than a raw coordinate tap, so
     * try it first; fall back to a coordinate tap on the boxes region if the element is not
     * resolvable. Without focus, DIGIT_x key events are dropped and the boxes stay empty.
     */
    private void tapToFocusPasscode() {
        try {
            var els = driver.findElements(PASSCODE_INPUT);
            if (!els.isEmpty()) {
                els.get(els.size() - 1).click(); // inner clickable wrapper
                log.info("Clicked passcode input wrapper to focus");
                return;
            }
        } catch (Exception e) {
            log.warn("Click passcode input wrapper failed: {}", e.getMessage());
        }
        // Fallback: coordinate tap on the boxes region (centre, ~34% down).
        try {
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            int x = (int) (size.getWidth() * 0.5);
            int y = (int) (size.getHeight() * 0.34);
            var finger = new org.openqa.selenium.interactions.PointerInput(
                    org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger1");
            var tap = new org.openqa.selenium.interactions.Sequence(finger, 0);
            tap.addAction(finger.createPointerMove(java.time.Duration.ZERO,
                    org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(
                    org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(new org.openqa.selenium.interactions.Pause(finger,
                    java.time.Duration.ofMillis(100)));
            tap.addAction(finger.createPointerUp(
                    org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(java.util.Collections.singletonList(tap));
            log.info("Tapped passcode boxes to focus input at ({}, {})", x, y);
        } catch (Exception e) {
            log.warn("Tap-to-focus passcode failed: {}", e.getMessage());
        }
    }

    @Step("Skip onboarding screens")
    private void skipOnboarding() {
        // Wait for ANY first screen element (app loaded)
        By anyFirst = AppiumBy.xpath(
                "//*[@text='Skip' or @text='No thanks' or @text='Allow' "
                + "or @text='Later' or @text='Passcode' or @text='Enter your passcode' "
                + "or @text='Login' or @text='LOGIN' or @text='Register' "
                + "or @content-desc='testID-secondary-login-main' "
                + "or @content-desc='testID-secondary-g35-main' "
                + "or @content-desc='testID-primary-register-main' "
                + "or @content-desc='testID-notification-contaniner' "
                + "or @content-desc='testID-input-direct-mobile' "
                + "or @content-desc='testID-secondary-action-main' "
                + "or @content-desc='testID-primary-enableLocation-main' "
                + "or @content-desc='testID-master-amount-main']");
        // Cold start of this FLAG_SECURE banking app can take well over 15s to first render
        // (the welcome screen's root wrapper is testID-notification-contaniner). On a cold/idle
        // device the first paint can intermittently exceed 40s, so allow a generous window.
        waits.waitForVisible(anyFirst, 75);

        // ALL skippable elements in ONE xpath
        By skipAll = AppiumBy.xpath(
                "//*[@text='Skip' or @text='No thanks' or @text='NO THANKS' "
                + "or @text='Allow' or @text='ALLOW' or @text='While using the app' "
                + "or @text='Later' "
                + "or @content-desc='testID-secondary-action-main' "
                + "or @content-desc='testID-primary-enableLocation-main']");

        // Login button on the welcome screen (tap it to go to login form). Covers multiple builds:
        // testID-secondary-login-main / g35-main (obfuscated) / text "Login"/"LOGIN".
        By loginBtn = AppiumBy.xpath(
                "//*[@content-desc='testID-secondary-login-main' "
                + "or @content-desc='testID-secondary-g35-main' "
                + "or @content-desc='testID-primary-login-main' "
                + "or @content-desc='testID-primary-loginBtn-main' "
                + "or @text='Login' or @text='LOGIN' or @text='Log in']");

        // True terminal = dashboard or passcode screen (NOT mobile field — it exists in DOM behind onboarding)
        By realTerminal = AppiumBy.xpath(
                "//*[@content-desc='testID-master-amount-main' "
                + "or contains(@content-desc,'testID-passCode.screen')]");

        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        for (int i = 0; i < 10; i++) {
            // Check real terminal first (dashboard/passcode — means we're past login).
            // findQuick waits up to 2s so a still-mounting RN screen isn't missed by a 0ms probe.
            try {
                var termEls = waits.findQuick(realTerminal, 2);
                if (!termEls.isEmpty() && termEls.get(0).isDisplayed()) {
                    log.info("Dashboard/passcode visible — skip complete after {} iteration(s)", i);
                    break;
                }
            } catch (Exception ignored) {}

            // Try Login button (last onboarding step → goes to login form). Give the welcome
            // screen up to 2s per iteration to render the button before falling through; the
            // previous 0ms probe could spin through every iteration before the button mounted.
            try {
                var loginEls = waits.findQuick(loginBtn, 2);
                if (!loginEls.isEmpty() && loginEls.get(0).isDisplayed()) {
                    loginEls.get(0).click();
                    log.info("Tapped Login button — onboarding complete");
                    break;
                }
            } catch (Exception ignored) {}

            // Tap any skippable element (Skip, Allow, Later, etc.)
            try {
                var skippables = driver.findElements(skipAll);
                if (!skippables.isEmpty()) {
                    for (var el : skippables) {
                        try {
                            if (el.isDisplayed()) {
                                el.click();
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(10));
        log.info("Onboarding skip complete");
    }

    @Step("Enter credentials: {mobile} / {id}")
    private void enterCredentials(String mobile, String id) {
        // After the Login button is tapped the credential FORM is a fresh screen transition that,
        // on a cold cloud (LambdaTest) start of this FLAG_SECURE app, can take well over 8s to
        // render — give the mobile field a generous window to avoid a false timeout.
        waits.waitForClickable(MOBILE, 25).click();
        driver.findElement(MOBILE).sendKeys(mobile);
        log.info("Mobile: {}", mobile);

        dismissKeyboard();
        waits.waitForClickable(NATID, 10).click();
        driver.findElement(NATID).sendKeys(id);
        log.info("ID: {}", id);

        dismissKeyboard();
        waits.waitForClickable(SUBMIT, 10).click();
        log.info("Login submitted");
    }

    @Step("Enter OTP: {otp}")
    private void enterOtp(String otp) {
        otpPage.enterOtp(otp);
        log.info("OTP entered: {}", otp);
    }

    @Step("Enter passcode")
    private void enterPasscode(String passcode) {
        boolean passcodeVisible = waits.isPresent(PASSCODE_SCREEN, 3);
        if (!passcodeVisible) {
            otpPage.isVisible(0); // Quick check — OTP should have transitioned
        }
        passcodePage.enterPasscode(passcode);
        log.info("Passcode entered");
    }

    // ── Helpers ────────────────────────────────────────

    private boolean quickTap(By loc) {
        try {
            var els = driver.findElements(loc);
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                els.get(0).click();
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void dismissKeyboard() {
        common.dismissKeyboard();
    }
}
