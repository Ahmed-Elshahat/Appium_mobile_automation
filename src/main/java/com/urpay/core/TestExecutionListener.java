package com.urpay.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.urpay.platform.health.AppHealthChecker;
import com.urpay.platform.health.AppHealthCheckerFactory;
import com.urpay.reporting.ReportManager;
import com.urpay.utils.ScreenshotUtils;

import io.appium.java_client.AppiumDriver;

/**
 * TestNG listener for reporting, screenshots, and cloud hooks.
 *
 * Screenshots are saved to allure-results/ on failure.
 * After the suite completes, allure result JSONs are patched to include
 * the screenshot attachments (the Allure TestNG adapter closes test cases
 * before this listener runs, so direct lifecycle API doesn't work).
 */
public class TestExecutionListener implements ITestListener, ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger(TestExecutionListener.class);
    private static final Path ALLURE_DIR = Paths.get("allure-results");

    /** testMethodName → screenshot filename in allure-results */
    private final Map<String, String> failureScreenshots = new ConcurrentHashMap<>();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getFullTestName(result);
        ReportManager.createTest(testName);
        ReportManager.info("Test started: " + result.getMethod().getMethodName());
        CloudSessionManager.updateTestName(testName);
        log.info("▶ Starting: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ReportManager.pass("TEST PASSED: " + testName);
        CloudSessionManager.updateStatus("passed");
        log.info("✅ Passed: {}", testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        try {
            AppiumDriver driver = DriverFactory.getInstance().getDriver();

            // Capture app health state before screenshot/recovery
            try {
                AppHealthChecker healthChecker = AppHealthCheckerFactory.create(driver);
                String appState = healthChecker.getAppState();
                boolean inForeground = healthChecker.isAppInForeground();
                boolean crashed = healthChecker.hasAppCrashed();

                String healthReport = String.format(
                        "App State: %s | Foreground: %s | Crashed: %s",
                        appState, inForeground, crashed);
                log.info("Health check for {}: {}", testName, healthReport);

                // Attach health info to Allure
                Files.createDirectories(ALLURE_DIR);
                String healthFileName = UUID.randomUUID() + "-health.txt";
                Files.write(ALLURE_DIR.resolve(healthFileName),
                        healthReport.getBytes(StandardCharsets.UTF_8));

                if (crashed) {
                    log.error("⚠ APP CRASHED during {}", testName);
                    ReportManager.fail("APP CRASH DETECTED: " + healthReport);
                } else if (!inForeground) {
                    log.warn("⚠ App not in foreground during {}: {}", testName, appState);
                    ReportManager.fail("APP NOT IN FOREGROUND: " + healthReport);
                }
            } catch (Exception healthEx) {
                log.debug("Health check unavailable: {}", healthEx.getMessage());
            }

            byte[] screenshotBytes = ScreenshotUtils.takeScreenshotAsBytes(driver);

            if (screenshotBytes.length > 0) {
                // Write screenshot to allure-results/
                String fileName = UUID.randomUUID() + "-attachment.png";
                Files.createDirectories(ALLURE_DIR);
                Files.write(ALLURE_DIR.resolve(fileName), screenshotBytes);
                failureScreenshots.put(testName, fileName);
                log.info("Screenshot saved for Allure: {} -> {}", testName, fileName);
            }

            // ExtentReports attachment
            String base64 = ScreenshotUtils.takeScreenshotAsBase64(driver);
            if (!base64.isEmpty()) {
                ReportManager.fail("TEST FAILED: " + testName, base64);
            } else {
                ReportManager.fail("TEST FAILED: " + testName + " (screenshot unavailable)");
            }

            // Save to disk (target/screenshots/)
            ScreenshotUtils.takeScreenshot(driver, testName);
        } catch (Exception e) {
            log.warn("Screenshot capture failed for {}: {}", testName, e.getMessage());
            ReportManager.fail("TEST FAILED: " + testName + " (driver unavailable)");
        }

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            ReportManager.fail("Error: " + throwable.getMessage());
        }

        CloudSessionManager.updateStatus("failed");
        log.error("❌ Failed: {} — {}", testName,
                throwable != null ? throwable.getMessage() : "unknown error");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ReportManager.skip("TEST SKIPPED: " + testName);
        log.warn("⏭ Skipped: {}", testName);
    }

    // ── ISuiteListener — patch allure JSONs after all tests complete ──

    @Override
    public void onStart(ISuite suite) { /* no-op */ }

    @Override
    public void onFinish(ISuite suite) {
        if (failureScreenshots.isEmpty()) return;
        log.info("Patching {} Allure result(s) with failure screenshots...", failureScreenshots.size());

        try {
            Files.list(ALLURE_DIR)
                    .filter(p -> p.toString().endsWith("-result.json"))
                    .forEach(this::patchResultFile);
        } catch (IOException e) {
            log.warn("Failed to patch Allure results: {}", e.getMessage());
        }
    }

    private void patchResultFile(Path resultFile) {
        try {
            String json = Files.readString(resultFile, StandardCharsets.UTF_8);

            for (Map.Entry<String, String> entry : failureScreenshots.entrySet()) {
                String testName = entry.getKey();
                String screenshotFile = entry.getValue();

                // Match by test name in the JSON (e.g. "name":"testZainRecharge")
                if (json.contains("\"name\":\"" + testName + "\"")
                        && (json.contains("\"status\":\"failed\"") || json.contains("\"status\":\"broken\""))) {

                    // Build attachment JSON
                    String attachment = "{\"name\":\"Failure Screenshot\","
                            + "\"source\":\"" + screenshotFile + "\","
                            + "\"type\":\"image/png\"}";

                    // Insert into attachments array
                    json = json.replace("\"attachments\":[]",
                            "\"attachments\":[" + attachment + "]");

                    Files.writeString(resultFile, json, StandardCharsets.UTF_8);
                    log.info("Patched Allure result for {} with screenshot {}", testName, screenshotFile);
                    return; // one patch per file
                }
            }
        } catch (IOException e) {
            log.warn("Failed to patch {}: {}", resultFile.getFileName(), e.getMessage());
        }
    }

    private String getFullTestName(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        String methodName = result.getMethod().getMethodName();
        return className + " :: " + methodName;
    }
}
