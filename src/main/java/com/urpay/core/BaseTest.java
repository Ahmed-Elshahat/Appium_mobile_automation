package com.urpay.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.urpay.helpers.RegistrationApiHelper;
import com.urpay.helpers.WalletBalanceHelper;
import com.urpay.platform.health.AppHealthChecker;
import com.urpay.platform.health.AppHealthCheckerFactory;
import com.urpay.reporting.ApiReporting;
import com.urpay.reporting.ReportManager;
import com.urpay.utils.ScreenshotUtils;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.qameta.allure.Allure;

/**
 * Base test class — all test classes extend this.
 *
 * SOLID:
 *   SRP — Only handles driver lifecycle and suite setup.
 *         Reporting/screenshots/cloud hooks in TestExecutionListener.
 *   DIP — Uses DriverFactory.getInstance() (DriverProvider interface).
 *
 * Rules enforced:
 *   - Driver lifecycle in @BeforeMethod/@AfterClass (a fresh session per suite, not suite-level).
 *   - No Thread.sleep().
 *   - No assertions (those belong in test classes).
 *   - No reporting logic (that's the listener's job).
 */
@Listeners(TestExecutionListener.class)
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ConfigManager config;
    protected AppHealthChecker healthChecker;

    @BeforeSuite(alwaysRun = true)
    public void setupSuite() {
        config = ConfigManager.getInstance();

        String profileName = config.getProfileName();
        ReportManager.initReports("URPay-" + profileName);
        ReportManager.writeAllureMetadata();
        // Attach every REST Assured API request + response (incl. status code) to the Allure report.
        ApiReporting.enable();

        log.info("═══════════════════════════════════════════");
        log.info("  URPay Test Suite Starting");
        log.info("  Environment: {}", config.getEnv());
        log.info("  Profile: {}", profileName);
        log.info("  Platform: {}", config.get("platform", "android"));
        log.info("  Remote: {}", config.getBoolean("remote", false));
        log.info("═══════════════════════════════════════════");
    }

    @BeforeMethod(alwaysRun = true)
    public void setupDriver() {
        // Only init driver if not already active (supports chained tests via dependsOnMethods)
        if (!DriverFactory.getInstance().isDriverActive()) {
            DriverFactory.getInstance().initDriver();
        }
        healthChecker = AppHealthCheckerFactory.create(getDriver());
    }

    /**
     * Capture failure screenshot INSIDE the test lifecycle — this runs BEFORE ITestListener
     * callbacks, so the Allure lifecycle is still open and Allure.addAttachment() works.
     *
     * Execution order: test fails → @AfterMethod (HERE, Allure OPEN) → ITestListener.onTestFailure
     * (Allure adapter closes test case). This guarantees the screenshot attaches to the correct
     * test result without needing post-hoc JSON patching.
     */
    @AfterMethod(alwaysRun = true)
    public void captureFailureScreenshot(ITestResult result) {
        if (result.getStatus() != ITestResult.FAILURE) {
            return;
        }
        String testName = result.getMethod().getMethodName();
        try {
            if (!DriverFactory.getInstance().isDriverActive()) {
                log.warn("Driver not active for failure screenshot: {}", testName);
                // Attach failure details as text when no driver
                Throwable t = result.getThrowable();
                String details = "Screenshot unavailable — driver not active.\n\n"
                        + "Error: " + (t != null ? t.getClass().getName() + ": " + t.getMessage() : "unknown");
                Allure.addAttachment("Failure Details (no driver)",
                        "text/plain", details);
                return;
            }
            AppiumDriver driver = getDriver();
            // Screenshot
            byte[] screenshot = ScreenshotUtils.takeScreenshotAsBytes(driver);
            if (screenshot.length > 0) {
                Allure.addAttachment("Failure Screenshot", "image/png",
                        new java.io.ByteArrayInputStream(screenshot), ".png");
                log.info("✅ Failure screenshot attached to Allure for: {}", testName);
            } else {
                // Retry once after short delay (transient connection issues)
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                screenshot = ScreenshotUtils.takeScreenshotAsBytes(driver);
                if (screenshot.length > 0) {
                    Allure.addAttachment("Failure Screenshot (retry)", "image/png",
                            new java.io.ByteArrayInputStream(screenshot), ".png");
                    log.info("✅ Failure screenshot attached on retry for: {}", testName);
                } else {
                    log.warn("Screenshot returned 0 bytes for {} (FLAG_SECURE or connection issue)", testName);
                    Allure.addAttachment("Screenshot Unavailable",
                            "text/plain", "Screenshot capture returned 0 bytes (FLAG_SECURE or connection issue)");
                }
            }
            // Page source (UI hierarchy) — critical for element timeout triage
            try {
                String pageSource = driver.getPageSource();
                if (pageSource != null && !pageSource.isEmpty()) {
                    Allure.addAttachment("Page Source (UI hierarchy)", "text/xml",
                            new java.io.ByteArrayInputStream(pageSource.getBytes(java.nio.charset.StandardCharsets.UTF_8)), ".xml");
                }
            } catch (Exception psEx) {
                log.debug("Page source capture failed for {}: {}", testName, psEx.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failure capture failed for {}: {}", testName, e.getMessage());
            // Still attach error details as text
            Throwable t = result.getThrowable();
            String details = "Screenshot capture failed: " + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + "\n\nOriginal error: " + (t != null ? t.getClass().getName() + ": " + t.getMessage() : "unknown");
            try {
                Allure.addAttachment("Failure Details (capture error)",
                        "text/plain", details);
            } catch (Exception ignored) { }
        }
    }

    /**
     * Quit this suite's driver when the class finishes so the next {@code <test>} scheduled on
     * the same parallel worker thread starts a brand-new session. Without this the ThreadLocal
     * driver leaks across suites, and on cloud (LambdaTest) every subsequent suite would reuse
     * the previous suite's session/device. Per-class (not per-method) because a suite's chained
     * tests ({@code dependsOnMethods}) deliberately share one session/device.
     */
    @AfterClass(alwaysRun = true)
    public void teardownDriver() {
        DriverFactory.getInstance().quitDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void teardownSuite() {
        ReportManager.flush();
        DriverFactory.getInstance().quitAllDrivers();
        log.info("Suite completed. All drivers cleaned up. Report generated.");
    }

    // ── Convenience for subclasses ─────────────────────────────────

    protected AppiumDriver getDriver() {
        return DriverFactory.getInstance().getDriver();
    }

    /**
     * Fail fast with a categorised {@code AppCrashException} if the app process has crashed.
     * Call at critical checkpoints (e.g. right after submitting an OTP, confirming a payment, or
     * entering an amount) to pin a crash to the exact feature instead of waiting for the next
     * element to time out.
     */
    protected void assertAppAlive() {
        com.urpay.utils.CrashGuard.assertAppAlive(getDriver());
    }

    protected void forceRestartApp() {
        String appId = config.get("platform", "android").equalsIgnoreCase("ios")
                ? config.get("bundleId", "com.urpay.consumer.sit")
                : config.get("appPackage", "com.urpay.consumer.sit");
        try {
            AppiumDriver driver = getDriver();
            if (driver instanceof InteractsWithApps) {
                ((InteractsWithApps) driver).terminateApp(appId);
                ((InteractsWithApps) driver).activateApp(appId);
            }
            log.info("App restarted: {}", appId);
        } catch (Exception e) {
            log.warn("App restart failed, reinitializing driver: {}", e.getMessage());
            DriverFactory.getInstance().quitDriver();
            DriverFactory.getInstance().initDriver();
        }
    }

    /**
     * Top up wallet balance to 20,000 SAR for any user (old or new).
     * Call in @BeforeSuite or login method to ensure sufficient balance for P&C tests.
     * @param walletNumber the wallet number from config or provisioned user
     */
    protected void topUpBalance(String walletNumber) {
        if (walletNumber != null && !walletNumber.isEmpty()) {
            boolean ok = WalletBalanceHelper.topUp(walletNumber);
            if (ok) {
                log.info("Wallet {} topped up to 20,000 SAR", walletNumber);
            } else {
                log.warn("Failed to top up wallet {}", walletNumber);
            }
        }
    }

    /**
     * Top up wallet balance by mobile number (for old/existing users in P&C config).
     * @param mobileNumber the mobile number (05XXXXXXXX format)
     */
    protected void topUpBalanceByMobile(String mobileNumber) {
        if (mobileNumber != null && !mobileNumber.isEmpty()) {
            boolean ok = WalletBalanceHelper.topUpByMobile(mobileNumber);
            if (ok) {
                log.info("Wallet for mobile {} topped up to 20,000 SAR", mobileNumber);
            } else {
                log.warn("Failed to top up wallet for mobile {}", mobileNumber);
            }
        }
    }

    /**
     * Register a fresh NAT user via API and top up balance. Returns provisioned credentials.
     * Use for tests that need a brand-new account with funds.
     */
    protected RegistrationApiHelper.Provisioned registerFreshUserWithBalance() {
        RegistrationApiHelper.Provisioned user = RegistrationApiHelper.registerNationalAndReturn();
        if (user == null) {
            throw new RuntimeException("Failed to provision a new user via API — check VPN/backend access");
        }
        log.info("User provisioned: mobile={}, poi={}, wallet={}", user.mobile, user.poi, user.walletNumber);
        topUpBalance(user.walletNumber);
        return user;
    }

    /**
     * Capture screenshot and attach to Allure report.
     * Call this INSIDE a test method (where Allure context is active).
     */
    protected void captureScreenshot(String name) {
        try {
            byte[] screenshot = ScreenshotUtils.takeScreenshotAsBytes(getDriver());
            if (screenshot.length > 0) {
                Allure.addAttachment(name, "image/png",
                        new java.io.ByteArrayInputStream(screenshot), ".png");
            }
        } catch (Exception e) {
            log.warn("Screenshot capture failed: {}", e.getMessage());
        }
    }
}
