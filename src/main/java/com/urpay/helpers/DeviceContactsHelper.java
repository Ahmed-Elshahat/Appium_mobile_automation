package com.urpay.helpers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;

/**
 * Best-effort device-contact seeding for contact-picker flows (e.g. Send Gift "Cash", which picks
 * the recipient from the device's phone contacts).
 *
 * <p>Runs entirely on the cloud (LambdaTest) — it fires an {@code ACTION_INSERT} intent into the
 * phone's native Contacts editor via {@code mobile: startActivity} (a standard command, NOT the
 * relaxed-security {@code mobile: shell} which LambdaTest blocks), pre-fills name + number, taps
 * Save, then returns to the app under test. Best-effort: swallows all errors so it never fails the
 * flow, and re-activates the app in a {@code finally} block.
 */
public final class DeviceContactsHelper {

    private static final Logger log = LoggerFactory.getLogger(DeviceContactsHelper.class);

    // Save / done buttons in the native Contacts editor (cross-OEM: AOSP/Pixel + Samsung).
    private static final org.openqa.selenium.By SAVE_BTN = AppiumBy.xpath(
            "//*[@text='Save' or @content-desc='Save'"
            + " or contains(@resource-id,'save') or contains(@resource-id,'menu_done')]");
    // Optional "Save contact to" account picker — pick a local/phone account so Save is enabled.
    private static final org.openqa.selenium.By ACCOUNT_OPTION = AppiumBy.xpath(
            "//*[@text='Phone' or @text='Device' or @text='Phone contact'"
            + " or @text='Save to device' or @text='Save to phone']");

    private DeviceContactsHelper() {
    }

    /**
     * Ensure a device phone contact with the given display name and number exists. Best-effort.
     *
     * @param driver      the active Appium driver
     * @param appPackage  the app-under-test package to re-activate afterwards
     * @param displayName the contact display name (avoid spaces — passed as an am start extra)
     * @param phoneNumber the contact phone number (store it in the form the picker searches by)
     */
    public static void ensureContact(AppiumDriver driver, String appPackage, String displayName,
            String phoneNumber) {
        // Make sure the app can actually READ contacts, otherwise the picker shows an empty list
        // even after the contact is seeded. Server-side `pm grant` (allowed on LambdaTest).
        grantContactsPermission(driver, appPackage);
        try {
            // Open the native "create contact" editor pre-filled via ACTION_INSERT. This uses the
            // server-side adb (allowed on LambdaTest), unlike the blocked mobile: shell.
            // UiAutomator2 mobile: startActivity schema uses action / mimeType / extras.
            Map<String, Object> args = new HashMap<>();
            args.put("action", "android.intent.action.INSERT");
            args.put("mimeType", "vnd.android.cursor.dir/contact");
            args.put("extras", List.of(
                    List.of("s", "name", displayName),
                    List.of("s", "phone", phoneNumber)));
            args.put("wait", true);
            driver.executeScript("mobile: startActivity", args);

            // Some OEMs prompt "Save contact to <account>" first — pick a local/phone account.
            tapIfPresent(driver, ACCOUNT_OPTION, 3);
            // Save the contact.
            if (!tapIfPresent(driver, SAVE_BTN, 8)) {
                log.warn("Contacts Save button not found — contact may not have been saved");
            }
            log.info("Seeded device contact '{}' -> {}", displayName, phoneNumber);
        } catch (Exception e) {
            log.warn("ensureContact failed (best-effort, continuing): {}", e.getMessage());
        } finally {
            // Return to the app under test regardless of outcome.
            try {
                Map<String, Object> act = new HashMap<>();
                act.put("appId", appPackage);
                driver.executeScript("mobile: activateApp", act);
            } catch (Exception e) {
                log.warn("Could not re-activate {} after contact seeding: {}", appPackage,
                        e.getMessage());
            }
        }
    }

    /**
     * Grant the app the contacts read/write permissions so its picker can see seeded contacts.
     * Best-effort — uses server-side {@code pm grant} (allowed on LambdaTest), never fails the flow.
     */
    private static void grantContactsPermission(AppiumDriver driver, String appPackage) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("appPackage", appPackage);
            args.put("action", "grant");
            args.put("permissions", List.of(
                    "android.permission.READ_CONTACTS",
                    "android.permission.WRITE_CONTACTS"));
            driver.executeScript("mobile: changePermissions", args);
            log.info("Granted READ/WRITE_CONTACTS to {}", appPackage);
        } catch (Exception e) {
            log.warn("Could not grant contacts permission to {} (best-effort): {}", appPackage,
                    e.getMessage());
        }
    }

    /**
     * Restart the app under test so a launch-time device-contacts read picks up a newly-seeded
     * contact (the app caches an empty contact list at first launch). Best-effort.
     */
    public static void restartApp(AppiumDriver driver, String appPackage) {
        try {
            Map<String, Object> id = new HashMap<>();
            id.put("appId", appPackage);
            driver.executeScript("mobile: terminateApp", id);
            driver.executeScript("mobile: activateApp", id);
            log.info("Restarted {} to refresh device contacts", appPackage);
        } catch (Exception e) {
            log.warn("Could not restart {} after contact seeding: {}", appPackage, e.getMessage());
        }
    }

    private static boolean tapIfPresent(AppiumDriver driver, org.openqa.selenium.By locator,
            int timeoutSec) {
        Duration original = driver.manage().timeouts().getImplicitWaitTimeout();
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeoutSec));
            List<WebElement> els = driver.findElements(locator);
            if (!els.isEmpty()) {
                els.get(0).click();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            driver.manage().timeouts().implicitlyWait(original);
        }
    }
}
