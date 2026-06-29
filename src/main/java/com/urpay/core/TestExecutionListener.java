package com.urpay.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * testMethodName → crash/health context captured at failure, only when the app crashed
     * or left the foreground. Injected into the Allure result message in onFinish so the
     * "App crashed / not in foreground" category matches (otherwise the crash is visible only
     * in the console log and the failure is mis-bucketed as a generic element timeout).
     */
    private final Map<String, String> failureHealth = new ConcurrentHashMap<>();

    /** testMethodName → logcat crash-evidence filename (text/plain) in allure-results */
    private final Map<String, String> failureCrashLogFiles = new ConcurrentHashMap<>();

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getFullTestName(result);
        // Do NOT create Allure steps from listener callbacks — the Allure TestNG adapter
        // owns the test lifecycle and records start/stop/status automatically. Stepping here
        // logs "no test case running". Keep to console logging + cloud session naming.
        CloudSessionManager.updateTestName(testName);
        log.info("▶ Starting: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        CloudSessionManager.updateStatus("passed");
        log.info("✅ Passed: {}", testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();

        try {
            AppiumDriver driver = DriverFactory.getInstance().getDriver();

            // Log app health state — distinguishes crashes from assertion failures during triage
            try {
                AppHealthChecker healthChecker = AppHealthCheckerFactory.create(driver);
                boolean crashed = healthChecker.hasAppCrashed();
                boolean inForeground = healthChecker.isAppInForeground();
                // Logcat crash signature catches a crash that already auto-restarted (process
                // alive again → crashed=false) by reading the persisted FATAL EXCEPTION / ANR.
                String crashSignature = healthChecker.findCrashSignature();
                boolean crashDetected = crashed || (crashSignature != null && !crashSignature.isEmpty());
                String healthReport = String.format(
                        "App State: %s | Foreground: %s | Crashed: %s | LogcatCrash: %s",
                        healthChecker.getAppState(),
                        inForeground,
                        crashed,
                        crashSignature != null && !crashSignature.isEmpty());
                if (crashDetected) {
                    log.error("⚠ APP CRASHED during {} — {}", testName, healthReport);
                    failureHealth.put(testName, "APP CRASHED - " + healthReport);
                    if (crashSignature != null && !crashSignature.isEmpty()) {
                        writeCrashLogAttachment(testName, crashSignature);
                        log.error("Crash signature for {}:\n{}", testName, crashSignature);
                    }
                } else if (!inForeground) {
                    // App left the foreground but no crash signature in logcat and the process is
                    // still alive — surfaced as a distinct symptom (could be a backgrounding, a
                    // system dialog, or a crash whose log we couldn't read).
                    log.warn("⚠ App not in foreground during {} — {}", testName, healthReport);
                    failureHealth.put(testName, "APP NOT IN FOREGROUND - " + healthReport);
                } else {
                    log.info("Health check for {}: {}", testName, healthReport);
                }
            } catch (Throwable healthEx) {
                // Health reporting is best-effort triage only. Catch Throwable (not just
                // Exception) so a linkage/NoClassDefFoundError here can never mask the real
                // test failure or crash the listener (which aborts the forked test JVM).
                log.debug("Health check unavailable: {}", healthEx.getMessage());
            }

            // Screenshot → allure-results/ (linked to the failed test via the onFinish JSON patch)
            byte[] screenshotBytes = ScreenshotUtils.takeScreenshotAsBytes(driver);
            if (screenshotBytes.length > 0) {
                String fileName = UUID.randomUUID() + "-attachment.png";
                Files.createDirectories(ALLURE_DIR);
                Files.write(ALLURE_DIR.resolve(fileName), screenshotBytes);
                failureScreenshots.put(testName, fileName);
                log.info("Screenshot saved for Allure: {} -> {}", testName, fileName);
            }
            // Keep a copy on disk for quick local inspection
            ScreenshotUtils.takeScreenshot(driver, testName);
        } catch (Exception e) {
            log.warn("Screenshot capture failed for {}: {}", testName, e.getMessage());
        }

        CloudSessionManager.updateStatus("failed");
        log.error("❌ Failed: {} — {}", testName,
                throwable != null ? throwable.getMessage() : "unknown error");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.warn("⏭ Skipped: {}", testName);
    }

    // ── ISuiteListener — patch allure JSONs after all tests complete ──

    @Override
    public void onStart(ISuite suite) { /* no-op */ }

    @Override
    public void onFinish(ISuite suite) {
        if (failureScreenshots.isEmpty() && failureHealth.isEmpty()) return;
        log.info("Patching Allure result(s) with failure screenshots / crash context...");

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

            // Only failed/broken results carry screenshots or crash context
            if (!json.contains("\"status\":\"failed\"") && !json.contains("\"status\":\"broken\"")) {
                return;
            }

            // Match this file to a captured test by name (e.g. "name":"testZainRecharge")
            String testName = matchTestName(json);
            if (testName == null) {
                return; // one patch per file
            }

            boolean changed = false;

            // 1. Failure screenshot + crash log → attachments array
            if (json.contains("\"attachments\":[]")) {
                List<String> attachments = new ArrayList<>();
                String screenshotFile = failureScreenshots.get(testName);
                if (screenshotFile != null) {
                    attachments.add("{\"name\":\"Failure Screenshot\","
                            + "\"source\":\"" + screenshotFile + "\","
                            + "\"type\":\"image/png\"}");
                }
                String crashLogFile = failureCrashLogFiles.get(testName);
                if (crashLogFile != null) {
                    attachments.add("{\"name\":\"Crash Log (logcat)\","
                            + "\"source\":\"" + crashLogFile + "\","
                            + "\"type\":\"text/plain\"}");
                }
                if (!attachments.isEmpty()) {
                    json = json.replace("\"attachments\":[]",
                            "\"attachments\":[" + String.join(",", attachments) + "]");
                    changed = true;
                    log.info("Patched Allure result for {} with {} attachment(s)",
                            testName, attachments.size());
                }
            }

            // 2. Crash / not-in-foreground context → prepend to statusDetails.message so the
            //    "App crashed / not in foreground" category matches and the crash surfaces in
            //    the report itself, not just the console log.
            String health = failureHealth.get(testName);
            if (health != null) {
                String patched = injectHealthIntoMessage(json, health);
                if (!patched.equals(json)) {
                    json = patched;
                    changed = true;
                    log.info("Tagged Allure result for {} with crash context: {}", testName, health);
                }
            }

            if (changed) {
                Files.writeString(resultFile, json, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Failed to patch {}: {}", resultFile.getFileName(), e.getMessage());
        }
    }

    /** Return the captured failed-test name whose Allure {@code name} appears in this result. */
    private String matchTestName(String json) {
        for (String testName : failureScreenshots.keySet()) {
            if (json.contains("\"name\":\"" + testName + "\"")) {
                return testName;
            }
        }
        for (String testName : failureHealth.keySet()) {
            if (json.contains("\"name\":\"" + testName + "\"")) {
                return testName;
            }
        }
        return null;
    }

    /**
     * Prepend the crash/health marker to the test's statusDetails.message. Idempotent — skips
     * if already tagged or no message field is present.
     */
    private String injectHealthIntoMessage(String json, String health) {
        String marker = "[" + health + "] ";
        if (json.contains(marker)) {
            return json; // already tagged
        }
        String anchor = "\"message\":\"";
        int idx = json.indexOf(anchor);
        if (idx < 0) {
            return json;
        }
        int insertAt = idx + anchor.length();
        return json.substring(0, insertAt) + jsonEscape(marker) + json.substring(insertAt);
    }

    /** Minimal JSON string escaping for the controlled health marker (no control chars). */
    private String jsonEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Persist the logcat crash evidence as a text attachment linked in onFinish. */
    private void writeCrashLogAttachment(String testName, String crashSignature) {
        try {
            Files.createDirectories(ALLURE_DIR);
            String fileName = UUID.randomUUID() + "-attachment.txt";
            Files.write(ALLURE_DIR.resolve(fileName),
                    crashSignature.getBytes(StandardCharsets.UTF_8));
            failureCrashLogFiles.put(testName, fileName);
            log.info("Crash log saved for Allure: {} -> {}", testName, fileName);
        } catch (IOException e) {
            log.warn("Failed to write crash log attachment for {}: {}", testName, e.getMessage());
        }
    }

    private String getFullTestName(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        String methodName = result.getMethod().getMethodName();
        return className + " :: " + methodName;
    }
}
