package com.urpay.tests.dmp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.DmpWishlistPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Product Wishlist Management.
 *
 * Migrated from Katalon (DMP_1 branch):
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Product Wishlist Management/
 *     Product WishlistManagement
 *   → login → NavigateToDMP → open a product → clickOnWishButton (add to wishlist) →
 *     NavigateToWishlist → ToValidateProductsAreWishedAtWishListPage.
 */
@Epic("DMP")
@Feature("Wishlist Management")
public class ProductWishlistManagementTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "regression"})
    @Description("Add a product to the wishlist and verify it appears on the Wishlist page")
    @Severity(SeverityLevel.NORMAL)
    public void testAddProductToWishlist() {
        new LoginFlow().loginWith(
                config.get("dmp.mobileNumber"),
                config.get("dmp.id"),
                config.get("dmp.verificationCode", "1234"),
                config.get("dmp.passCode", "2233"));

        DmpWishlistPage wishlist = new DmpFlow().addFirstProductToWishlist();

        Assert.assertTrue(wishlist.hasProducts(),
                "The wishlist should contain at least one wished product");
    }
}
