package com.urpay.utils;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SRP: Screenshot capture utility — capture only, no reporting logic.
 * Saves to target/screenshots/ with timestamped filename.
 * Returns File for Allure/ExtentReports attachment by the caller.
 */
public final class ScreenshotUtils {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "target/screenshots";

    private ScreenshotUtils() {}

    /**
     * Take a screenshot and save to disk.
     *
     * @param driver   the AppiumDriver instance
     * @param testName name to include in the filename
     * @return the saved screenshot File, or null if capture failed
     */
    public static File takeScreenshot(AppiumDriver driver, String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
            String safeName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = safeName + "_" + timestamp + ".png";

            Path destDir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(destDir);

            Path destPath = destDir.resolve(fileName);
            Files.copy(srcFile.toPath(), destPath);

            log.info("Screenshot saved: {}", destPath);
            return destPath.toFile();

        } catch (IOException e) {
            log.error("Failed to save screenshot for '{}': {}", testName, e.getMessage());
            return null;
        }
    }

    /**
     * Take a screenshot and return as Base64 string (for inline report embedding).
     *
     * @param driver the AppiumDriver instance
     * @return Base64-encoded screenshot string, or empty string if capture failed
     */
    public static String takeScreenshotAsBase64(AppiumDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            log.error("Failed to capture screenshot as Base64: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Take a screenshot and return as raw bytes (for Allure attachment).
     *
     * @param driver the AppiumDriver instance
     * @return screenshot bytes, or empty array if capture failed
     */
    public static byte[] takeScreenshotAsBytes(AppiumDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to capture screenshot as bytes: {}", e.getMessage());
            return new byte[0];
        }
    }
}
