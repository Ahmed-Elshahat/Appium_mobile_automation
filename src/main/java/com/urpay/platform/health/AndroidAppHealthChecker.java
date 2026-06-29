package com.urpay.platform.health;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;

/**
 * Android-specific app health checker using UiAutomator2 capabilities.
 */
public class AndroidAppHealthChecker implements AppHealthChecker {

    private static final Logger log = LoggerFactory.getLogger(AndroidAppHealthChecker.class);

    /** Cap the captured crash block so a huge logcat dump never bloats the report. */
    private static final int MAX_CRASH_LINES = 60;
    private static final int MAX_CRASH_CHARS = 6000;

    private final AndroidDriver driver;
    private final String appPackage;

    public AndroidAppHealthChecker(AndroidDriver driver, String appPackage) {
        this.driver = driver;
        this.appPackage = appPackage;
    }

    @Override
    public boolean isAppInForeground() {
        try {
            ApplicationState state = driver.queryAppState(appPackage);
            return state == ApplicationState.RUNNING_IN_FOREGROUND;
        } catch (Exception e) {
            log.warn("Failed to query app state: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasAppCrashed() {
        try {
            ApplicationState state = driver.queryAppState(appPackage);
            return state == ApplicationState.NOT_RUNNING;
        } catch (Exception e) {
            log.warn("Failed to query app state: {}", e.getMessage());
            return true;
        }
    }

    @Override
    public String getAppState() {
        try {
            ApplicationState state = driver.queryAppState(appPackage);
            return state.name();
        } catch (Exception e) {
            return "UNKNOWN (error: " + e.getMessage() + ")";
        }
    }

    /**
     * Scan the captured logcat buffer for a crash/ANR of the app under test.
     *
     * Catches the case a point-in-time state poll cannot: the app crashes (e.g. after a tap),
     * Android relaunches it in the background, and by the time the test fails the process is
     * RUNNING_IN_BACKGROUND again (so {@link #hasAppCrashed()} reads false). The logcat record
     * of the FATAL EXCEPTION / ANR / process death persists across the restart.
     *
     * Requires logcat capture to be enabled on the session (skipLogcatCapture=false).
     */
    @Override
    public String findCrashSignature() {
        LogEntries entries;
        try {
            entries = driver.manage().logs().get("logcat");
        } catch (Exception e) {
            log.debug("logcat unavailable for crash scan: {}", e.getMessage());
            return "";
        }
        if (entries == null) {
            return "";
        }

        List<String> hits = new ArrayList<>();
        boolean inFatalBlock = false;
        for (LogEntry entry : entries) {
            String line = entry.getMessage();
            if (line == null || line.isEmpty()) {
                continue;
            }

            // Start of a Java/Kotlin crash block (own package confirmed by the Process: line later).
            if (line.contains("FATAL EXCEPTION")) {
                inFatalBlock = true;
                hits.add(line.trim());
                continue;
            }
            // Continuation of a crash block — AndroidRuntime carries the stack trace lines.
            if (inFatalBlock) {
                if (line.contains("AndroidRuntime")) {
                    hits.add(line.trim());
                    continue;
                }
                inFatalBlock = false; // block ended
            }

            // Process death / ANR / native crash markers scoped to our package.
            boolean packageScoped =
                    (line.contains("ANR in") && line.contains(appPackage))
                    || (line.contains(appPackage) && line.contains("has died"))
                    || (line.contains("am_crash") && line.contains(appPackage))
                    || line.contains(">>> " + appPackage + " <<<")
                    || (line.contains("Force finishing activity") && line.contains(appPackage));
            if (packageScoped) {
                hits.add(line.trim());
            }
        }

        if (hits.isEmpty()) {
            return "";
        }

        // Only keep a Java crash block if it references our package (filters out other apps' crashes).
        boolean ownPackage = hits.stream().anyMatch(h -> h.contains(appPackage));
        if (!ownPackage) {
            log.debug("logcat crash signatures found but none reference {}; ignoring", appPackage);
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size() && i < MAX_CRASH_LINES; i++) {
            if (sb.length() + hits.get(i).length() + 1 > MAX_CRASH_CHARS) {
                break;
            }
            sb.append(hits.get(i)).append('\n');
        }
        return sb.toString().trim();
    }

    @Override
    public boolean recoverApp() {
        try {
            ApplicationState state = driver.queryAppState(appPackage);
            if (state == ApplicationState.RUNNING_IN_FOREGROUND) {
                return true;
            }
            driver.activateApp(appPackage);
            ApplicationState after = driver.queryAppState(appPackage);
            boolean recovered = after == ApplicationState.RUNNING_IN_FOREGROUND;
            log.info("App recovery attempt: {} → {}", state, after);
            return recovered;
        } catch (Exception e) {
            log.error("App recovery failed: {}", e.getMessage());
            return false;
        }
    }
}
