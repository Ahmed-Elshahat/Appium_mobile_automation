package com.urpay.tests.dmp.browse;

import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Physical Product Sorting Management.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Physical Product Sorting Management
 *     → Setup test data for physical product sorting management → login → NavigateToDMP →
 *       Verify Proper Sorting of Physical Products With and Without Groups (Descending / Ascending).
 *
 * Identical to the digital {@link ProductSortingManagementTest} except the Store entry point is the
 * physical Devices listing \u2014 reached via the Store "Smart Phones" category tile (the revamped build
 * removed the standalone "Devices" View-All that Katalon's {@code viewAllDevices} tapped) \u2014 and the
 * categories come from {@code dmpA.physicalSorting.categories}. Katalon used Huawei/Offers; the current
 * catalog exposes brand chips Samsung/Apple on that listing. See {@link AbstractDmpSortingTest} for the
 * assertion parity note (soft sort check due to the known grouped-product ordering bug).
 */
@Epic("DMP")
@Feature("Browse - Sorting")
public class PhysicalProductSortingManagementTest extends AbstractDmpSortingTest {

    @Override
    protected String categoriesKey() {
        return "dmpA.physicalSorting.categories";
    }

    @Override
    protected String defaultCategories() {
        return "Samsung,Apple";
    }

    @Override
    protected boolean physical() {
        return true;
    }

    @Test(groups = {"dmp", "dmp-sorting", "regression"})
    @Description("Physical product sorting — price high→low (descending) per category")
    @Severity(SeverityLevel.NORMAL)
    public void testPhysicalProductSortingDescending() {
        runSorting(true);
    }

    @Test(groups = {"dmp", "dmp-sorting", "regression"})
    @Description("Physical product sorting — price low→high (ascending) per category")
    @Severity(SeverityLevel.NORMAL)
    public void testPhysicalProductSortingAscending() {
        runSorting(false);
    }
}
