package com.urpay.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.urpay.platform.health.AppHealthChecker;
import com.urpay.platform.health.AppHealthCheckerFactory;
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
 *   - Driver lifecycle in @BeforeMethod/@AfterMethod (not suite-level).
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
