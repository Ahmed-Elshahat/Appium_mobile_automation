package com.urpay.utils;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.AppiumDriver;

/**
 * SRP: Centralized explicit wait utility.
 * All waits in the framework MUST go through this class.
 *
 * Rules enforced:
 *   - No Thread.sleep() anywhere in the project.
 *   - Default timeout read from ConfigManager.
 *   - Every wait uses WebDriverWait + ExpectedConditions.
 */
public class WaitUtils {

    private static final Logger log = LoggerFactory.getLogger(WaitUtils.class);

    private final WebDriverWait wait;
    private final AppiumDriver driver;

    public WaitUtils(AppiumDriver driver, long timeoutInSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
    }

    // ── Visibility ─────────────────────────────────────────────────

    /**
     * Wait until element is visible on screen.
     *
     * @param element the WebElement to wait for
     * @return the visible WebElement
     */
    public WebElement waitForVisible(WebElement element) {
        log.debug("Waiting for element to be visible: {}", element);
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait until element located by locator is visible.
     *
     * @param locator the By locator
     * @return the visible WebElement
     */
    public WebElement waitForVisible(By locator) {
        log.debug("Waiting for element to be visible: {}", locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait until element is visible with custom timeout.
     *
     * @param element          the WebElement to wait for
     * @param timeoutInSeconds custom timeout override
     * @return the visible WebElement
     */
    public WebElement waitForVisible(WebElement element, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait until element located by locator is visible with custom timeout.
     *
     * @param locator          the By locator
     * @param timeoutInSeconds custom timeout override
     * @return the visible WebElement
     */
    public WebElement waitForVisible(By locator, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ── Clickable ──────────────────────────────────────────────────

    /**
     * Wait until element is clickable.
     *
     * @param element the WebElement to wait for
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(WebElement element) {
        log.debug("Waiting for element to be clickable: {}", element);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wait until element located by locator is clickable.
     *
     * @param locator the By locator
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(By locator) {
        log.debug("Waiting for element to be clickable: {}", locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait until element is clickable with custom timeout.
     *
     * @param element          the WebElement to wait for
     * @param timeoutInSeconds custom timeout override
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(WebElement element, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wait until element located by locator is clickable with custom timeout.
     *
     * @param locator          the By locator
     * @param timeoutInSeconds custom timeout override
     * @return the clickable WebElement
     */
    public WebElement waitForClickable(By locator, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ── Invisibility ───────────────────────────────────────────────

    /**
     * Wait until element is no longer visible.
     *
     * @param locator the By locator
     * @return true if element became invisible within timeout
     */
    public boolean waitForInvisible(By locator) {
        log.debug("Waiting for element to become invisible: {}", locator);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait until element is no longer visible with custom timeout.
     *
     * @param locator          the By locator
     * @param timeoutInSeconds custom timeout override
     * @return true if element became invisible within timeout
     */
    public boolean waitForInvisible(By locator, long timeoutInSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ── Text ───────────────────────────────────────────────────────

    /**
     * Wait until element contains expected text.
     *
     * @param element      the WebElement to check
     * @param expectedText the text to wait for
     * @return true if element contains the text within timeout
     */
    public boolean waitForText(WebElement element, String expectedText) {
        log.debug("Waiting for text '{}' in element", expectedText);
        return wait.until(ExpectedConditions.textToBePresentInElement(element, expectedText));
    }

    /**
     * Wait until element located by locator contains expected text.
     *
     * @param locator      the By locator
     * @param expectedText the text to wait for
     * @return true if element contains the text within timeout
     */
    public boolean waitForText(By locator, String expectedText) {
        log.debug("Waiting for text '{}' in element: {}", expectedText, locator);
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    // ── Presence ───────────────────────────────────────────────────

    /**
     * Wait until element is present in DOM (may not be visible).
     *
     * @param locator the By locator
     * @return the present WebElement
     */
    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait until all elements matching locator are present.
     *
     * @param locator the By locator
     * @return list of present WebElements
     */
    public List<WebElement> waitForAllPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
    }

    // ── Boolean checks (no exception on timeout) ──────────────────

    /**
     * Check if element is displayed within timeout — returns false instead of throwing.
     *
     * @param element          the WebElement to check
     * @param timeoutInSeconds timeout
     * @return true if visible, false if timeout
     */
    public boolean isDisplayed(WebElement element, long timeoutInSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.visibilityOf(element));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if element located by locator is present within timeout.
     *
     * @param locator          the By locator
     * @param timeoutInSeconds timeout
     * @return true if present, false if timeout
     */
    public boolean isPresent(By locator, long timeoutInSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Quick find — returns elements found within timeout, empty list if none.
     * Uses explicit WebDriverWait, never touches implicitlyWait.
     */
    public java.util.List<WebElement> findQuick(By locator, long timeoutInSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return driver.findElements(locator);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
