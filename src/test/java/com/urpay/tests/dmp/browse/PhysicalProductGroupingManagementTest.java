package com.urpay.tests.dmp.browse;

import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Physical Product Grouping Management.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Physical Product Grouping Management
 *     → Setup test data for physical product category management → login → NavigateToDMP →
 *       Verify Physical Products are Displayed in Relevant Categories.
 *
 * Product "iPhone 16"; relevant = Smart Phones. The Katalon suite DISABLES the irrelevant-category
 * test ({@code isRun=false}), so only the relevant check runs here. Entry is the physical Devices
 * ("Smart Phones") listing. See {@link AbstractDmpGroupingTest} for the assertion parity notes.
 */
@Epic("DMP")
@Feature("Browse - Grouping")
public class PhysicalProductGroupingManagementTest extends AbstractDmpGroupingTest {

    @Override
    protected String loginPrefix() {
        return "dmpA.groupPhysical";
    }

    @Override
    protected String productKey() {
        return "dmpA.physicalGrouping.product";
    }

    @Override
    protected String defaultProduct() {
        return "iPhone 16";
    }

    @Override
    protected String relevantKey() {
        return "dmpA.physicalGrouping.relevantCategories";
    }

    @Override
    protected String defaultRelevant() {
        return "Smart Phones";
    }

    @Override
    protected String irrelevantKey() {
        return "dmpA.physicalGrouping.irrelevantCategories";
    }

    @Override
    protected String defaultIrrelevant() {
        return "";
    }

    @Override
    protected boolean physical() {
        return true;
    }

    @Test(groups = {"dmp", "dmp-grouping", "regression"})
    @Description("Physical product appears in its relevant category (Smart Phones)")
    @Severity(SeverityLevel.NORMAL)
    public void testPhysicalProductDisplayedInRelevantCategories() {
        runRelevant();
    }
}
