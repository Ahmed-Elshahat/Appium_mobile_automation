package com.urpay.utils;

import java.time.Duration;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;

/**
 * App-lifecycle guards that prevent the "many backs &rarr; app exits" problem in parallel runs.
 *
 * <p><b>Root cause:</b> on Android, pressing Back on the dashboard/root screen backgrounds
 * (exits) the app. Under parallel load screens render slowly, so blind/looping back presses
 * overshoot the intended screen, reach the dashboard, and the next Back exits the app — after
 * which every subsequent step in the test fails.
 *
 * <p>These helpers make Back <i>exit-safe</i>: never Back from the dashboard, and re-activate
 * the app if a Back press did background it.
 */
public final class AppGuard {

    private static final Logger log = LoggerFactory.getLogger(AppGuard.class);

    /** Dashboard balance widget — present only on the home screen. */
    private static final By DASHBOARD_MARKER =
            AppiumBy.accessibilityId("testID-master-amount-main");

    private AppGuard() {}

    private static String appPackage() {
        return ConfigManager.getInstance().get("appPackage", "com.urpay.consumer.sit");
    }

    /** Instant check (implicit wait 0) for whether the dashboard is currently showing. */
    public static boolean isOnDashboard(AppiumDriver driver) {
        long implicit = ConfigManager.getInstance().getInt("timeout", 10);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            return !driver.findElements(DASHBOARD_MARKER).isEmpty();
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicit));
        }
    }

    /** True if the URPay app is currently in the foreground. */
    public static boolean isInForeground(AppiumDriver driver) {
        try {
            if (driver instanceof InteractsWithApps app) {
                return app.queryAppState(appPackage()) == ApplicationState.RUNNING_IN_FOREGROUND;
            }
        } catch (Exception e) {
            log.debug("isInForeground check failed: {}", e.getMessage());
        }
        return true; // assume foreground if we cannot tell
    }

    /** Re-activate the app if a stray Back press backgrounded/exited it. */
    public static void ensureForeground(AppiumDriver driver) {
        try {
            if (driver instanceof InteractsWithApps app
                    && app.queryAppState(appPackage()) != ApplicationState.RUNNING_IN_FOREGROUND) {
                log.warn("App not in foreground (a Back press likely exited it) — re-activating {}",
                        appPackage());
                app.activateApp(appPackage());
            }
        } catch (Exception e) {
            log.debug("ensureForeground skipped: {}", e.getMessage());
        }
    }

    /**
     * Press Android Back ONCE, but never from the dashboard (Back there exits the app),
     * then re-activate the app if the press accidentally backgrounded it.
     *
     * @return {@code true} if Back was pressed; {@code false} if skipped because we are
     *         already on the dashboard (caller should stop backing).
     */
    public static boolean safeBack(AppiumDriver driver) {
        if (isOnDashboard(driver)) {
            log.info("On dashboard — skipping Back (would exit the app)");
            return false;
        }
        driver.navigate().back();
        ensureForeground(driver);
        return true;
    }
}
