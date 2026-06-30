package com.urpay.utils;

/**
 * Thrown when the app under test is detected to have crashed (process gone) during a test.
 *
 * <p>The message always starts with {@code "APP CRASHED"} so the Allure defect categoriser
 * ({@code src/test/resources/categories.json}) buckets these under "App crashed / not in
 * foreground" instead of mislabeling them as generic element timeouts. Unchecked so it can
 * surface from any wait without checked-exception plumbing.
 */
public class AppCrashException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AppCrashException(String message) {
        super(message);
    }
}
