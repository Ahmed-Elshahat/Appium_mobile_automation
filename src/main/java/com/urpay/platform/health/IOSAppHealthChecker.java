package com.urpay.platform.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.appmanagement.ApplicationState;
import io.appium.java_client.ios.IOSDriver;

/**
 * iOS-specific app health checker using XCUITest capabilities.
 */
public class IOSAppHealthChecker implements AppHealthChecker {

    private static final Logger log = LoggerFactory.getLogger(IOSAppHealthChecker.class);
    private final IOSDriver driver;
    private final String bundleId;

    public IOSAppHealthChecker(IOSDriver driver, String bundleId) {
        this.driver = driver;
        this.bundleId = bundleId;
    }

    @Override
    public boolean isAppInForeground() {
        try {
            ApplicationState state = driver.queryAppState(bundleId);
            return state == ApplicationState.RUNNING_IN_FOREGROUND;
        } catch (Exception e) {
            log.warn("Failed to query app state: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasAppCrashed() {
        try {
            ApplicationState state = driver.queryAppState(bundleId);
            return state == ApplicationState.NOT_RUNNING;
        } catch (Exception e) {
            // A dead/timed-out SESSION is not an app crash — never fabricate a crash from it.
            if (com.urpay.utils.SessionLoss.matches(e.getMessage())) {
                log.warn("Session lost while querying app state (infra, not a crash): {}", e.getMessage());
                return false;
            }
            log.warn("Failed to query app state: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getAppState() {
        try {
            ApplicationState state = driver.queryAppState(bundleId);
            return state.name();
        } catch (Exception e) {
            if (com.urpay.utils.SessionLoss.matches(e.getMessage())) {
                return "SESSION_LOST";
            }
            return "UNKNOWN (error: " + e.getMessage() + ")";
        }
    }

    /**
     * Not implemented for iOS: there is no logcat equivalent. iOS crash detection would read
     * the device crash reports (e.g. {@code mobile: getDeviceTime} + crash log retrieval), which
     * the cloud provider gates differently. Returns empty so callers fall back to the
     * state-based checks.
     */
    @Override
    public String findCrashSignature() {
        return "";
    }

    @Override
    public boolean recoverApp() {
        try {
            ApplicationState state = driver.queryAppState(bundleId);
            if (state == ApplicationState.RUNNING_IN_FOREGROUND) {
                return true;
            }
            driver.activateApp(bundleId);
            ApplicationState after = driver.queryAppState(bundleId);
            boolean recovered = after == ApplicationState.RUNNING_IN_FOREGROUND;
            log.info("App recovery attempt: {} → {}", state, after);
            return recovered;
        } catch (Exception e) {
            log.error("App recovery failed: {}", e.getMessage());
            return false;
        }
    }
}
