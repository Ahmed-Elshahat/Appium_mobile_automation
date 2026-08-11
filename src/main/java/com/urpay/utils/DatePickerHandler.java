package com.urpay.utils;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.AppiumDriver;

/**
 * Native Android date-spinner handler.
 *
 * Migrated from Katalon {@code com.uspace.calendar.handler.CalendarHandler.selectDate(month, day,
 * year)} used by {@code ResetPasscode/ForgotPasscode/UserVerificationScreen/enterVallidDOBAndNavigateToNextScreen}.
 *
 * The Date-Of-Birth field opens the native Android {@code DatePickerDialog} in spinner mode (its
 * pickers live under {@code resource-id 'android:id/pickers'}). It exposes three
 * {@code android.widget.NumberPicker}s — {@code [1]=Month}, {@code [2]=Day}, {@code [3]=Year} —
 * each with an inner {@code android.widget.EditText} showing the current value. A value is set by
 * swiping the picker up (next/increase) or down (previous/decrease) until the EditText matches.
 *
 * Rules: ZERO Thread.sleep — the W3C swipe gesture carries its own press/move duration and the
 * value is re-read after the synchronous {@code driver.perform}; no assertions (caller verifies).
 */
public class DatePickerHandler {

    private static final Logger log = LoggerFactory.getLogger(DatePickerHandler.class);

    private static final List<String> MONTHS = Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    private static final int MONTH_PICKER = 1;
    private static final int DAY_PICKER = 2;
    private static final int YEAR_PICKER = 3;
    private static final int MAX_SWIPES = 40;

    private final AppiumDriver driver;
    private final WaitUtils waits;
    private final SwipeUtils swipes;

    public DatePickerHandler(AppiumDriver driver) {
        this.driver = driver;
        this.waits = new WaitUtils(driver, 10);
        this.swipes = new SwipeUtils(driver);
    }

    /**
     * Select the given date on the open native spinner.
     *
     * @param month three-letter or full month label as shown by the picker (e.g. {@code "May"})
     * @param day   day of month (e.g. {@code "30"})
     * @param year  four-digit year (e.g. {@code "1994"})
     */
    public void selectDate(String month, String day, String year) {
        // Wait for the spinner to be present before driving it.
        boolean spinnerPresent = waits.isPresent(By.xpath(pickerXpath(MONTH_PICKER) + "/android.widget.EditText"), 5);

        if (!spinnerPresent) {
            log.warn("Native date picker is not in spinner mode; trying calendar-grid fallback");
            selectDateFromCalendarGrid(month, day, year);
            return;
        }

        // Mirror Katalon order: year, then month, then day.
        swipeUntilValue(YEAR_PICKER, year, PickerType.NUMBER);
        swipeUntilValue(MONTH_PICKER, month, PickerType.MONTH);
        swipeUntilValue(DAY_PICKER, day, PickerType.NUMBER);

        log.info("Date spinner set to {} {} {} (current: {} {} {})",
                month, day, year,
                getCurrentValue(MONTH_PICKER), getCurrentValue(DAY_PICKER), getCurrentValue(YEAR_PICKER));
    }

    /**
     * Fallback path for Android calendar-style pickers where NumberPicker is absent.
     * Selects day cell by visible text and leaves month/year navigation to the app's default view.
     */
    private void selectDateFromCalendarGrid(String month, String day, String year) {
        String normalizedDay = String.valueOf(Integer.parseInt(day.trim()));
        String monthYear = month.trim() + " " + year.trim();

        By dayCell = By.xpath(
            "//android.view.View[@text='" + normalizedDay + "']"
            + " | //android.widget.TextView[@text='" + normalizedDay + "']"
            + " | //android.view.View[@content-desc='" + normalizedDay + "']"
            + " | //android.widget.TextView[@content-desc='" + normalizedDay + "']"
            + " | //android.view.View[contains(@content-desc,'" + normalizedDay + "')"
            + " and contains(@content-desc,'" + monthYear + "')]"
            + " | //android.widget.TextView[contains(@content-desc,'" + normalizedDay + "')"
            + " and contains(@content-desc,'" + monthYear + "')]");

        if (!waits.isPresent(dayCell, 8)) {
            throw new IllegalStateException("Calendar-grid fallback could not find day cell '" + normalizedDay
                    + "' for target date " + month + " " + day + " " + year);
        }

        try {
            List<WebElement> cells = driver.findElements(dayCell);
            for (WebElement cell : cells) {
                try {
                    if (cell.isDisplayed() && cell.isEnabled()) {
                        cell.click();
                        log.info("Calendar-grid fallback selected day '{}'", normalizedDay);
                        return;
                    }
                } catch (Exception ignored) {
                    // try next candidate
                }
            }
            throw new IllegalStateException("Calendar-grid fallback found day cell but could not click it: "
                    + normalizedDay);
        } catch (WebDriverException e) {
            throw failFastSessionLoss("selecting day cell in calendar-grid fallback", e);
        }
    }

    private enum PickerType { MONTH, NUMBER }

