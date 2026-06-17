package com.urpay.platform.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;

/**
 * Android-specific app health checker using UiAutomator2 capabilities.
 */
public class AndroidAppHealthChecker implements AppHealthChecker {

    private static final Logger log = LoggerFactory.getLogger(AndroidAppHealthChecker.class);
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
