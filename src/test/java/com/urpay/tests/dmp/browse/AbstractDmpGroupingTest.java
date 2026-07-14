package com.urpay.tests.dmp.browse;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpBrowseFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.browse.DmpAllItemsPage;

import io.qameta.allure.Allure;

/**
 * Shared base for the two DMP grouping suites (digital &amp; physical product category management),
 * migrated from Katalon (DMP_1 branch) Test Suites/DMP TestSuite/New Test Suites After Revamp/
 * {@code Product Grouping Management} and {@code Physical Product Grouping Management}.
 *
 * <p>Both open the Store, drill into each configured category's All Items listing and search for the
 * configured product by name:
 * <ul>
 *   <li><b>Relevant categories</b> — the product should appear. Katalon <em>commented out</em> the
 *       per-category presence assertion (note: "only the first product category API returns"), so a
 *       strict per-category check is unreliable. This migration records each category's result to
 *       Allure and hard-asserts the product appears in <b>at least one</b> relevant category.</li>
 *   <li><b>Irrelevant categories</b> — the product should NOT appear; the app shows a
 *       "No Results Found / Sorry, we couldn't find a match" empty state, which is asserted.
 *       (Katalon disables this test for the physical suite, so only the digital subclass runs it.)</li>
 * </ul>
 *
 * <p>LANE A parallel migration — own profile {@code sit-dmp-a}, own user, namespace
 * {@code tests/dmp/browse}.
 */
public abstract class AbstractDmpGroupingTest extends BaseTest {

    protected final ConfigManager config = ConfigManager.getInstance();
    private boolean authenticated = false;

    protected abstract String productKey();
    protected abstract String defaultProduct();
    protected abstract String relevantKey();
    protected abstract String defaultRelevant();
    protected abstract String irrelevantKey();
    protected abstract String defaultIrrelevant();

    /** {@code true} to enter via the physical Devices ("Smart Phones") listing. */
    protected abstract boolean physical();

    protected void runRelevant() {
        ensureLoggedIn();
        String product = config.get(productKey(), defaultProduct());
        DmpBrowseFlow flow = new DmpBrowseFlow();
        boolean foundAny = false;
        for (String category : split(relevantKey(), defaultRelevant())) {
            DmpAllItemsPage items = flow.searchProductInCategory(category, product, physical());
            boolean found = items.hasSearchResults();
            foundAny = foundAny || found;
            Allure.step("Relevant '" + category + "' search '" + product + "' -> "
                    + (found ? "results shown" : "NO results"));
        }
        Assert.assertTrue(foundAny,
                "Product '" + product + "' should appear in at least one relevant category "
                        + split(relevantKey(), defaultRelevant()));
    }

    protected void runIrrelevant() {
        ensureLoggedIn();
        String product = config.get(productKey(), defaultProduct());
        DmpBrowseFlow flow = new DmpBrowseFlow();
        for (String category : split(irrelevantKey(), defaultIrrelevant())) {
            DmpAllItemsPage items = flow.searchProductInCategory(category, product, physical());
            boolean noResults = items.isNoResultsShown();
            Allure.step("Irrelevant '" + category + "' search '" + product + "' -> "
                    + (noResults ? "No Results (expected)" : "results shown (unexpected)"));
            Assert.assertTrue(noResults,
                    "Product '" + product + "' should NOT appear in irrelevant category '" + category
                            + "' — expected the 'No Results Found' empty state");
        }
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

    private List<String> split(String key, String fallback) {
        List<String> list = new ArrayList<>();
        for (String c : config.get(key, fallback).split(",")) {
            if (!c.trim().isEmpty()) {
                list.add(c.trim());
            }
        }
        return list;
    }
}