    private void swipeUntilValue(int pickerIndex, String target, PickerType type) {
        String current = readStableValue(pickerIndex);
        int tries = 0;
        // Fraction of the picker height travelled per swipe. Start small so each swipe nudges ~one
        // item (a large swipe flings several items and overshoots, which made the month oscillate
        // between May/Jun). Adapt: grow it when a swipe produced no movement, shrink it on overshoot.
        double travel = 0.20;
        while (!current.equalsIgnoreCase(target) && tries < MAX_SWIPES) {
            int cmp = compareValue(current, target, type);
            if (cmp == 0) {
                break;
            }
            boolean increase = cmp < 0;
            try {
                swipePicker(pickerIndex, increase, travel);
            } catch (WebDriverException e) {
                throw failFastSessionLoss("swiping picker " + pickerIndex, e);
            }
            String updated;
            try {
                updated = readStableValue(pickerIndex);
            } catch (WebDriverException e) {
                throw failFastSessionLoss("reading picker " + pickerIndex, e);
            }
            if (updated.equals(current)) {
                // No movement — the swipe was too short; lengthen it.
                travel = Math.min(travel * 1.6, 0.6);
            } else {
                int newCmp = compareValue(updated, target, type);
                // Overshoot (we crossed the target) — shorten the swipe for a finer approach.
                if (newCmp != 0 && Integer.signum(newCmp) != Integer.signum(cmp)) {
                    travel = Math.max(travel * 0.5, 0.08);
                }
                current = updated;
            }
            tries++;
        }
        if (!current.equalsIgnoreCase(target)) {
            log.warn("Could not set picker {} to '{}' after {} swipe(s); current '{}'",
                    pickerIndex, target, tries, current);
        }
    }

    /**
     * Compare the current picker value to the target. Returns a negative number when the current
     * value is "below" the target (needs to increase), positive when "above", zero when equal.
     */
    private int compareValue(String current, String target, PickerType type) {
        try {
            if (type == PickerType.MONTH) {
                return Integer.compare(indexOfMonth(current), indexOfMonth(target));
            }
            return Integer.compare(Integer.parseInt(current.trim()), Integer.parseInt(target.trim()));
        } catch (RuntimeException e) {
            log.warn("Compare fallback for picker value '{}' → '{}': {}", current, target, e.getMessage());
            return -1; // default to increasing
        }
    }

    /**
     * Read the picker value, polling until two consecutive reads agree so the value is not sampled
     * mid-fling (which would corrupt the direction decision). Bounded — no Thread.sleep.
     */
    private String readStableValue(int pickerIndex) {
        String last = getCurrentValue(pickerIndex);
        for (int i = 0; i < 4; i++) {
            String v = getCurrentValue(pickerIndex);
            if (!v.isEmpty() && v.equals(last)) {
                return v;
            }
            last = v;
        }
        return last;
    }

    private int indexOfMonth(String value) {
        String v = value.trim();
        for (int i = 0; i < MONTHS.size(); i++) {
            if (v.equalsIgnoreCase(MONTHS.get(i)) || v.toUpperCase().startsWith(MONTHS.get(i).toUpperCase())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Swipe a NumberPicker. Increasing the value swipes content upward (bottom → top); decreasing
     * swipes downward (top → bottom). The swipe is centred on the picker and its length is
     * {@code travel} × picker-height, so a small {@code travel} moves roughly one item.
     */
    private void swipePicker(int pickerIndex, boolean increase, double travel) {
        List<WebElement> pickers = driver.findElements(By.xpath(pickerXpath(pickerIndex)));
        if (pickers.isEmpty()) {
            log.warn("NumberPicker[{}] not found — cannot swipe", pickerIndex);
            return;
        }
        Rectangle r = pickers.get(0).getRect();
        int centerX = r.getX() + r.getWidth() / 2;
        int centerY = r.getY() + r.getHeight() / 2;
        int half = Math.max(1, (int) (r.getHeight() * travel / 2));
        int topY = centerY - half;
        int bottomY = centerY + half;
        if (increase) {
            swipes.performSwipe(centerX, bottomY, centerX, topY); // up → next value
        } else {
            swipes.performSwipe(centerX, topY, centerX, bottomY); // down → previous value
        }
    }

    private String getCurrentValue(int pickerIndex) {
        List<WebElement> els;
        try {
            els = driver.findElements(By.xpath(pickerXpath(pickerIndex) + "/android.widget.EditText"));
        } catch (WebDriverException e) {
            throw failFastSessionLoss("locating picker value " + pickerIndex, e);
        }
        if (els.isEmpty()) {
            return "";
        }
        String text = els.get(0).getText();
        return text == null ? "" : text.trim();
    }

    private String pickerXpath(int pickerIndex) {
        return "//android.widget.NumberPicker[" + pickerIndex + "]";
    }

    private RuntimeException failFastSessionLoss(String action, WebDriverException cause) {
        String message = cause.getMessage() == null ? "" : cause.getMessage().toLowerCase();
        if (message.contains("connection refused") || message.contains("session") || message.contains("invalid session")) {
            return new IllegalStateException("Session lost while " + action + " in DOB picker", cause);
        }
        return cause;
    }
}
