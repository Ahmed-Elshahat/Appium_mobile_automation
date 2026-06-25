package com.urpay.reporting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.core.ConfigManager;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

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

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Write Allure report metadata (Environment, Categories, Executor widgets) into the
     * results directory. Called once per suite from BaseTest so the report always reflects
     * the actual run configuration (env, profile, device, build).
     */
    public static void writeAllureMetadata() {
        Path dir = Paths.get(System.getProperty("allure.results.directory", "allure-results"));
        try {
            Files.createDirectories(dir);
            writeEnvironment(dir);
            copyCategories(dir);
            writeExecutor(dir);
            log.info("Allure metadata (environment, categories, executor) written to {}",
                    dir.toAbsolutePath());
        } catch (Exception e) {
            log.warn("Could not write Allure metadata: {}", e.getMessage());
        }
    }

    private static void writeEnvironment(Path dir) throws IOException {
        ConfigManager c = ConfigManager.getInstance();
        boolean remote = c.getBoolean("remote", false);
        String env =
                "Squad=Payments & Cards\n"
                + "Environment=" + c.getEnv() + "\n"
                + "Profile=" + c.getProfileName() + "\n"
                + "Platform=" + c.get("platform", "android") + "\n"
                + "Platform.Version=" + c.get("platformVersion", "n/a") + "\n"
                + "App.Package=" + c.get("appPackage", "n/a") + "\n"
                + "App.Build=" + c.get("lt.appUrl", c.get("app", "n/a")) + "\n"
                + "Execution=" + (remote ? "LambdaTest Cloud" : "Local Appium") + "\n"
                + "Device=" + c.get("deviceName", "n/a") + "\n"
                + "Hub=" + (remote ? c.get("lt.url", "n/a") : c.get("appiumUrl", "n/a")) + "\n"
                + "Framework=Appium + TestNG\n"
                + "Executed.On=" + LocalDateTime.now().format(TS) + "\n";
        Files.writeString(dir.resolve("environment.properties"), env, StandardCharsets.UTF_8);
    }

    private static void copyCategories(Path dir) throws IOException {
        try (InputStream in = ReportManager.class.getResourceAsStream("/categories.json")) {
            if (in != null) {
                Files.copy(in, dir.resolve("categories.json"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void writeExecutor(Path dir) throws IOException {
        ConfigManager c = ConfigManager.getInstance();
        boolean remote = c.getBoolean("remote", false);
        String json =
                "{\n"
                + "  \"name\": \"" + (remote ? "LambdaTest" : "Local Appium") + "\",\n"
                + "  \"type\": \"" + (remote ? "lambdatest" : "local") + "\",\n"
                + "  \"buildName\": \"P&C Squad - " + c.getProfileName() + " - "
                        + LocalDateTime.now().format(TS) + "\",\n"
                + "  \"reportName\": \"URPay Payments & Cards - Allure Report\"\n"
                + "}\n";
        Files.writeString(dir.resolve("executor.json"), json, StandardCharsets.UTF_8);
    }

    /**
     * No-op — Allure flushes automatically.
     * Kept for backward compatibility with BaseTest.
     */
    public static void flush() {
        log.info("Allure results saved to allure-results/");
    }
}
