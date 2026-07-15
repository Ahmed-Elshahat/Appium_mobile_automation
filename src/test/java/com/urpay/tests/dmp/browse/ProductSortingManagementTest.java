package com.urpay.tests.dmp.browse;

import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Product Sorting Management (digital vouchers).
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Product Sorting Management
 *     → Setup test data for product sorting management → login → NavigateToDMP →
 *       Verify Proper Sorting of Products With and Without Groups (Descending / Ascending).
 *
 * Entry via the Store Vouchers "View All"; categories from {@code dmpA.sorting.categories}
 * (Katalon: App Store, Telecom). See {@link AbstractDmpSortingTest} for the assertion parity note
 * (soft sort check due to the known grouped-product ordering bug).
 */
@Epic("DMP")
@Feature("Browse - Sorting")
public class ProductSortingManagementTest extends AbstractDmpSortingTest {

    @Override
    protected String loginPrefix() {
        return "dmpA.sortProduct";
    }

    @Override
    protected String categoriesKey() {
        return "dmpA.sorting.categories";
    }

    @Override
    protected String defaultCategories() {
        return "App Store,Telecom";
    }

    @Override
    protected boolean physical() {
        return false;
    }

    @Test(groups = {"dmp", "dmp-sorting", "regression"})
    @Description("Digital product sorting — price high→low (descending) per category")
    @Severity(SeverityLevel.NORMAL)
    public void testDigitalProductSortingDescending() {
        runSorting(true);
    }

    @Test(groups = {"dmp", "dmp-sorting", "regression"})
    @Description("Digital product sorting — price low→high (ascending) per category")
    @Severity(SeverityLevel.NORMAL)
    public void testDigitalProductSortingAscending() {
        runSorting(false);
    }
}
