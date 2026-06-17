package com.urpay.platform.health;

/**
 * Platform-neutral app health checker.
 * Detects app crashes, background state, and session issues.
 *
 * Implementations:
 *   - AndroidAppHealthChecker: Uses activity state + process check
 *   - IOSAppHealthChecker: Uses app state query via XCUITest
 */
public interface AppHealthChecker {

    /**
     * Check if the app is currently in the foreground.
     */
    boolean isAppInForeground();

    /**
     * Check if the app has crashed (process not running).
     */
    boolean hasAppCrashed();

    /**
     * Get current app state as a descriptive string.
     */
    String getAppState();

    /**
     * Attempt to recover the app to foreground.
     * @return true if recovery successful
     */
    boolean recoverApp();
}
