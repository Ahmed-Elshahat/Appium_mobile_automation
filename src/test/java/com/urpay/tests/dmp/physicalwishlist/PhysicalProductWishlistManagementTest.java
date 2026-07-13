package com.urpay.tests.dmp.physicalwishlist;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.urpay.core.BaseTest;
import com.urpay.core.ConfigManager;
import com.urpay.flows.DmpFlow;
import com.urpay.flows.LoginFlow;
import com.urpay.pages.dmp.physical.DmpPhysicalDevicesPage;
import com.urpay.pages.dmp.physical.DmpPhysicalWishlistPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

/**
 * DMP Physical Product Wishlist Management (LANE B).
 *
 * Migrated from Katalon (DMP_1 branch)
 *   Test Suites/DMP TestSuite/New Test Suites After Revamp/Physical Product Wishlist Management:
 *   login → NavigateToDMP → NavigateToWishlist → unwish all → assert empty →
 *   (Devices → search by name → open product → add to wishlist) ×2 →
 *   NavigateToWishlist → assert two products wished.
 *
 * The two @Test methods share one session (chained via {@code dependsOnMethods}) so the login and
 * cleared-wishlist state carry over from phase 1 to phase 2.
 */
@Epic("DMP")
@Feature("Physical Product Wishlist Management")
public class PhysicalProductWishlistManagementTest extends BaseTest {

    private final ConfigManager config = ConfigManager.getInstance();

    @Test(groups = {"dmp", "dmp-physical-wishlist", "regression"}, priority = 1)
    @Description("Clearing the wishlist leaves the empty-state message")
    @Severity(SeverityLevel.NORMAL)
    public void testWishlistIsEmptyAfterClearing() {
        new LoginFlow().loginWith(
                config.get("dmpb.mobileNumber"),
                config.get("dmpb.id"),
                config.get("dmpb.verificationCode", "1234"),
                config.get("dmpb.passCode", "2233"));

        new DmpFlow().openMarketPlace().openWishlist();
        DmpPhysicalWishlistPage wishlist = new DmpPhysicalWishlistPage();
        wishlist.unwishAll();

        Assert.assertTrue(wishlist.isEmpty(),
                "Wishlist should show the empty-state message after clearing");
    }

    @Test(groups = {"dmp", "dmp-physical-wishlist", "regression"}, priority = 2,
            dependsOnMethods = "testWishlistIsEmptyAfterClearing")
    @Description("Adding two physical products makes them appear on the Wishlist")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddTwoPhysicalProductsToWishlist() {
        addPhysicalProductToWishlist(config.get("dmpb.physical.firstProduct", "Mobily"));
        addPhysicalProductToWishlist(config.get("dmpb.physical.secondProduct", "Apple"));

        new DmpFlow().openMarketPlace().openWishlist();
        DmpPhysicalWishlistPage wishlist = new DmpPhysicalWishlistPage();

        Assert.assertEquals(wishlist.getWishlistCount(), 2,
                "Both physical products should be wished on the Wishlist");
    }

    /** Open the Store → Devices list, search by name, open the first result, and wish it. */
    private void addPhysicalProductToWishlist(String productName) {
        new DmpFlow().openMarketPlace();
        DmpPhysicalDevicesPage devices = new DmpPhysicalDevicesPage().openDevicesSection();
        devices.searchAndOpenFirstProduct(productName).addToWishlist();
    }
}
