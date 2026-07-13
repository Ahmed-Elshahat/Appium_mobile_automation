package com.urpay.utils;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.appium.java_client.AppiumBy;

/**
 * Centralized, platform-aware detection of backend/SIT error banners.
 *
 * <p>Addresses code-review findings:
 * <ul>
 *   <li><b>F4</b> — banner detection is no longer Android {@code @text}-only. The locator matches
 *       Android <em>and</em> iOS/React-Native surfaces: {@code @text}, {@code @content-desc},
 *       {@code @label}, {@code @name} and {@code @value}, all case-insensitively.</li>
 *   <li><b>F3</b> — callers can now cheaply ask {@link #isPresent} whether real backend evidence
 *       exists before deciding to raise a {@link BackendErrorException} (vs a framework/navigation
 *       failure), so a missing success screen alone is never misclassified as a backend defect.</li>
 * </ul>
 *
 * <p>The raised {@link BackendErrorException} message intentionally carries a backend phrase
 * ("Transaction Declined" / "currently unavailable") matched by
 * {@code src/test/resources/categories.json} so Allure buckets it under
 * "Backend service unavailable (SIT)".
 *
 * <p>Reusable by cards, remittance, SADAD, top-up and future iOS flows — never toggles
 * {@code implicitlyWait}; all lookups go through {@link WaitUtils#findQuick(By, long)}.
 */
public final class BackendErrorGuard {

    private static final Logger log = LoggerFactory.getLogger(BackendErrorGuard.class);

    private BackendErrorGuard() { }

    /** Attributes inspected across Android + iOS / React-Native accessibility trees. */
    private static final String[] ATTRS = { "@text", "@content-desc", "@label", "@name", "@value" };

    /** Lower-cased phrase tokens that identify a backend/service failure banner. */
    private static final String[] TOKENS = {
            "declined", "unavailable", "went wrong", "server error",
            "again later", "network error", "timed out"
    };

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";

    /** Backend error banner locator, generated once from {@link #ATTRS} × {@link #TOKENS}. */
    private static final By BANNER = AppiumBy.xpath(buildXpath());

    private static String buildXpath() {
        StringBuilder sb = new StringBuilder("//*[");
        boolean first = true;
        for (String attr : ATTRS) {
            for (String token : TOKENS) {
                if (!first) {
                    sb.append(" or ");
                }
                first = false;
                sb.append("contains(translate(").append(attr)
                        .append(",'").append(UPPER).append("','").append(LOWER).append("'),'")
                        .append(token).append("')");
            }
        }
        return sb.append("]").toString();
    }

    /**
     * @return the visible backend banner's text if one is showing, otherwise {@link Optional#empty()}.
     *         Never toggles implicit wait; uses a short explicit poll.
     */
    public static Optional<String> detect(WaitUtils waits) {
        List<WebElement> banners = waits.findQuick(BANNER, 1);
        if (banners.isEmpty()) {
            return Optional.empty();
        }
        WebElement banner = banners.get(0);
        try {
            if (!banner.isDisplayed()) {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty(); // banner vanished between find and read — not a stable state
        }
        return Optional.of(readBannerText(banner));
    }

    /** True if a backend/SIT error banner is currently visible. */
    public static boolean isPresent(WaitUtils waits) {
        return detect(waits).isPresent();
    }

    /**
     * Raise a categorized {@link BackendErrorException} <b>only</b> when an explicit backend banner
     * is visible. If no banner is present this is a no-op — the caller decides how to treat a
     * missing success screen (F3: do not assume "backend" without evidence).
     *
     * @param stage human-readable operation name, e.g. "digital card issuance".
     */
    public static void raiseIfPresent(WaitUtils waits, String stage) {
        detect(waits).ifPresent(text -> {
            log.error("\u26A0 BACKEND ERROR during {}: {}", stage, text);
            throw new BackendErrorException(
                    "Backend rejected " + stage + " — \"" + text + "\". SIT backend/provider defect "
                    + "(Transaction Declined / service currently unavailable), not a test defect.");
        });
    }

    /** Read the banner text from whichever platform attribute carries it. */
    private static String readBannerText(WebElement banner) {
        String text = safeAttr(banner::getText);
        if (isBlank(text)) {
            for (String attr : new String[] { "content-desc", "label", "name", "value" }) {
                text = safeAttr(() -> banner.getAttribute(attr));
                if (!isBlank(text)) {
                    break;
                }
            }
        }
        return isBlank(text) ? "Service is currently unavailable. Please try again later." : text;
    }

    private static String safeAttr(java.util.function.Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
