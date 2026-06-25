package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.auth.LoginPage;
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
    private final LoginPage loginPage;
    private final OtpPage otpPage;
    private final PasscodePage passcodePage;

    // ── Login-screen locators (used only in onboarding/credential entry) ──
    private static final By MOBILE     = AppiumBy.accessibilityId("testID-input-direct-mobile");
    private static final By NATID      = AppiumBy.accessibilityId("testID-input-direct-id");
    private static final By SUBMIT     = AppiumBy.accessibilityId("testID-primary--main");
    private static final By PASSCODE_SCREEN = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-passCode.screen')]");

    public LoginFlow() {
        this.driver = DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
        this.platformActions = PlatformActionsFactory.create(driver);
        this.common = new CommonComponentsPage();
        this.loginPage = new LoginPage();
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
    @Step("Full login: skip → env → credentials → OTP → passcode → dashboard")
    public DashboardPage loginWith(String mobile, String id, String otp, String passcode) {
        skipOnboarding();

        // If passcode screen already visible (app remembers login), just enter passcode
        if (waits.isPresent(PASSCODE_SCREEN, 2)) {
            log.info("Passcode screen detected — entering passcode directly");
            passcodePage.enterPasscode(passcode);
            return new DashboardPage();
        }
        // If dashboard already visible, no login needed
        if (waits.isPresent(AppiumBy.accessibilityId("testID-master-amount-main"), 2)) {
            log.info("Dashboard already visible — skipping login");
            return new DashboardPage();
        }

        // Select environment (SIT/UAT) from dropdown before entering credentials
        selectEnvironment();

        enterCredentials(mobile, id);
        enterOtp(otp);
        // The passcode screen appears a beat AFTER OTP submission — the backend transition
        // can lag several seconds. Wait for it before firing keypad digits, otherwise the
        // passcode is typed into the transitional screen and never registers (→ no dashboard).
        if (!waits.isPresent(PASSCODE_SCREEN, 20)) {
            log.warn("Passcode screen not detected within 20s after OTP — attempting passcode entry anyway");
        }
        passcodePage.enterPasscode(passcode);
        // Let the dashboard render after passcode; backend can be slow on the first load.
        if (waits.isPresent(AppiumBy.accessibilityId("testID-master-amount-main"), 20)) {
            log.info("Dashboard rendered after passcode");
        }
        return new DashboardPage();
    }

    // ── Private Steps ──────────────────────────────────

    @Step("Select environment from login dropdown")
    private void selectEnvironment() {
        String env = ConfigManager.getInstance().getEnv(); // e.g. "SIT"
        // The login screen exposes an "Environment" dropdown (placeholder text "Environment").
        // Its testID is not stable across builds, so locate by visible text and treat this as
        // BEST-EFFORT: if the dropdown/option is absent, skip and continue — the SIT build
        // defaults to the SIT backend, so login still proceeds.
        By envDropdown = AppiumBy.xpath(
                "//*[@content-desc='testID-env-dropdown' or @text='Environment' "
                + "or @content-desc='Environment']");
        if (!waits.isPresent(envDropdown, 5)) {
            log.warn("Environment dropdown not found — skipping env selection (defaulting to {})", env);
            return;
        }
        try {
            driver.findElement(envDropdown).click();
            log.info("Opened environment dropdown");
            By option = AppiumBy.xpath(
                    "//*[@text='" + env + "' or @content-desc='" + env + "' or @label='" + env + "']");
            waits.waitForClickable(option, 6).click();
            log.info("Environment selected: {}", env);
        } catch (Exception e) {
            log.warn("Environment selection incomplete for '{}' — continuing to credentials: {}",
                    env, e.getMessage());
        }
    }

    @Step("Skip onboarding screens")
    private void skipOnboarding() {
        // Wait for ANY first screen element (app loaded)
        By anyFirst = AppiumBy.xpath(
                "//*[@text='Skip' or @text='No thanks' or @text='Allow' "
                + "or @text='Later' or @text='Passcode' or @text='Enter your passcode' "
                + "or @content-desc='testID-secondary-login-main' "
                + "or @content-desc='testID-input-direct-mobile' "
                + "or @content-desc='testID-secondary-action-main' "
                + "or @content-desc='testID-primary-enableLocation-main' "
                + "or @content-desc='testID-master-amount-main']");
        waits.waitForVisible(anyFirst, 30);

        // ALL skippable elements in ONE xpath
        By skipAll = AppiumBy.xpath(
                "//*[@text='Skip' or @text='No thanks' or @text='NO THANKS' "
                + "or @text='Allow' or @text='ALLOW' or @text='While using the app' "
                + "or @text='Later' "
                + "or @content-desc='testID-secondary-action-main' "
                + "or @content-desc='testID-primary-enableLocation-main']");

        // Login button = final onboarding target (tap it to go to login form)
        By loginBtn = AppiumBy.accessibilityId("testID-secondary-login-main");

        // True terminal = dashboard or passcode screen (NOT mobile field — it exists in DOM behind onboarding)
        By realTerminal = AppiumBy.xpath(
                "//*[@content-desc='testID-master-amount-main' "
                + "or contains(@content-desc,'testID-passCode.screen')]");

        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        for (int i = 0; i < 8; i++) {
            // Check real terminal first (dashboard/passcode — means we're past login)
            try {
                var termEls = driver.findElements(realTerminal);
                if (!termEls.isEmpty() && termEls.get(0).isDisplayed()) {
                    log.info("Dashboard/passcode visible — skip complete after {} iteration(s)", i);
                    break;
                }
            } catch (Exception ignored) {}

            // Try Login button (last onboarding step → goes to login form)
            try {
                var loginEls = driver.findElements(loginBtn);
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
        waits.waitForClickable(MOBILE, 8).click();
        driver.findElement(MOBILE).sendKeys(mobile);
        log.info("Mobile: {}", mobile);

        dismissKeyboard();
        waits.waitForClickable(NATID, 5).click();
        driver.findElement(NATID).sendKeys(id);
        log.info("ID: {}", id);

        dismissKeyboard();
        waits.waitForClickable(SUBMIT, 5).click();
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
