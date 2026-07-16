package com.urpay.pages.dmp.physicalorder;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP delivery-location screen for the LANE D physical-order journey.
 *
 * Migrated from Katalon (DMP_1 branch) Test Cases/DMP/PhysicalProducts/AddLocationName:
 * after moving a physical product to checkout the app asks for a delivery location — the test
 * enters a name, saves it, edits it, then adds another location. There is NO payment step in this
 * suite (it validates the location-management flow only).
 *
 * Locators (Object Repository/Android/DMP/PhysicalProducts):
 *   LocationNameInputField = {@code testID-input-direct-undefined} (first)
 *   NextButton (save/update)= {@code testID-primary-saveOrUpdateLocation-main}
 *   LocationEditButton      = {@code testID-secondary-handleEdit-main}
 *   AddAnotherLocation      = {@code testID-View.d30f69c7-…-424dc060d77f.CircularAdd}
 */
public class DmpDeliveryLocationPage extends BasePage {

    @AndroidFindBy(xpath = "(//*[@content-desc='testID-input-direct-undefined' "
            + "or @label='testID-input-direct-undefined' or @name='testID-input-direct-undefined'])[1]")
    private WebElement locationNameInput;

    // Save / update the location — the revamped map picker uses testID-primary-WA4-main for the
    // 'Next' CTA (the visible 'Next' TextView itself is not clickable). Text-tolerant fallbacks kept.
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-WA4-main' "
            + "or @content-desc='testID-primary-saveOrUpdateLocation-main' "
            + "or @text='Next' or @text='Save' or @text='Save Location' or @text='Update']")
    private WebElement nextButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-handleEdit-main' "
            + "or @text='Edit']")
    private WebElement editButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.CircularAdd' "
            + "or @text='Add another location' or @text='Add Another Location']")
    private WebElement addAnotherLocationButton;

    // Revamped delivery screen reached after 'Buy now' for a physical product: a 'Manage location' /
    // 'Delivery location' screen (saved-location list + 'Confirm Location') OR the legacy add-location
    // name-input form. Either confirms the physical-order delivery step was reached.
    private static final By DELIVERY_SCREEN = AppiumBy.xpath(
            "//*[@text='Manage location' or @text='Delivery location' or @text='Confirm Location' "
            + "or contains(@content-desc,'deliveryLocation') or @content-desc='testID-primary-p20-main' "
            + "or @content-desc='testID-input-direct-undefined' or @name='testID-input-direct-undefined']");

    @Step("Check the delivery-location screen is displayed")
    public boolean isLoaded() {
        return isPresent(DELIVERY_SCREEN, 25);
    }

    // 'Confirm Location' primary CTA on the revamped Manage-location screen.
    private static final By CONFIRM_LOCATION = AppiumBy.xpath(
            "//*[@content-desc='testID-primary-p20-main' or @text='Confirm Location' "
            + "or @text='Confirm location']");

    // Place-order / pay CTA on the order-summary screen (LinearGradient primary button testID).
    private static final By PLACE_ORDER = AppiumBy.xpath(
            "//*[@content-desc='testID-TouchableOpacity.1e77c248-7475-4a5b-8891-8fa4f2864061' "
            + "or contains(@text,'Place Order') or @text='Pay' or @text='Confirm and Pay' "
            + "or starts-with(@text,'Pay ')]");

    private static final By LOADER = AppiumBy.xpath("//*[starts-with(@content-desc,'testID-Loader')]");

    private void waitForLoaderGone() {
        try {
            waitUtils.waitForInvisible(LOADER);
        } catch (Exception ignored) {
            // loader already gone or never shown
        }
    }

    /**
     * Complete the delivery-location step and place the order end-to-end:
     * add-location form (enter a name + Next) and/or Manage-location list (Confirm Location) →
     * order summary (Place Order) → payment verification (OTP) → order confirmation.
     * Screens vary by account state, so each stage is applied only when present.
     */
    @Step("Complete the delivery location and place the order")
    public com.urpay.pages.dmp.DmpOrderConfirmationPage completeDeliveryAndPlaceOrder(
            String locationName, String otp) {
        // 1) Add-location form (map + 'Location details' name input + Next). The location name must be
        // UNIQUE — a duplicate raises "Location name already exists" and keeps Next disabled — so append
        // 3 random digits each run.
        if (isDisplayed(locationNameInput, 6)) {
            String uniqueName = locationName + (100 + new java.util.Random().nextInt(900));
            enterLocationName(uniqueName);
            tapNext();
            waitForLoaderGone();
        }
        // 2) Manage-location list — confirm the selected/added delivery location.
        if (isPresent(CONFIRM_LOCATION, 10)) {
            tap(CONFIRM_LOCATION, 15);
            waitForLoaderGone();
        }
        // TEMP diagnostic: map the order-summary / checkout screen before placing the order.
        dumpPageSource("physical-order-summary");
        // 3) Order summary — place the order.
        if (isPresent(PLACE_ORDER, 15)) {
            tap(PLACE_ORDER, 15);
            waitForLoaderGone();
        }
        // 4) Payment verification (OTP) — reuse the proven OtpPage.
        try {
            new com.urpay.pages.auth.OtpPage().enterOtp(otp);
        } catch (Exception e) {
            log.info("Payment OTP step skipped/failed: {}", e.getMessage());
        }
        waitForLoaderGone();
        // TEMP diagnostic: map the final screen (Order Placed! / payment result).
        dumpPageSource("physical-after-pay");
        return new com.urpay.pages.dmp.DmpOrderConfirmationPage();
    }

    @Step("Enter the delivery location name '{name}'")
    public DmpDeliveryLocationPage enterLocationName(String name) {
        tap(locationNameInput, 15);
        locationNameInput.clear();
        locationNameInput.sendKeys(name);
        hideKeyboard();
        return this;
    }

    @Step("Save / continue the location (Next)")
    public DmpDeliveryLocationPage tapNext() {
        tap(nextButton, 15);
        return this;
    }

    @Step("Edit the saved location")
    public DmpDeliveryLocationPage tapEdit() {
        tap(editButton, 15);
        return this;
    }

    @Step("Add another delivery location")
    public DmpDeliveryLocationPage tapAddAnotherLocation() {
        tap(addAnotherLocationButton, 15);
        return this;
    }
}
