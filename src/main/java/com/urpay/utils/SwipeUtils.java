package com.urpay.utils;

import java.time.Duration;
import java.util.Collections;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.AppiumDriver;

/**
 * SRP: Swipe/scroll gesture utility using W3C Actions API.
 * No deprecated TouchAction — uses PointerInput + Sequence only.
 *
 * Swipe percentages are configurable via constructor (default 80% of screen).
 */
public class SwipeUtils {

    private static final Logger log = LoggerFactory.getLogger(SwipeUtils.class);

    private final AppiumDriver driver;
    private final double swipePercentage;
    private static final int SWIPE_DURATION_MS = 300;
    private static final int PRESS_DURATION_MS = 100;

    /**
     * Create SwipeUtils with default 80% swipe distance.
     *
     * @param driver the AppiumDriver instance
     */
    public SwipeUtils(AppiumDriver driver) {
        this(driver, 0.80);
    }

    /**
     * Create SwipeUtils with custom swipe distance percentage.
     *
     * @param driver          the AppiumDriver instance
     * @param swipePercentage fraction of screen to swipe (0.0 to 1.0)
     */
    public SwipeUtils(AppiumDriver driver, double swipePercentage) {
        this.driver = driver;
        this.swipePercentage = Math.max(0.1, Math.min(swipePercentage, 0.95));
    }

    // ── Directional Swipes ─────────────────────────────────────────

    /**
     * Swipe up (scroll content down).
     */
    public void swipeUp() {
        Dimension size = getScreenSize();
        int centerX = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * (0.5 + swipePercentage / 2));
        int endY = (int) (size.getHeight() * (0.5 - swipePercentage / 2));

        log.debug("Swipe UP: ({},{}) → ({},{})", centerX, startY, centerX, endY);
        performSwipe(centerX, startY, centerX, endY);
    }

    /**
     * Swipe down (scroll content up).
     */
    public void swipeDown() {
        Dimension size = getScreenSize();
        int centerX = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * (0.5 - swipePercentage / 2));
        int endY = (int) (size.getHeight() * (0.5 + swipePercentage / 2));

        log.debug("Swipe DOWN: ({},{}) → ({},{})", centerX, startY, centerX, endY);
        performSwipe(centerX, startY, centerX, endY);
    }

    /**
     * Swipe left (scroll content right).
     */
    public void swipeLeft() {
        Dimension size = getScreenSize();
        int centerY = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * (0.5 + swipePercentage / 2));
        int endX = (int) (size.getWidth() * (0.5 - swipePercentage / 2));

        log.debug("Swipe LEFT: ({},{}) → ({},{})", startX, centerY, endX, centerY);
        performSwipe(startX, centerY, endX, centerY);
    }

    /**
     * Swipe right (scroll content left).
     */
    public void swipeRight() {
        Dimension size = getScreenSize();
        int centerY = size.getHeight() / 2;
        int startX = (int) (size.getWidth() * (0.5 - swipePercentage / 2));
        int endX = (int) (size.getWidth() * (0.5 + swipePercentage / 2));

        log.debug("Swipe RIGHT: ({},{}) → ({},{})", startX, centerY, endX, centerY);
        performSwipe(startX, centerY, endX, centerY);
    }

    // ── Swipe to Element ───────────────────────────────────────────

    /**
     * Swipe up repeatedly until element is found or max attempts reached.
     *
     * @param locator     the By locator to search for
     * @param maxAttempts maximum number of swipes before giving up
     * @return the found WebElement
     * @throws org.openqa.selenium.NoSuchElementException if not found within max attempts
     */
    public WebElement swipeToElement(By locator, int maxAttempts) {
        log.info("Swiping to find element: {} (max {} attempts)", locator, maxAttempts);
        for (int i = 0; i < maxAttempts; i++) {
            try {
                WebElement element = driver.findElement(locator);
                if (element.isDisplayed()) {
                    log.debug("Element found after {} swipe(s)", i);
                    return element;
                }
            } catch (Exception e) {
                // Element not found yet — swipe and retry
            }
            swipeUp();
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Element not found after " + maxAttempts + " swipes: " + locator);
    }

    /**
     * Swipe to element with default 5 attempts.
     *
     * @param locator the By locator to search for
     * @return the found WebElement
     */
    public WebElement swipeToElement(By locator) {
        return swipeToElement(locator, 5);
    }

    // ── Coordinate-based tap ──────────────────────────────────────

    /**
     * Tap at specific screen coordinates using W3C Actions.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void tapAtCoordinates(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 0);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new Pause(finger, Duration.ofMillis(50)));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(tap));
    }

    // ── Core Swipe ─────────────────────────────────────────────────

    /**
     * Perform a swipe from point (startX, startY) to (endX, endY).
     *
     * @param startX start x-coordinate
     * @param startY start y-coordinate
     * @param endX   end x-coordinate
     * @param endY   end y-coordinate
     */
    public void performSwipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 0);

        swipe.addAction(finger.createPointerMove(
                Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(new Pause(finger, Duration.ofMillis(PRESS_DURATION_MS)));
        swipe.addAction(finger.createPointerMove(
                Duration.ofMillis(SWIPE_DURATION_MS), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }

    private Dimension getScreenSize() {
        return driver.manage().window().getSize();
    }
}