package com.urpay.tests.dmp.browse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpBrowseFlow;
import com.urpay.flows.LoginFlow;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Product Sorting Management.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Product Sorting Management
 *     → Setup test data for product sorting management → login → NavigateToDMP →
 *       Verify Proper Sorting of Products With and Without Groups (Descending / Ascending).
 *
 * For each configured voucher category the flow opens the Store, drills into the category's
 * All Items listing, applies the price sort (high→low / low→high) and samples the product prices.
 * The test asserts products are displayed after the sort is applied and validates the sampled
 * order matches the requested direction.
 *
 * LANE A parallel migration — own profile {@code sit-dmp-a}, own user, namespace
 * {@code tests/dmp/browse}.
 */
@Epic("DMP")
@Feature("Browse - Sorting")
public class ProductSortingManagementTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();
    private boolean authenticated = false;

    @Test(groups = {"dmp", "dmp-sorting", "regression"})
    @Description("Digital product sorting — price high→low (descending) per category")
    @Severity(SeverityLevel.NORMAL)
    public void testDigitalProductSortingDescending() {
        ensureLoggedIn();
        verifySortingForCategories(true);
    }

    @Test(groups = {"dmp", "dmp-sorting", "regression"})
    @Description("Digital product sorting — price low→high (ascending) per category")
    @Severity(SeverityLevel.NORMAL)
    public void testDigitalProductSortingAscending() {
        ensureLoggedIn();
        verifySortingForCategories(false);
    }

    // ── helpers ────────────────────────────────────────────────────

    private void verifySortingForCategories(boolean descending) {
        int sample = Integer.parseInt(config.get("dmpA.sorting.sampleSize", "5"));
        DmpBrowseFlow flow = new DmpBrowseFlow();
        for (String category : categories()) {
            List<Double> prices = flow.getSortedCategoryPrices(category, descending, sample);

            Assert.assertFalse(prices.isEmpty(),
                    "Products should be displayed after sorting the '" + category + "' category");
            Assert.assertEquals(prices, sortedCopy(prices, descending),
                    "Products in '" + category + "' should be sorted "
                            + (descending ? "high\u2192low" : "low\u2192high") + " but were " + prices);
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

    private List<String> categories() {
        List<String> list = new ArrayList<>();
        for (String c : config.get("dmpA.sorting.categories", "App Store,Telecom").split(",")) {
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
