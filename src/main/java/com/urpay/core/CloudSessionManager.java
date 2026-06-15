package com.urpay.core;

import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRP: Manages cloud session hooks (LambdaTest, BrowserStack, etc.).
 * Extracted from DriverFactory — driver creation and cloud hooks are separate concerns.
 */
public final class CloudSessionManager {

    private static final Logger log = LoggerFactory.getLogger(CloudSessionManager.class);

    private CloudSessionManager() {}

    /**
     * Update test name in cloud dashboard.
     */
    public static void updateTestName(String testName) {
        if (!isRemoteExecution()) return;
        executeScript("lambda-name=" + testName);
    }

    /**
     * Update test status in cloud dashboard.
     */
    public static void updateStatus(String status) {
        if (!isRemoteExecution()) return;
        executeScript("lambda-status=" + status);
    }

    private static boolean isRemoteExecution() {
        return ConfigManager.getInstance().getBoolean("remote", false);
    }

    private static void executeScript(String script) {
        try {
            AppiumDriver driver = DriverFactory.getInstance().getDriver();
            driver.executeScript(script);
        } catch (Exception e) {
            log.debug("Cloud script skipped (not on cloud?): {}", e.getMessage());
        }
    }
}
