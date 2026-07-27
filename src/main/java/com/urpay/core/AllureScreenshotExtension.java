package com.urpay.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.TestResult;

import com.urpay.utils.ScreenshotUtils;

/**
 * Allure SPI extension that captures failure screenshots at the CORRECT lifecycle moment.
 *
 * <p><b>Why this exists:</b> Neither post-hoc JSON patching (onFinish) nor @AfterMethod work:
 * <ul>
 *   <li>onFinish JSON patching — Allure adapter overwrites patches after they're applied</li>
 *   <li>@AfterMethod — Allure creates a fixture context; Allure.addAttachment() goes to the
 *       fixture, not the test case</li>
 * </ul>
 *
 * <p><b>How this works:</b> {@code beforeTestStop()} is called by the Allure lifecycle INSIDE
 * {@code stopTestCase()}, which the Allure TestNG adapter invokes from its own
 * {@code onTestFailure()}. At this point:
 * <ul>
 *   <li>The test status is already set (FAILED/BROKEN)</li>
 *   <li>We're on the TEST THREAD → ThreadLocal DriverFactory is available</li>
 *   <li>The result hasn't been written to disk yet → our attachments are included</li>
 *   <li>No fixture context interference</li>
 * </ul>
 *
 * <p>Registered via SPI: META-INF/services/io.qameta.allure.listener.TestLifecycleListener
 */
public class AllureScreenshotExtension implements TestLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(AllureScreenshotExtension.class);

    @Override
    public void beforeTestStop(TestResult result) {
        if (result.getStatus() != Status.FAILED && result.getStatus() != Status.BROKEN) {
            return;
        }

        String testName = result.getName();
        log.info("📸 Capturing failure screenshot for: {} [status={}]", testName, result.getStatus());

        try {
            if (!DriverFactory.getInstance().isDriverActive()) {
                log.warn("Driver not active — attaching failure details as text for: {}", testName);
                attachText(result, "Failure Details (no driver)",
                        "Screenshot unavailable — driver not active.\n\nTest: " + testName
                                + "\nStatus: " + result.getStatus());
                return;
            }

            AppiumDriver driver = DriverFactory.getInstance().getDriver();

            // Capture screenshot
            byte[] screenshot = ScreenshotUtils.takeScreenshotAsBytes(driver);
            if (screenshot.length == 0) {
                // Retry once (transient connection issues)
                log.debug("Screenshot attempt 1 returned 0 bytes for {}, retrying...", testName);
                try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                screenshot = ScreenshotUtils.takeScreenshotAsBytes(driver);
            }

            if (screenshot.length > 0) {
                attachScreenshot(result, "Failure Screenshot", screenshot);
                log.info("✅ Screenshot attached for: {}", testName);
            } else {
                log.warn("⚠ Screenshot 0 bytes for {} — attaching text explanation", testName);
                attachText(result, "Screenshot Unavailable",
                        "Screenshot capture returned 0 bytes (FLAG_SECURE or connection issue).\nTest: " + testName);
            }

            // Page source (UI hierarchy) — critical for element timeout triage
            try {
                String pageSource = driver.getPageSource();
                if (pageSource != null && !pageSource.isEmpty()) {
                    attachPageSource(result, "Page Source (UI hierarchy)", pageSource);
                }
            } catch (Exception psEx) {
                log.debug("Page source capture failed for {}: {}", testName, psEx.getMessage());
            }

        } catch (Exception e) {
            log.warn("Screenshot capture failed for {}: {}", testName, e.getMessage());
            attachText(result, "Failure Details (capture error)",
                    "Screenshot capture failed: " + e.getClass().getSimpleName() + ": " + e.getMessage()
                            + "\n\nTest: " + testName);
        }
    }

    private void attachScreenshot(TestResult result, String name, byte[] data) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        String source = lifecycle.prepareAttachment(name, "image/png", "png");
        lifecycle.writeAttachment(source, new ByteArrayInputStream(data));
        result.getAttachments().add(new Attachment()
                .setName(name)
                .setType("image/png")
                .setSource(source));
    }

    private void attachPageSource(TestResult result, String name, String pageSource) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        String source = lifecycle.prepareAttachment(name, "text/xml", "xml");
        lifecycle.writeAttachment(source, new ByteArrayInputStream(
                pageSource.getBytes(StandardCharsets.UTF_8)));
        result.getAttachments().add(new Attachment()
                .setName(name)
                .setType("text/xml")
                .setSource(source));
    }

    private void attachText(TestResult result, String name, String content) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        String source = lifecycle.prepareAttachment(name, "text/plain", "txt");
        lifecycle.writeAttachment(source, new ByteArrayInputStream(
                content.getBytes(StandardCharsets.UTF_8)));
        result.getAttachments().add(new Attachment()
                .setName(name)
                .setType("text/plain")
                .setSource(source));
    }
}
