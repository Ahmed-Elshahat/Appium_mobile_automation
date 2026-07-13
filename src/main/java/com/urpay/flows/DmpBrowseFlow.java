package com.urpay.flows;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.urpay.pages.dmp.browse.DmpAllItemsPage;
import com.urpay.pages.dmp.browse.DmpSortFilterPage;

import io.qameta.allure.Step;

/**
 * DMP browse journeys — sorting &amp; grouping (LANE A parallel migration).
 *
 * Composes the FROZEN {@link DmpFlow} MarketPlace navigation with the LANE-A
 * {@code pages/dmp/browse} page objects. No assertions (returns data / pages for the test).
 *
 * Migrated from Katalon (DMP_1 branch) Test Suites/DMP TestSuite/New Test Suites After Revamp/
 *   Product Sorting Management — Verify Proper Sorting of Products With and Without Groups
 *   (Ascending / Descending): for each category → open Store → Vouchers "View All" →
 *   select category chip → open filter → sort by price → sample the product prices.
 */
public class DmpBrowseFlow {

    private static final Logger log = LoggerFactory.getLogger(DmpBrowseFlow.class);

    /**
     * Open the Store, drill into the given category's All Items listing, apply the price sort and
     * return the first {@code sampleSize} product prices (top to bottom).
     *
     * @param category   voucher category label, e.g. "App Store" / "Telecom".
     * @param descending {@code true} = sort high→low, {@code false} = sort low→high.
     * @param sampleSize how many product prices to sample.
     * @return the sampled prices in display order.
     */
    @Step("Sort '{category}' by price (descending={descending}) and sample {sampleSize} prices")
    public List<Double> getSortedCategoryPrices(String category, boolean descending, int sampleSize) {
        new DmpFlow().openMarketPlace();
        DmpAllItemsPage items = new DmpAllItemsPage().openFromVouchers();
        items.selectCategory(category);
        DmpSortFilterPage filter = items.openFilter();
        DmpAllItemsPage sorted = filter.sortByPrice(descending);
        List<Double> prices = sorted.getProductPrices(sampleSize);
        log.info("Sampled {} prices for category '{}' (descending={}): {}",
                prices.size(), category, descending, prices);
        return prices;
    }
}
