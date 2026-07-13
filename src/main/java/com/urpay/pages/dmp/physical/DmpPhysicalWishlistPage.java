package com.urpay.pages.dmp.physical;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Wishlist page — LANE-B view used by the Physical Product Wishlist Management journey to
 * clear the wishlist, read its empty state, and count wished products.
 *
 * Migrated from Katalon (DMP_1 branch) com.uspace.dmp.WishlistProcessor
 * (getSizeOfWishlist / unwishAllProducts / clickOnProductWishlist) +
 * Test Cases/DMP/Wishlist/ToValidateWishlistIsEmpty.
 *
 * Reads the same Wishlist screen as the frozen DmpWishlistPage (opened via
 * {@code DmpMarketPlacePage.openWishlist()}) without modifying it.
 */
public class DmpPhysicalWishlistPage extends BasePage {

    // Empty-state title — testID or visible text.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-Text.1fbf1702-3e9d-4610-992e-6b17ff2028db' "
            + "or contains(@text,'Wishlist is Empty') or contains(@text,'wishlist is empty')]")
    private WebElement emptyTitle;

    // Empty-state subtitle.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-Text.b6ebab44-99b8-4e5a-b5eb-d1a4aea5054f' "
            + "or contains(@text,'Start adding items')]")
    private WebElement emptySubtitle;

    // Any wished product card (indexed testID prefix shared across the revamped Store lists).
    private static final String PRODUCT_CARD_TESTID =
            "testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.";
    // Per-card wish (remove) heart on the wishlist.
    private static final String REMOVE_HEART_TESTID =
            "testID-View.5a5abef0-2a62-4337-a5ab-d784a35b54a0.";

    // Highest number of wishlist items the journey ever manages (mirrors Katalon maxItems).
    private static final int MAX_ITEMS = 10;

    @Step("Check the Wishlist is empty (empty-state title shown)")
    public boolean isEmpty() {
        return isDisplayed(emptyTitle, 15);
    }

    @Step("Get the Wishlist empty-state title text")
    public String getEmptyTitle() {
        return getText(emptyTitle);
    }

    @Step("Get the Wishlist empty-state subtitle text")
    public String getEmptySubtitle() {
        return getText(emptySubtitle);
    }

    /**
     * Count the wished products by probing the indexed card testIDs 0..MAX_ITEMS until the next
     * index is absent (mirrors Katalon {@code WishlistProcessor.getSizeOfWishlist}).
     */
    @Step("Count the wished products on the Wishlist")
    public int getWishlistCount() {
        int count = 0;
        for (int i = 0; i <= MAX_ITEMS; i++) {
            if (isPresent(card(i), i == 0 ? 10 : 2)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Remove every product from the wishlist by tapping each card's wish (remove) heart from the
     * highest index down to 0, so lower indices keep their position (mirrors Katalon
     * {@code unwishAllProducts}).
     */
    @Step("Remove all products from the Wishlist")
    public void unwishAll() {
        int size = getWishlistCount();
        for (int i = size - 1; i >= 0; i--) {
            tap(removeHeart(i), 10);
        }
    }

    private By card(int index) {
        String testId = PRODUCT_CARD_TESTID + index;
        return AppiumBy.xpath(
                "//*[@content-desc='" + testId + "' or @label='" + testId + "' or @name='" + testId + "']");
    }

    private By removeHeart(int index) {
        String testId = REMOVE_HEART_TESTID + index;
        return AppiumBy.xpath(
                "//*[@content-desc='" + testId + "' or @label='" + testId + "' or @name='" + testId + "']");
    }
}
