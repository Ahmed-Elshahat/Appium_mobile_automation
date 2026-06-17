package com.urpay.core;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.platform.MobilePlatformActions;
import com.urpay.platform.Platform;
import com.urpay.platform.PlatformActionsFactory;

import io.appium.java_client.AppiumDriver;

/**
 * Thread-safe Appium driver factory.
 *
 * SOLID applied:
 *   SRP — Only manages driver lifecycle. Cloud hooks extracted to CloudSessionManager.
 *   OCP — New providers added via DriverCreationStrategy, no modification needed.
 *   DIP — Implements DriverProvider interface for loose coupling.
 *
 * Replaces: Katalon MobileDriverFactory + MobileConfiguration.groovy
 */
public class DriverFactory implements DriverProvider {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<AppiumDriver> driverThread = new ThreadLocal<>();
    private static final ThreadLocal<MobilePlatformActions> actionsThread = new ThreadLocal<>();
    private static final ThreadLocal<Platform> platformThread = new ThreadLocal<>();

    // Registry of all drivers across threads for reliable parallel cleanup
    private static final Map<Long, AppiumDriver> allDrivers = new ConcurrentHashMap<>();

    // OCP: Strategy registry — add new providers without modifying this class
    private static final Map<String, DriverCreationStrategy> strategies = new ConcurrentHashMap<>();
    private static final DriverFactory INSTANCE = new DriverFactory();

    static {
        strategies.put("android", new LocalAndroidDriverStrategy());
        strategies.put("ios", new LocalIOSDriverStrategy());
        strategies.put("remote", new LambdaTestDriverStrategy());
    }

    private DriverFactory() {}

    public static DriverFactory getInstance() {
        return INSTANCE;
    }

    /**
     * OCP: Register a custom driver creation strategy at runtime.
     * e.g. registerStrategy("browserstack", new BrowserStackDriverStrategy());
     */
    public static void registerStrategy(String name, DriverCreationStrategy strategy) {
        strategies.put(name, strategy);
    }

    // ── DriverProvider implementation ──────────────────────────────

    @Override
    public void initDriver() {
        ConfigManager config = ConfigManager.getInstance();
        boolean isRemote = config.getBoolean("remote", false);
        String platform = config.get("platform", "android");

        String strategyKey = isRemote ? "remote" : platform.toLowerCase();
        DriverCreationStrategy strategy = strategies.get(strategyKey);

        if (strategy == null) {
            throw new IllegalStateException("No driver strategy registered for: " + strategyKey);
        }

        log.info("Creating driver with strategy: {}", strategyKey);
        AppiumDriver driver = strategy.createDriver(config);

        int timeout = config.getInt("timeout", 10);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));

        driverThread.set(driver);
        allDrivers.put(Thread.currentThread().getId(), driver);
        actionsThread.set(PlatformActionsFactory.create(driver));
        platformThread.set(PlatformActionsFactory.detectPlatform(driver));
        log.info("Driver initialized. Session: {}", driver.getSessionId());
    }

    @Override
    public AppiumDriver getDriver() {
        AppiumDriver driver = driverThread.get();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized. Call initDriver() first.");
        }
        return driver;
    }

    @Override
    public void quitDriver() {
        AppiumDriver driver = driverThread.get();
        if (driver != null) {
            try {
                log.info("Quitting driver. Session: {}", driver.getSessionId());
                driver.quit();
            } catch (Exception e) {
                log.warn("Error quitting driver: {}", e.getMessage());
            } finally {
                allDrivers.remove(Thread.currentThread().getId());
                driverThread.remove();
                actionsThread.remove();
                platformThread.remove();
            }
        }
    }

    /**
     * Quit all drivers across all threads. Use at suite teardown to prevent session leaks.
     */
    public void quitAllDrivers() {
        for (Map.Entry<Long, AppiumDriver> entry : allDrivers.entrySet()) {
            try {
                AppiumDriver driver = entry.getValue();
                if (driver != null) {
                    log.info("Quitting driver for thread {}. Session: {}", entry.getKey(), driver.getSessionId());
                    driver.quit();
                }
            } catch (Exception e) {
                log.warn("Error quitting driver for thread {}: {}", entry.getKey(), e.getMessage());
            }
        }
        allDrivers.clear();
        driverThread.remove();
        actionsThread.remove();
        platformThread.remove();
    }

    @Override
    public boolean isDriverActive() {
        AppiumDriver driver = driverThread.get();
        if (driver == null) return false;
        try {
            return driver.getSessionId() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get platform-specific actions for the current thread's driver.
     */
    public MobilePlatformActions getPlatformActions() {
        MobilePlatformActions actions = actionsThread.get();
        if (actions == null) {
            throw new IllegalStateException("PlatformActions not initialized. Call initDriver() first.");
        }
        return actions;
    }

    /**
     * Get the current platform for the active driver.
     */
    public Platform getPlatform() {
        Platform p = platformThread.get();
        return p != null ? p : Platform.ANDROID;
    }

    /**
     * Check if running on Android.
     */
    public boolean isAndroid() {
        return getPlatform().isAndroid();
    }

    /**
     * Check if running on iOS.
     */
    public boolean isIOS() {
        return getPlatform().isIOS();
    }
}
