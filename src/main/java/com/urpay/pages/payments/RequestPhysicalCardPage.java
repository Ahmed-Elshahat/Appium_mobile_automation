package com.urpay.pages.payments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Request Physical Card page — address form, request submission.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/RequestPhysicalCard/
 */
public class RequestPhysicalCardPage extends BasePage {

    // RequestPhysicalCard/RequestPysicalCopyButton.rs — "Request" button on Card Settings
    private static final By REQUEST_PHYSICAL_BTN = AppiumBy.xpath(
            "//*[@text='Request physical copy']/parent::*//*[@text='Request'] | " +
            "//*[@text='Request' and @class='android.widget.TextView'] | " +
            "//*[@content-desc='testID-primary-requestPhysicalCopy-main']");

    // ── Address Form Fields (new UI) ──
    private static final By BUILDING_NO = AppiumBy.accessibilityId("testID-input-direct-buildingNo");
    private static final By ADDITIONAL_NO = AppiumBy.accessibilityId("testID-input-direct-additionalNo");
    private static final By STREET_NAME = AppiumBy.accessibilityId("testID-input-direct-streetName");
    private static final By DISTRICT = AppiumBy.accessibilityId("testID-input-direct-district");
    private static final By CITY_DROPDOWN = AppiumBy.accessibilityId("testID-multi-select-city");
    private static final By SELECT_CITY = AppiumBy.accessibilityId("testID-search-item-0");
    private static final By POSTAL_CODE = AppiumBy.accessibilityId("testID-input-direct-postalCode");

    // RequestPhysicalCard/NextButton.rs → testID-primary-onSubmit-main
    private static final By NEXT_BTN = AppiumBy.accessibilityId("testID-primary-onSubmit-main");

    // RequestPhysicalCard/ViewCardButton.rs
    @AndroidFindBy(accessibility = "testID-primary-backToCardsDB-main")
    private WebElement viewCardButton;

    // Old locators kept for fallback
    @AndroidFindBy(accessibility = "testID-check-box-main")
    private WebElement riyadhRegionCheckbox;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Request Physical Copy button")
    public void tapRequestPhysicalCopy() {
        tap(REQUEST_PHYSICAL_BTN);
    }

    @Step("Fill national address form")
    public void fillAddressForm(String buildingNo, String additionalNo,
                                String streetName, String district, String postalCode) {
        type(BUILDING_NO, buildingNo);
        type(ADDITIONAL_NO, additionalNo);
        type(STREET_NAME, streetName);
        type(DISTRICT, district);
        tap(CITY_DROPDOWN);
        tap(SELECT_CITY);
        type(POSTAL_CODE, postalCode);
    }

    @Step("Select Riyadh delivery region (old UI)")
    public void selectRiyadhRegion() {
        tap(riyadhRegionCheckbox);
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(NEXT_BTN);
    }

    @Step("Tap View Card button")
    public void tapViewCard() {
        tap(viewCardButton);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public boolean isLoaded() {
        return isPresent(REQUEST_PHYSICAL_BTN, 10);
    }

    public boolean isAddressFormVisible() {
        return isPresent(BUILDING_NO, 8);
    }
}
