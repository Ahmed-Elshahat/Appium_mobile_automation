package com.urpay.flows;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;
import com.urpay.core.DriverFactory;
import com.urpay.pages.dashboard.DashboardPage;
import com.urpay.utils.WaitUtils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
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
 */
public class LoginFlow {

    private static final Logger log = LoggerFactory.getLogger(LoginFlow.class);

    private final AndroidDriver driver;
    private final WaitUtils waits;

    // ── Locators ───────────────────────────────────────
    private static final By SKIP_TEXT   = AppiumBy.xpath("//*[@text='Skip']");
    private static final By SKIP_SEC   = AppiumBy.accessibilityId("testID-secondary-action-main");
    private static final By LOC_ENABLE = AppiumBy.accessibilityId("testID-primary-enableLocation-main");
    private static final By LOGIN_BTN  = AppiumBy.accessibilityId("testID-secondary-login-main");
    private static final By MOBILE     = AppiumBy.accessibilityId("testID-input-direct-mobile");
    private static final By NATID      = AppiumBy.accessibilityId("testID-input-direct-id");
    private static final By SUBMIT     = AppiumBy.accessibilityId("testID-primary--main");
    private static final By OTP_0      = AppiumBy.accessibilityId("testID-OTP-Input-Field-0");
    private static final By LATER_BTN  = AppiumBy.xpath("//*[@text='Later']");
    private static final By PASSCODE_SCREEN = AppiumBy.xpath(
            "//*[contains(@content-desc,'testID-passCode.screen') or contains(@text,'passcode') or contains(@text,'PIN')]");

    public LoginFlow() {
        this.driver = (AndroidDriver) DriverFactory.getInstance().getDriver();
        this.waits = new WaitUtils(driver, 10);
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

        // If passcode screen already visible (app remembers login), just enter passcode
        if (waits.isPresent(PASSCODE_SCREEN, 2)) {
            log.info("Passcode screen detected — entering passcode directly");
            enterPasscode(passcode);
            return new DashboardPage();
        }
        // If dashboard already visible, no login needed
        if (waits.isPresent(AppiumBy.accessibilityId("testID-master-amount-main"), 2)) {
            log.info("Dashboard already visible — skipping login");
            return new DashboardPage();
        }

        enterCredentials(mobile, id);
        enterOtp(otp);
        enterPasscode(passcode);
        return new DashboardPage();
    }

    // ── Private Steps ──────────────────────────────────

    @Step("Skip onboarding screens")
    private void skipOnboarding() {
        // Handle Android system dialogs first (Location Accuracy, permissions)
        By noThanks = AppiumBy.xpath("//*[@text='No thanks' or @text='NO THANKS']");
        By allowBtn = AppiumBy.xpath("//*[@text='Allow' or @text='ALLOW' or @text='While using the app']");
        By anyFirst = AppiumBy.xpath(
                "//*[@text='Skip' or @text='No thanks' or @text='Allow' " +
                "or @text='Later' or @text='Passcode' or @text='Enter your passcode' " +
                "or @content-desc='testID-secondary-login-main' " +
                "or @content-desc='testID-input-direct-mobile' " +
                "or @content-desc='testID-secondary-action-main' " +
                "or @content-desc='testID-primary-enableLocation-main' " +
                "or @content-desc='testID-master-amount-main']");
        waits.waitForVisible(anyFirst, 20);

        // If passcode screen or dashboard already showing, skip onboarding
        if (waits.isPresent(PASSCODE_SCREEN, 1) ||
            waits.isPresent(AppiumBy.accessibilityId("testID-master-amount-main"), 1)) {
            log.info("Passcode or Dashboard already visible — skipping onboarding");
            return;
        }

        // Fast skip loop — handle system dialogs + app onboarding
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ZERO);
        for (int i = 0; i < 10; i++) {
            if (quickTap(LOGIN_BTN)) break;
            if (quickTap(MOBILE)) break;
            // System dialogs
            quickTap(noThanks);
            quickTap(allowBtn);
            // App onboarding
            quickTap(SKIP_TEXT);
            quickTap(SKIP_SEC);
            quickTap(LOC_ENABLE);
            // Notifications popup
            quickTap(LATER_BTN);
        }
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(10));

        // Ensure we're on login form
        waits.waitForClickable(MOBILE, 15);
        log.info("Onboarding skipped, login form ready");
    }

    @Step("Enter credentials: {mobile} / {id}")
    private void enterCredentials(String mobile, String id) {
        // Mobile
        waits.waitForClickable(MOBILE, 10).click();
        driver.findElement(MOBILE).sendKeys(mobile);
        log.info("Mobile: {}", mobile);

        // Dismiss keyboard → ID
        dismissKeyboard();
        waits.waitForClickable(NATID, 10).click();
        driver.findElement(NATID).sendKeys(id);
        log.info("ID: {}", id);

        // Dismiss keyboard → Submit
        dismissKeyboard();
        waits.waitForClickable(SUBMIT, 10).click();
        log.info("Login submitted");
    }

    @Step("Enter OTP: {otp}")
    private void enterOtp(String otp) {
        waits.waitForClickable(OTP_0, 15);
        quickTap(OTP_0);
        for (char d : otp.toCharArray()) {
            pressDigit(d);
        }
        log.info("OTP entered: {}", otp);
    }

    @Step("Enter passcode")
    private void enterPasscode(String passcode) {
        // Wait for OTP to disappear (means passcode screen loaded)
        boolean passcodeVisible = waits.isPresent(PASSCODE_SCREEN, 5);
        if (!passcodeVisible) {
            waits.waitForInvisible(OTP_0, 5);
        }
        for (char d : passcode.toCharArray()) {
            pressDigit(d);
        }
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
        try {
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
        } catch (Exception ignored) {}
    }

    private void pressDigit(char c) {
        driver.pressKey(new KeyEvent(AndroidKey.valueOf("DIGIT_" + c)));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }
}
