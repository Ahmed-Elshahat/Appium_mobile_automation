package com.urpay.utils;

/**
 * Detects an Appium/cloud <b>session loss</b> — the driver session has quit, timed out or is no
 * longer known to the server — as opposed to an application crash.
 *
 * <p>Why this exists: when the session is dead, any {@code driver.queryAppState(...)} health probe
 * itself throws "Unable to find the session info for particular sessionId ... session has quit
 * already or timed out". Without this check that infra failure was mislabeled as
 * "APP CRASHED - App State: UNKNOWN", polluting the Allure report with false <em>broken</em> /
 * <em>unknown</em> results. A lost session is an infrastructure issue (LambdaTest idle timeout,
 * network drop, grid eviction), not a product defect — it must be categorized and retried, never
 * reported as an app crash.
 */
public final class SessionLoss {

    private SessionLoss() { }

    /** True if any throwable in the cause chain indicates the driver session is gone. */
    public static boolean isSessionLost(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (matches(c.getMessage()) || isSessionLossType(c)) {
                return true;
            }
        }
        return false;
    }

    /** True if the message text carries a known session-loss signature. */
    public static boolean matches(String message) {
        if (message == null) {
            return false;
        }
        String m = message.toLowerCase();
        return m.contains("unable to find the session info")
                || m.contains("session has quit")
                || m.contains("session is either terminated or not started")
                || m.contains("invalid session id")
                || m.contains("nosuchsession")
                || m.contains("session id is null")
                || m.contains("cannot find the requested resource")
                || (m.contains("session") && m.contains("timed out"));
    }

    private static boolean isSessionLossType(Throwable c) {
        String cls = c.getClass().getName().toLowerCase();
        return cls.contains("nosuchsession");
    }
}
