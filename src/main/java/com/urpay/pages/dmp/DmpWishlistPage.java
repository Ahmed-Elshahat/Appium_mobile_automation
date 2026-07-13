package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP Wishlist page — reached from the Store header wishlist (heart) icon.
 *
 * Migrated from Katalon {@code com.uspace.dmp.WishlistProcessor}
 * (moveToWishlistPage / clickOnProduct). The wishlist lists the products the user has
 * previously wished; tapping a product card opens its details page.
 *
 * Product cards carry the indexed testID
 * {@code testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.<index>} — the same
 * card testID used across the revamped Store product lists.
 */
public class DmpWishlistPage extends BasePage {

    // "My Wishlist" screen title (revamped header).
    @AndroidFindBy(xpath = "//*[@text='Wishlist' or @text='My Wishlist']")
    private WebElement wishlistTitle;

    // "Your wishlist is empty" marker — present only when nothing is wished.
    @AndroidFindBy(xpath = "//*[contains(@text,'wishlist is empty') or contains(@text,'Wishlist is empty')]")
    private WebElement emptyWishlistLabel;

    private static final String PRODUCT_CARD_TESTID =
            "testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.";

    @Step("Check the Wishlist page is loaded")
    public boolean isLoaded() {
        return isDisplayed(wishlistTitle, 20);
    }

    @Step("Check the Wishlist is empty")
    public boolean isEmpty() {
        return isDisplayed(emptyWishlistLabel, 5);
    }

    @Step("Check the Wishlist has at least one product")
    public boolean hasProducts() {
        return isPresent(productCard(0), 20);
    }

    @Step("Select wishlist product at index {index}")
    public DmpProductDetailsPage selectProduct(int index) {
        tap(productCard(index), 20);
        return new DmpProductDetailsPage();
    }

    private org.openqa.selenium.By productCard(int index) {
        String testId = PRODUCT_CARD_TESTID + index;
        return io.appium.java_client.AppiumBy.xpath(
                "//*[@content-desc='" + testId + "' or @label='" + testId + "' or @name='" + testId + "']");
    }
}
