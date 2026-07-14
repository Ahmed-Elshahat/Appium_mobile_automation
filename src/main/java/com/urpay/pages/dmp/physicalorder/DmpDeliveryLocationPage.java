package com.urpay.pages.dmp.physicalorder;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

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

    // Save / update the location (text-tolerant: cloud build may hash the middle testID segment).
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-saveOrUpdateLocation-main' "
            + "or @text='Next' or @text='Save' or @text='Save Location' or @text='Update']")
    private WebElement nextButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-secondary-handleEdit-main' "
            + "or @text='Edit']")
    private WebElement editButton;

    @AndroidFindBy(xpath = "//*[@content-desc='testID-View.d30f69c7-50f6-4253-8542-424dc060d77f.CircularAdd' "
            + "or @text='Add another location' or @text='Add Another Location']")
    private WebElement addAnotherLocationButton;

    @Step("Check the delivery-location screen is displayed")
    public boolean isLoaded() {
        return isDisplayed(locationNameInput, 20);
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
