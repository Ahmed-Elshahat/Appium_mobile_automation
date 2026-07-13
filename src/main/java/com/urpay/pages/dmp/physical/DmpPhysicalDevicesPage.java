package com.urpay.pages.dmp.physical;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;
import com.urpay.pages.dmp.DmpProductDetailsPage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP physical "Devices" listing — the full-catalogue screen reached from the Store by tapping a
 * section "View all" (onViewMore) control, which exposes a product search box.
 *
 * Migrated from Katalon (DMP_1 branch) Test Cases/DMP/Wishlist/ToMoveToDevicesPage +
 * ToSearchByProductName + ToValidateClickOnProduct (Physical Product Wishlist Management suite):
 * tap viewAllDevices ({@code testID-secondary-onViewMore-main}) → tap the search field
 * ({@code testID-TextInput.7e0015ce-…}) → type the product name → open product card 0.
 *
 * This is a LANE-B-owned page that READS the frozen Store screens without modifying them and
 * returns the frozen {@link DmpProductDetailsPage} for the wishlist (heart) action.
 */
public class DmpPhysicalDevicesPage extends BasePage {

    // Section "View all" / "View more" control (opens the full devices list). First one on the Store.
    @AndroidFindBy(xpath = "(//*[@content-desc='testID-secondary-onViewMore-main' "
            + "or @name='testID-secondary-onViewMore-main' or @label='testID-secondary-onViewMore-main' "
            + "or @text='View all' or @text='View All' or @text='View more' or @text='View More'])[1]")
    private WebElement viewAllDevices;

    // Product search input on the devices list.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-TextInput.7e0015ce-2927-4d26-af63-bab91907305d' "
            + "or contains(@content-desc,'testID-TextInput.7e0015ce') "
            + "or @class='android.widget.EditText']")
    private WebElement searchInput;

    // Indexed product card testID shared across the revamped Store product lists.
    private static final String PRODUCT_CARD_TESTID =
            "testID-TouchableOpacity.9378f6b3-a294-4126-9ae9-137f2bfe09cd.";

    @Step("Check the devices search box is displayed")
    public boolean isLoaded() {
        return isDisplayed(searchInput, 20);
    }

    @Step("Open the devices list ('View all') and focus the search box")
    public DmpPhysicalDevicesPage openDevicesSection() {
        tap(viewAllDevices, 20);
        tap(searchInput, 20);
        return this;
    }

    @Step("Search the devices list for '{productName}'")
    public DmpPhysicalDevicesPage searchProduct(String productName) {
        type(searchInput, productName);
        hideKeyboard();
        return this;
    }

    @Step("Open the device product at index {index}")
    public DmpProductDetailsPage openProduct(int index) {
        tap(productCard(index), 20);
        return new DmpProductDetailsPage();
    }

    /** Search by name and open the first matching product's details page. */
    @Step("Search for '{productName}' and open the first result")
    public DmpProductDetailsPage searchAndOpenFirstProduct(String productName) {
        searchProduct(productName);
        return openProduct(0);
    }

    private By productCard(int index) {
        String testId = PRODUCT_CARD_TESTID + index;
        return AppiumBy.xpath(
                "//*[@content-desc='" + testId + "' or @label='" + testId + "' or @name='" + testId + "']");
    }
}
