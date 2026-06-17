package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Request Physical Card page — delivery region selection, request submission.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/RequestPhysicalCard/
 */
public class RequestPhysicalCardPage extends BasePage {

    // RequestPhysicalCard/RequestPysicalCopyButton.rs
    @AndroidFindBy(accessibility = "testID-primary-requestPhysicalCopy-main")
    private WebElement requestPhysicalCopyButton;

    // RequestPhysicalCard/ChooseRiyadhChexkBox.rs
    @AndroidFindBy(accessibility = "testID-check-box-main")
    private WebElement riyadhRegionCheckbox;

    // RequestPhysicalCard/NextButton.rs
    @AndroidFindBy(accessibility = "testID-primary-navigateToNextStep-main")
    private WebElement nextButton;

    // RequestPhysicalCard/ViewCardButton.rs
    @AndroidFindBy(accessibility = "testID-primary-backToCardsDB-main")
    private WebElement viewCardButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap Request Physical Copy button")
    public void tapRequestPhysicalCopy() {
        tap(requestPhysicalCopyButton);
    }

    @Step("Select Riyadh delivery region")
    public void selectRiyadhRegion() {
        tap(riyadhRegionCheckbox);
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(nextButton);
    }

    @Step("Tap View Card button")
    public void tapViewCard() {
        tap(viewCardButton);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS
    // ══════════════════════════════════════════════════

    public boolean isLoaded() {
        return isDisplayed(requestPhysicalCopyButton, 10);
    }
}
