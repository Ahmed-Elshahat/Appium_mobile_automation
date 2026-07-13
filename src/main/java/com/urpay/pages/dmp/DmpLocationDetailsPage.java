package com.urpay.pages.dmp;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * DMP delivery Location Details page — shown when ordering a physical product (e.g. a
 * "New Arrivals" device) before placing the order.
 *
 * Migrated from Katalon {@code Test Cases/DMP/CompleteLocationDetails/ValidateUserCompleteLocationDetails}:
 * if a "Confirm Location" button is already present, tap it; otherwise type a location into the
 * address input (testID-input-direct-undefined), tap "Next", then "Confirm Location".
 */
public class DmpLocationDetailsPage extends BasePage {

    // "Confirm Location" button (stable text).
    @AndroidFindBy(xpath = "//*[@text='Confirm Location' or contains(@text,'Confirm Location')]")
    private WebElement confirmLocationButton;

    // Address / location input.
    @AndroidFindBy(accessibility = "testID-input-direct-undefined")
    private WebElement locationInput;

    // "Next" button of the location form.
    @AndroidFindBy(xpath = "//*[@text='Next']")
    private WebElement nextButton;

    @Step("Check the Location Details step is present")
    public boolean isPresent() {
        return isDisplayed(confirmLocationButton, 8) || isDisplayed(locationInput, 8);
    }

    @Step("Complete the delivery location details (location: {location})")
    public void complete(String location) {
        if (isDisplayed(confirmLocationButton, 8)) {
            tap(confirmLocationButton, 15);
            return;
        }
        type(locationInput, location);
        tap(nextButton, 15);
        tap(confirmLocationButton, 15);
    }

    /** Complete the location step only if it is shown (digital orders skip it). */
    @Step("Complete location details if the step is shown")
    public void completeIfPresent(String location) {
        if (isPresent()) {
            complete(location);
        }
    }
}
