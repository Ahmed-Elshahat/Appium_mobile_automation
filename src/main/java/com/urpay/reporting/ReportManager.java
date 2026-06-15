package com.urpay.reporting;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

/**
 * SRP: Allure reporting utility.
 * Provides static convenience methods for logging steps and attaching screenshots.
 * All actual report generation is handled by Allure's TestNG listener automatically.
 *
 * Usage:
 *   ReportManager.info("Step description");
 *   ReportManager.pass("Verification passed");
 *   ReportManager.fail("Error occurred", screenshotBase64);
 *   ReportManager.attachScreenshot(screenshotBytes, "name");
 */
public final class ReportManager {

    private static final Logger log = LoggerFactory.getLogger(ReportManager.class);

    private ReportManager() {}

    /**
     * No-op — Allure doesn't need explicit init.
     * Kept for backward compatibility with BaseTest.
     */
    public static void initReports(String suiteName) {
        log.info("Allure reporting active for suite: {}", suiteName);
    }

    /**
     * No-op — Allure creates tests automatically via its TestNG listener.
     * Kept for backward compatibility with TestExecutionListener.
     */
    public static void createTest(String testName) {
        // Allure handles test creation automatically
    }

    public static void info(String message) {
        Allure.step(message);
        log.info(message);
    }

    public static void pass(String message) {
        Allure.step(message, Status.PASSED);
        log.info("✅ {}", message);
    }

    public static void fail(String message) {
        Allure.step(message, Status.FAILED);
        log.error("❌ {}", message);
    }

    public static void fail(String message, String screenshotBase64) {
        Allure.step(message, Status.FAILED);
        if (screenshotBase64 != null && !screenshotBase64.isEmpty()) {
            byte[] bytes = java.util.Base64.getDecoder().decode(screenshotBase64);
            Allure.addAttachment("Failure Screenshot", "image/png",
                    new ByteArrayInputStream(bytes), ".png");
        }
        log.error("❌ {}", message);
    }

    public static void skip(String message) {
        Allure.step(message, Status.SKIPPED);
        log.warn("⏭ {}", message);
    }

    public static void warning(String message) {
        Allure.step("⚠ " + message);
        log.warn(message);
    }

    /**
     * Attach a screenshot (byte array) to the current Allure test.
     */
    public static void attachScreenshot(byte[] screenshotBytes, String name) {
        if (screenshotBytes != null && screenshotBytes.length > 0) {
            Allure.addAttachment(name, "image/png",
                    new ByteArrayInputStream(screenshotBytes), ".png");
        }
    }

    /**
     * Attach a screenshot (Base64 string) to the current Allure test.
     */
    public static void attachScreenshot(String base64) {
        if (base64 != null && !base64.isEmpty()) {
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            Allure.addAttachment("Screenshot", "image/png",
                    new ByteArrayInputStream(bytes), ".png");
        }
    }

    /**
     * No-op — Allure flushes automatically.
     * Kept for backward compatibility with BaseTest.
     */
    public static void flush() {
        log.info("Allure results saved to allure-results/");
    }
}
