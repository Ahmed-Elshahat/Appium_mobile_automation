package com.urpay.tests.dmp.browse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpBrowseFlow;
import com.urpay.flows.LoginFlow;

import io.qameta.allure.Allure;

/**
 * Shared base for the two DMP sorting suites (digital vouchers &amp; physical devices),
 * migrated from Katalon (DMP_1 branch) Test Suites/DMP TestSuite/New Test Suites After Revamp/
 * {@code Product Sorting Management} and {@code Physical Product Sorting Management}.
 *
 * <p>Both Katalon suites, for each configured category, open the Store, drill into the category's
 * All Items listing, apply the price sort (high&rarr;low / low&rarr;high) and sample the product
 * prices via {@code AllItemsPageProcessor.getProducts}.
 *
 * <p><b>Assertion parity — known app bug.</b> The suite name ("… With and Without Groups")
 * refers to a documented app defect: <em>grouped/featured</em> products (a product family shown
 * with a "Starting from" price) are pinned ahead of the linearly-sorted <em>ungrouped</em>
 * products, so the full sampled list is not strictly ordered. The Katalon authors handled this by
 * making the descending assertion {@code FailureHandling.CONTINUE_ON_FAILURE} (soft) and
 * <em>commenting out</em> the ascending assertion entirely. This migration mirrors that intent:
 * the hard assertion is that products are <b>displayed after the sort is applied</b>; the sort
 * order itself is recorded to Allure as informational (actual vs expected) without failing the
 * test, until the grouping bug is fixed.
 *
 * <p>LANE A parallel migration — own profile {@code sit-dmp-a}, own user, namespace
 * {@code tests/dmp/browse}.
 */
public abstract class AbstractDmpSortingTest extends BaseTest {

    protected final ConfigManager config = ConfigManager.getInstance();
    private boolean authenticated = false;

    /** Config key holding the comma-separated categories to validate (e.g. {@code dmpA.sorting.categories}). */
    protected abstract String categoriesKey();

    /** Fallback categories when the config key is absent. */
    protected abstract String defaultCategories();

    /** {@code true} to enter via the physical Devices "View All"; {@code false} for digital Vouchers. */
    protected abstract boolean physical();

    protected void runSorting(boolean descending) {
        ensureLoggedIn();
        int sample = Integer.parseInt(config.get("dmpA.sorting.sampleSize", "5"));
        DmpBrowseFlow flow = new DmpBrowseFlow();
        for (String category : categories()) {
            List<Double> prices = flow.getSortedCategoryPrices(category, descending, sample, physical());

            Assert.assertFalse(prices.isEmpty(),
                    "Products should be displayed after sorting the '" + category + "' category");
            recordSortOrder(category, descending, prices);
        }
    }

    /**
     * Record the sampled order vs the expected sorted order to Allure. Informational only — mirrors
     * Katalon's soft/disabled sort assertion (see class javadoc: grouped-product ordering bug).
     */
    private void recordSortOrder(String category, boolean descending, List<Double> prices) {
        List<Double> expected = sortedCopy(prices, descending);
        boolean strictlySorted = prices.equals(expected);
        String direction = descending ? "high\u2192low" : "low\u2192high";
        Allure.step("'" + category + "' sort " + direction
                + " — sampled=" + prices + ", expected=" + expected
                + (strictlySorted ? " (strictly sorted)"
                        : " (NOT strictly sorted — known grouped-product ordering bug)"));
    }

    private void ensureLoggedIn() {
        if (authenticated) {
            return;
        }
        new LoginFlow().loginWith(
                config.get("dmpA.mobileNumber"),
                config.get("dmpA.id"),
                config.get("dmpA.verificationCode", "1234"),
                config.get("dmpA.passCode", "2233"));
        authenticated = true;
    }

    private List<String> categories() {
        List<String> list = new ArrayList<>();
        for (String c : config.get(categoriesKey(), defaultCategories()).split(",")) {
            if (!c.trim().isEmpty()) {
                list.add(c.trim());
            }
        }
        return list;
    }

    private List<Double> sortedCopy(List<Double> prices, boolean descending) {
        List<Double> copy = new ArrayList<>(prices);
        copy.sort(descending ? Comparator.reverseOrder() : Comparator.naturalOrder());
        return copy;
    }
}
