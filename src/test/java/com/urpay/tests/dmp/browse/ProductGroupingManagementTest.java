package com.urpay.tests.dmp.browse;

import org.testng.annotations.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Product Grouping Management (digital vouchers).
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Product Grouping Management
 *     → Setup test data for product category management → login → NavigateToDMP →
 *       Verify Products are Displayed in Relevant Categories → SetupUnrelatedCategories →
 *       Validate Products are Not Displayed in Irrelevant Categories.
 *
 * Product "Apple"; relevant = App Store / Recharge / Telecom; irrelevant = Food.
 * See {@link AbstractDmpGroupingTest} for the assertion parity notes.
 */
@Epic("DMP")
@Feature("Browse - Grouping")
public class ProductGroupingManagementTest extends AbstractDmpGroupingTest {

    @Override
    protected String loginPrefix() {
        return "dmpA.groupProduct";
    }

    @Override
    protected String productKey() {
        return "dmpA.grouping.product";
    }

    @Override
    protected String defaultProduct() {
        return "Apple";
    }

    @Override
    protected String relevantKey() {
        return "dmpA.grouping.relevantCategories";
    }

    @Override
    protected String defaultRelevant() {
        return "App Store,Recharge,Telecom";
    }

    @Override
    protected String irrelevantKey() {
        return "dmpA.grouping.irrelevantCategories";
    }

    @Override
    protected String defaultIrrelevant() {
        return "Food";
    }

    @Override
    protected boolean physical() {
        return false;
    }

    @Test(groups = {"dmp", "dmp-grouping", "regression"})
    @Description("Digital product appears in its relevant categories (App Store / Recharge / Telecom)")
    @Severity(SeverityLevel.NORMAL)
    public void testProductDisplayedInRelevantCategories() {
        runRelevant();
    }

    @Test(groups = {"dmp", "dmp-grouping", "regression"})
    @Description("Digital product does NOT appear in an irrelevant category (Food)")
    @Severity(SeverityLevel.NORMAL)
    public void testProductNotDisplayedInIrrelevantCategories() {
        runIrrelevant();
    }
}
