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
     * Scan the device log for a crash/ANR signature of the app under test.
     *
     * Unlike {@link #hasAppCrashed()} (a point-in-time process-state poll), this survives an
     * auto-restart: a crash that bounces to home and is immediately relaunched leaves a
     * permanent, timestamped record in the device log even though the process is alive again
     * by the time the failure is observed.
     *
     * @return the matched crash evidence (FATAL EXCEPTION stack / ANR / process-death lines),
     *         or an empty string if no crash signature is present or logs are unavailable.
     */
    String findCrashSignature();

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
