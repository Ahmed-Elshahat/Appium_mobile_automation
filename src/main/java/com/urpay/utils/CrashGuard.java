package com.urpay.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.platform.health.AppHealthChecker;
import com.urpay.platform.health.AppHealthCheckerFactory;

import io.appium.java_client.AppiumDriver;

/**
 * Minimal crash assertion. Turns a silent app crash into a fast, categorised failure.
 *
 * <p><b>Cost:</b> {@link #assertAppAlive(AppiumDriver)} performs one {@code queryAppState}
 * round-trip, so it is only meant to be called on paths that have <i>already</i> failed (a wait
 * timed out) or at explicit checkpoints — never per-tap on the happy path. On a passing run it is
 * never invoked, so there is zero overhead.
 */
public final class CrashGuard {

    private static final Logger log = LoggerFactory.getLogger(CrashGuard.class);

    private CrashGuard() {}

    /**
     * If the app process is not running, throw a categorised {@link AppCrashException}; otherwise
     * return quietly. Never throws anything other than {@link AppCrashException}, so it is safe to
     * call from a catch block without masking the original error when the app is actually fine.
     *
     * @param driver the active Appium driver
     */
    public static void assertAppAlive(AppiumDriver driver) {
        if (driver == null) {
            return;
        }
        boolean crashed;
        String state;
        try {
            AppHealthChecker checker = AppHealthCheckerFactory.create(driver);
            crashed = checker.hasAppCrashed();
            state = checker.getAppState();
        } catch (Exception probeFailure) {
            // Could not determine state (e.g. dead session) — don't mask the caller's real error.
            log.debug("Crash check skipped: {}", probeFailure.getMessage());
            return;
        }
        if (crashed) {
            throw new AppCrashException("APP CRASHED: app process not running | App State: " + state);
        }
    }
}
