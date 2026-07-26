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
import java.util.concurrent.atomic.AtomicBoolean;

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

    /** testMethodName → page-source (UI hierarchy) filename (text/xml) in allure-results */
    private final Map<String, String> failurePageSources = new ConcurrentHashMap<>();

    /** Guard so the ACTUAL device/session is appended to environment.properties only once per run. */
    private static final AtomicBoolean ENV_CAPTURED = new AtomicBoolean(false);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getFullTestName(result);
        // Do NOT create Allure steps from listener callbacks — the Allure TestNG adapter
        // owns the test lifecycle and records start/stop/status automatically. Stepping here
        // logs "no test case running". Keep to console logging + cloud session naming.
        CloudSessionManager.updateTestName(testName);
        captureActualEnvironmentOnce();
        log.info("▶ Starting: {}", testName);
    }

    /**
     * Append the device/session the cloud ACTUALLY allocated to environment.properties, once per
     * run. The BeforeSuite metadata records only the REQUESTED deviceName; on LambdaTest the nearest
     * available device is often different (e.g. Pixel 8 instead of the requested Galaxy S24), which
     * is otherwise invisible during triage. Best-effort: any failure is logged and ignored.
     */
    private void captureActualEnvironmentOnce() {
        if (ENV_CAPTURED.get() || !DriverFactory.getInstance().isDriverActive()) {
            return;
        }
        if (!ENV_CAPTURED.compareAndSet(false, true)) {
            return;
        }
        try {
            AppiumDriver driver = DriverFactory.getInstance().getDriver();
            org.openqa.selenium.Capabilities caps = driver.getCapabilities();
            String device = String.valueOf(cap(caps, "deviceName", "deviceModel", "device"));
            String osVer = String.valueOf(cap(caps, "platformVersion", "osVersion"));
            String session = driver.getSessionId() != null ? driver.getSessionId().toString() : "n/a";
            String extra = "Device.Actual=" + device + "\n"
                    + "Platform.Version.Actual=" + osVer + "\n"
                    + "Session.Id=" + session + "\n";
            Files.createDirectories(ALLURE_DIR);
            Files.writeString(ALLURE_DIR.resolve("environment.properties"), extra,
                    StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
            log.info("Captured actual run environment: device={} os={} session={}",
                    device, osVer, session);
        } catch (Throwable t) {
            log.debug("Actual-environment capture skipped: {}", t.getMessage());
        }
    }

    /** First non-empty capability value across the given keys (also tries the {@code appium:} prefix). */
    private static Object cap(org.openqa.selenium.Capabilities caps, String... keys) {
        for (String key : keys) {
            Object value = caps.getCapability(key);
            if (value == null) {
                value = caps.getCapability("appium:" + key);
            }
            if (value != null && !String.valueOf(value).isEmpty()) {
                return value;
            }
        }
        return "n/a";
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

        // A lost/timed-out Appium session is an INFRA failure, not an app crash. Detect it from the
        // test's own throwable and tag it distinctly, then skip the health probe below — that probe
        // would itself throw (the session is gone) and produce the misleading
        // "APP CRASHED - App State: UNKNOWN (error: Unable to find the session info ...)" text that
        // pollutes the report with false broken/unknown results.
        if (com.urpay.utils.SessionLoss.isSessionLost(throwable)) {
            String firstLine = throwable != null && throwable.getMessage() != null
                    ? throwable.getMessage().split("\\R", 2)[0] : "session has quit or timed out";
            log.warn("⚠ SESSION LOST during {} (infra, not an app crash) — {}", testName, firstLine);
            failureHealth.put(testName, "SESSION LOST (infra) - " + firstLine);
            // The session is gone — screenshots / logcat / app-state probes would all throw. Skip
            // them; the distinct SESSION-LOST tag above is enough for correct categorization.
            return;
        }

        try {
            AppiumDriver driver = DriverFactory.getInstance().getDriver();

            // GLOBAL backend-outage probe: any failure where the "Service is currently unavailable /
            // Transaction Declined" banner is on screen is a SIT backend defect — regardless of
            // whether the flow threw a BackendErrorException or the test merely asserted on a
            // missing success screen. Probing here (parallel to the crash probe) makes backend
            // detection automatic for EVERY suite, so individual flows no longer each need their own
            // banner check. Zero risk: it only re-labels an already-failed test.
            boolean backendTagged = alreadyBackendError(throwable);
            if (!backendTagged) {
                try {
                    java.util.Optional<String> banner =
                            com.urpay.utils.BackendErrorGuard.detect(new com.urpay.utils.WaitUtils(driver, 2));
                    if (banner.isPresent()) {
                        String text = banner.get().replaceAll("\\s+", " ").trim();
                        log.error("⚠ BACKEND DEFECT during {} — banner: {}", testName, text);
                        failureHealth.put(testName, "BACKEND DEFECT - " + text);
                        backendTagged = true;
                    }
                } catch (Throwable backendEx) {
                    log.debug("Backend-banner probe unavailable: {}", backendEx.getMessage());
                }
            }

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
                    if (!backendTagged) {
                        failureHealth.put(testName, "APP CRASHED - " + healthReport);
                    }
                    if (crashSignature != null && !crashSignature.isEmpty()) {
                        writeCrashLogAttachment(testName, crashSignature);
                        log.error("Crash signature for {}:\n{}", testName, crashSignature);
                    }
                } else if (!inForeground) {
                    // App left the foreground but no crash signature in logcat and the process is
                    // still alive — surfaced as a distinct symptom (could be a backgrounding, a
                    // system dialog, or a crash whose log we couldn't read).
                    log.warn("⚠ App not in foreground during {} — {}", testName, healthReport);
                    if (!backendTagged) {
                        failureHealth.put(testName, "APP NOT IN FOREGROUND - " + healthReport);
                    }
                } else {
                    log.info("Health check for {}: {}", testName, healthReport);
                }
            } catch (Throwable healthEx) {
                // Health reporting is best-effort triage only. Catch Throwable (not just
                // Exception) so a linkage/NoClassDefFoundError here can never mask the real
                // test failure or crash the listener (which aborts the forked test JVM).
                log.debug("Health check unavailable: {}", healthEx.getMessage());
            }

            // Full device logcat from the Appium server buffer (NOT the LambdaTest API, which caps
            // at 1MB = session start). This holds the crash tail (FATAL EXCEPTION / native signal /
            // ANR) for offline tracing. Requires devicelog=true so logcat capture isn't skipped.
            try {
                org.openqa.selenium.logging.LogEntries entries = driver.manage().logs().get("logcat");
                if (entries != null) {
                    Path lcDir = Paths.get("logcat");
                    Files.createDirectories(lcDir);
                    Path lcFile = lcDir.resolve(testName + "-" + System.currentTimeMillis() + ".log");
                    StringBuilder all = new StringBuilder();
                    int fatal = 0;
                    int lines = 0;
                    for (org.openqa.selenium.logging.LogEntry entry : entries) {
                        String m = entry.getMessage();
                        all.append(m).append('\n');
                        lines++;
                        if (m != null && (m.contains("FATAL EXCEPTION") || m.contains("FATAL SIGNAL")
                                || m.contains("ANR in") || m.contains("beginning of crash"))) {
                            fatal++;
                            log.error("CRASH> {}", m);
                        }
                    }
                    Files.writeString(lcFile, all.toString(), StandardCharsets.UTF_8);
                    log.error("⚑ Full logcat saved for {}: {} ({} lines, {} fatal markers)",
                            testName, lcFile.toAbsolutePath(), lines, fatal);
                }
            } catch (Exception lcEx) {
                log.warn("Logcat capture failed for {}: {}", testName, lcEx.getMessage());
            }

            // Screenshot → allure-results/ (linked to the failed test via the onFinish JSON patch)
            byte[] screenshotBytes = ScreenshotUtils.takeScreenshotAsBytes(driver);
            if (screenshotBytes.length > 0) {
                String fileName = UUID.randomUUID() + "-attachment.png";
                Files.createDirectories(ALLURE_DIR);
                Files.write(ALLURE_DIR.resolve(fileName), screenshotBytes);
                failureScreenshots.put(testName, fileName);
                log.info("Screenshot saved for Allure: {} -> {}", testName, fileName);
            } else {
                // Empty bytes = the OS refused the capture. The URPay app is FLAG_SECURE; on devices
                // that enforce it, Appium's getScreenshotAs returns nothing even though element
                // queries still work — which is exactly why failures can have no screenshot while
                // the test itself ran fine. Record WHY (kept out of the message if a stronger
                // health/backend tag already exists) so the report shows a reason, not a blank.
                log.warn("⚠ No screenshot for {} — getScreenshotAs returned 0 bytes (likely FLAG_SECURE "
                        + "screenshot block on this device). Page source is captured instead.", testName);
                failureHealth.putIfAbsent(testName,
                        "SCREENSHOT UNAVAILABLE (secure screen / FLAG_SECURE — capture blocked by OS)");
            }
            // Keep a copy on disk for quick local inspection
            ScreenshotUtils.takeScreenshot(driver, testName);

            // Page source (UI hierarchy) → allure-results/, linked via the onFinish JSON patch.
            // Locator/element timeouts are the #1 failure category; the XML tree shows instantly
            // whether the element was absent, renamed, or off-screen — no re-run needed.
            try {
                String pageSource = driver.getPageSource();
                if (pageSource != null && !pageSource.isEmpty()) {
                    String psName = UUID.randomUUID() + "-attachment.xml";
                    Files.writeString(ALLURE_DIR.resolve(psName), pageSource, StandardCharsets.UTF_8);
                    failurePageSources.put(testName, psName);
                    log.info("Page source saved for Allure: {} -> {}", testName, psName);
                }
            } catch (Exception psEx) {
                log.debug("Page source capture failed for {}: {}", testName, psEx.getMessage());
            }
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
        // Diagnostic summary — reveals at a glance whether the break is in CAPTURE (all zero) or in
        // LINKING (captured > 0 but report shows none). Compare against the per-test 'Screenshot
        // saved' / 'Patched Allure result' lines above.
        log.info("Allure attachment summary for suite '{}': screenshots={}, pageSources={}, healthTags={}",
                suite.getName(), failureScreenshots.size(), failurePageSources.size(), failureHealth.size());
        try {
            // 1. Attach failure screenshots + inject crash/health context (only if captured).
            if (!failureScreenshots.isEmpty() || !failureHealth.isEmpty()
                    || !failurePageSources.isEmpty()) {
                log.info("Patching Allure result(s) with failure screenshots / crash context...");
                Files.list(ALLURE_DIR)
                        .filter(p -> p.toString().endsWith("-result.json"))
                        .forEach(this::patchResultFile);
            }
            // 2. Demote broken results that are really product/UI defects to a clean 'failed' so the
            //    report never shows a backend 'service unavailable' or a locator timeout as 'broken'.
            //    Pure infra (session-loss, app-crash, driver-allocation) stays broken. Runs for
            //    every result file.
            Files.list(ALLURE_DIR)
                    .filter(p -> p.toString().endsWith("-result.json"))
                    .forEach(this::demoteBrokenResult);
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
            // Inject into the root-level attachments regardless of whether it is
            // already empty or already contains other entries (e.g. AllureRestAssured
            // API calls captured earlier in the test).
            {
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
                String pageSourceFile = failurePageSources.get(testName);
                if (pageSourceFile != null) {
                    attachments.add("{\"name\":\"Page Source (UI hierarchy)\","
                            + "\"source\":\"" + pageSourceFile + "\","
                            + "\"type\":\"text/xml\"}");
                }
                if (!attachments.isEmpty()) {
                    String newEntries = String.join(",", attachments);
                    String emptyKey = "\"attachments\":[]";
                    String nonEmptyKey = "\"attachments\":[";
                    int emptyIdx = json.indexOf(emptyKey);
                    if (emptyIdx >= 0) {
                        // Replace empty array with our attachments
                        json = json.substring(0, emptyIdx)
                                + "\"attachments\":[" + newEntries + "]"
                                + json.substring(emptyIdx + emptyKey.length());
                    } else {
                        // Prepend to the existing (non-empty) root-level attachments array
                        int nonEmptyIdx = json.indexOf(nonEmptyKey);
                        if (nonEmptyIdx >= 0) {
                            int insertAt = nonEmptyIdx + nonEmptyKey.length();
                            json = json.substring(0, insertAt)
                                    + newEntries + ","
                                    + json.substring(insertAt);
                        }
                    }
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

    /** Element / locator signatures that mean a test broke on the UI, not on a real defect. */
    private static final java.util.regex.Pattern LOCATOR_BROKEN = java.util.regex.Pattern.compile(
            "TimeoutException|NoSuchElement|no such element|StaleElementReference|"
            + "ElementNotInteractable|ElementClickIntercepted|InvalidSelector|InvalidElementState|"
            + "ElementNotVisible|waiting for element|Expected condition failed|"
            + "could not be located|Unable to locate element|element not found",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /** Backend / service defect signatures — a real product/BE failure, so these become 'failed'. */
    private static final java.util.regex.Pattern BACKEND_BROKEN = java.util.regex.Pattern.compile(
            "currently unavailable|Service is unavailable|Service Unavailable|service unavailable|"
            + "Transaction Declined|Something went wrong|Internal Server Error|try again later|"
            + "Backend rejected|SIT backend|service DOWN|Service is down",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /** Pure INFRA signatures that must KEEP their broken bucket (environment, not a product defect). */
    private static final java.util.regex.Pattern INFRA_BROKEN = java.util.regex.Pattern.compile(
            "SESSION LOST|Unable to find the session info|session has quit|invalid session id|"
            + "NoSuchSession|SessionNotCreated|Could not start a new session|"
            + "Unable to create a new remote session|APP CRASH|not in foreground|has crashed",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * Flip a {@code broken} result to a clean {@code failed} in the Allure result JSON when the
     * failure is a genuine product/UI defect that should never appear as "broken":
     * <ul>
     *   <li><b>Backend / service unavailable</b> — a real BE defect (not a test defect).</li>
     *   <li><b>Locator / element timeout</b> — UI flakiness, converted to a clean failure.</li>
     * </ul>
     * Pure infrastructure failures (session-loss, driver-allocation, app-crash) are left as broken
     * so their dedicated categories still apply. Only the root-level status is changed (it precedes
     * the {@code steps} array); the message is tagged for triage.
     */
    private void demoteBrokenResult(Path resultFile) {
        try {
            String json = Files.readString(resultFile, StandardCharsets.UTF_8);
            int statusIdx = json.indexOf("\"status\":\"broken\"");
            if (statusIdx < 0) {
                return; // not a broken result
            }
            String message = extractMessage(json);
            if (message == null || INFRA_BROKEN.matcher(message).find()) {
                return; // pure infra broken — keep its bucket
            }
            String marker;
            if (BACKEND_BROKEN.matcher(message).find()) {
                marker = "BACKEND DEFECT"; // real backend/service failure → product 'failed'
            } else if (LOCATOR_BROKEN.matcher(message).find()) {
                marker = "LOCATOR TIMEOUT"; // UI locator flakiness → clean 'failed'
            } else {
                return; // unattributed broken — leave as broken
            }
            String patched = json.substring(0, statusIdx)
                    + "\"status\":\"failed\""
                    + json.substring(statusIdx + "\"status\":\"broken\"".length());
            patched = injectHealthIntoMessage(patched, marker);
            Files.writeString(resultFile, patched, StandardCharsets.UTF_8);
            log.info("Demoted broken result to failed ({}): {}", marker, resultFile.getFileName());
        } catch (IOException e) {
            log.warn("Failed to demote {}: {}", resultFile.getFileName(), e.getMessage());
        }
    }

    /** Extract the root {@code statusDetails.message} value (handles escaped quotes). */
    private String extractMessage(String json) {
        String anchor = "\"message\":\"";
        int i = json.indexOf(anchor);
        if (i < 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int k = i + anchor.length(); k < json.length(); k++) {
            char c = json.charAt(k);
            if (c == '\\' && k + 1 < json.length()) {
                sb.append(c).append(json.charAt(++k));
                continue;
            }
            if (c == '"') {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
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
        for (String testName : failurePageSources.keySet()) {
            if (json.contains("\"name\":\"" + testName + "\"")) {
                return testName;
            }
        }
        for (String testName : failureCrashLogFiles.keySet()) {
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

    /**
     * True if the failure was already raised as a backend defect (a {@link
     * com.urpay.utils.BackendErrorException} anywhere in the cause chain, or a message that already
     * carries the outage phrase). Such failures are tagged by the broken→failed demoter, so the
     * global on-failure banner probe skips them to avoid a duplicate marker.
     */
    private boolean alreadyBackendError(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof com.urpay.utils.BackendErrorException) {
                return true;
            }
            String m = c.getMessage();
            if (m != null && (m.contains("currently unavailable") || m.contains("Transaction Declined"))) {
                return true;
            }
        }
        return false;
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
