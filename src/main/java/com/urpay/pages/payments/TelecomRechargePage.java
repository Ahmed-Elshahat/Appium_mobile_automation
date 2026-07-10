package com.urpay.pages.payments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import io.qameta.allure.Step;

/**
 * Telecom Recharge page — provider selection, package selection, recharge flow.
 *
 * Locators extracted from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/TelecomRecharge/
 */
public class TelecomRechargePage extends BasePage {

    // ── Provider Buttons ──
    @AndroidFindBy(accessibility = "testID-viewElementelecomZAIN2")
    @iOSXCUITFindBy(accessibility = "testID-viewElementelecomZAIN2")
    private WebElement zainButton;

    @AndroidFindBy(accessibility = "testID-viewElementelecomMobily2")
    @iOSXCUITFindBy(accessibility = "testID-viewElementelecomMobily2")
    private WebElement mobilyButton;

    @AndroidFindBy(accessibility = "testID-viewElementelecomSTC2")
    @iOSXCUITFindBy(accessibility = "testID-viewElementelecomSTC2")
    private WebElement stcButton;

    // ── STC Sub-options ──
    @AndroidFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0")
    @iOSXCUITFindBy(accessibility = "testID-View.ee7d7dc2-b367-4dd4-91b4-d66c95fec306.0")
    private WebElement sawaRechargeButton;

    @AndroidFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.2")
    @iOSXCUITFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.2")
    private WebElement sawaPackagesOption;

    @AndroidFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.1")
    @iOSXCUITFindBy(accessibility = "testID-TouchableWithoutFeedback.af5035ae-a8e2-4fd4-9452-40564ccbb18f.1")
    private WebElement quickNetOption;

    // ── First Card (zainFirstCard / mobilyFirstCard) → testID-data-0 ──
    @AndroidFindBy(accessibility = "testID-data-0")
    @iOSXCUITFindBy(accessibility = "testID-data-0")
    private WebElement firstCard;

    // ── First Package ──
    @AndroidFindBy(accessibility = "testID-View.0e93aa95-ebe7-44d5-81b9-b5871a1fbd37.0")
    @iOSXCUITFindBy(accessibility = "testID-View.0e93aa95-ebe7-44d5-81b9-b5871a1fbd37.0")
    private WebElement firstPackage;

    // ── Next Button — packages page ──
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onPress-main']"
            + " | //android.widget.TextView[@text='Next']/ancestor-or-self::*[@clickable='true'][1]")
    @iOSXCUITFindBy(accessibility = "testID-primary-onPress-main")
    private WebElement nextButtonPackages;

    // ── Next Button — confirm mobile number ──
    @AndroidFindBy(xpath = "//*[@content-desc='testID-View.b8ce712a-4ccc-44c1-9a87-78a58b2036b6']"
            + " | (//android.widget.TextView[@text='Next']/ancestor-or-self::*[@clickable='true'][1])[last()]")
    @iOSXCUITFindBy(accessibility = "testID-View.b8ce712a-4ccc-44c1-9a87-78a58b2036b6")
    private WebElement nextButtonMobile;

    // ── Mobile Number Field ──
    @AndroidFindBy(accessibility = "testID-input-direct-undefined")
    @iOSXCUITFindBy(accessibility = "testID-input-direct-undefined")
    private WebElement mobileNumberField;

    // ── Confirm Button ──
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onConfirm-main' or @text='Confirm']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement confirmButton;

    // ── Re-order Confirm ──
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-onConfirm-main' or @text='Confirm']")
    @iOSXCUITFindBy(accessibility = "testID-primary-onConfirm-main")
    private WebElement reorderConfirmButton;

    // ── Amount Display ──
    @AndroidFindBy(accessibility = "testID-master-amount-main")
    @iOSXCUITFindBy(accessibility = "testID-master-amount-main")
    private WebElement totalAmountInteger;

    @AndroidFindBy(accessibility = "testID-fraction-amount-main")
    @iOSXCUITFindBy(accessibility = "testID-fraction-amount-main")
    private WebElement totalAmountFraction;

    // ── Success Screen ──
    @AndroidFindBy(accessibility = "testID-secondary-action-main")
    @iOSXCUITFindBy(accessibility = "testID-secondary-action-main")
    private WebElement rechargeAnotherNumberButton;

    // DoneButton.rs → testID-primary-action-main
    @AndroidFindBy(xpath = "//*[@content-desc='testID-primary-action-main']"
            + " | //android.widget.TextView[@text='Done']/ancestor-or-self::*[@clickable='true'][1]")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement doneButton;

    // ── Order History — first card in last order ──
    @AndroidFindBy(xpath = "(//android.view.ViewGroup[@content-desc=\"testID-data-0\"])[1]")
    @iOSXCUITFindBy(accessibility = "testID-data-0")
    private WebElement firstCardInLastOrder;

    // firstCardServiceName.rs
    @AndroidFindBy(xpath = "(//*[@content-desc[contains(., 'testID-Text.title')]])[1]")
    @iOSXCUITFindBy(iOSNsPredicate = "name CONTAINS 'testID-Text.title'")
    private WebElement firstCardServiceName;

    // ── Order Detail Values (label-value-N) ──
    @AndroidFindBy(accessibility = "testID-label-value-0")
    @iOSXCUITFindBy(accessibility = "testID-label-value-0")
    private WebElement providerNameValue;

    @AndroidFindBy(accessibility = "testID-label-value-2")
    @iOSXCUITFindBy(accessibility = "testID-label-value-2")
    private WebElement productNameValue;

    @AndroidFindBy(accessibility = "testID-label-value-4")
    @iOSXCUITFindBy(accessibility = "testID-label-value-4")
    private WebElement accountNumberValue;

    @AndroidFindBy(accessibility = "testID-label-value-6")
    @iOSXCUITFindBy(accessibility = "testID-label-value-6")
    private WebElement purchaseAmountValue;

    @AndroidFindBy(accessibility = "testID-label-value-8")
    @iOSXCUITFindBy(accessibility = "testID-label-value-8")
    private WebElement purchaseDateValue;

    @AndroidFindBy(accessibility = "testID-label-value-10")
    @iOSXCUITFindBy(accessibility = "testID-label-value-10")
    private WebElement durationValue;

    // ── Re-order ──
    @AndroidFindBy(xpath = "(//*[@content-desc[contains(., 'testID-primary-action-main')]])[1]")
    @iOSXCUITFindBy(accessibility = "testID-primary-action-main")
    private WebElement reorderButton;

    @AndroidFindBy(accessibility = "testID-Text.e9250b94-72e6-44e9-a4a0-71b686634ef1")
    @iOSXCUITFindBy(accessibility = "testID-Text.e9250b94-72e6-44e9-a4a0-71b686634ef1")
    private WebElement mobileNumberHeader;

    @AndroidFindBy(accessibility = "testID-label-value-2")
    @iOSXCUITFindBy(accessibility = "testID-label-value-2")
    private WebElement reorderMobileNumberValue;

    // ── Back Button ──
    @AndroidFindBy(accessibility = "testID-right-icon-item")
    @iOSXCUITFindBy(accessibility = "testID-right-icon-item")
    private WebElement backButton;

    // ══════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Select Zain provider")
    public void selectZain() { tap(zainButton); }

    @Step("Select Mobily provider")
    public void selectMobily() { tap(mobilyButton); }

    @Step("Select STC provider")
    public void selectSTC() { tap(stcButton); }

    @Step("Select Sawa Recharge option")
    public void selectSawaRecharge() { tap(sawaRechargeButton); }

    @Step("Select Sawa packages option")
    public void selectSawaPackages() { tap(sawaPackagesOption); }

    @Step("Select QuickNet option")
    public void selectQuickNet() { tap(quickNetOption); }

    @Step("Select first card")
    public void selectFirstCard() { tap(firstCard); }

    @Step("Select first package (sawaFirstBackage)")
    public void selectFirstPackage() { tap(firstPackage); }

    @Step("Tap Next button (packages page)")
    public void tapNextPackages() {
        tap(nextButtonPackages);
    }

    @Step("Tap Next button (confirm mobile number)")
    public void tapNextMobile() {
        tap(nextButtonMobile);
    }

    @Step("Enter mobile number: {number}")
    public void enterMobileNumber(String number) {
        tap(mobileNumberField);
        type(mobileNumberField, number);
    }

    @Step("Tap Confirm")
    public void tapConfirm() {
        // The Confirm button is a full-width RN bottom button. element.click() often doesn't
        // fire React Native's onPress — fall back to a coordinate tap at the button's centre
        // (same pattern as InternationalTransferPage.scrollToConfirmAndTap).
        By confirm = io.appium.java_client.AppiumBy.xpath(
                "//*[@content-desc='testID-primary-onConfirm-main']"
                + " | //android.widget.TextView[@text='Confirm']/ancestor-or-self::*[@clickable='true'][1]");
        try {
            org.openqa.selenium.WebElement btn = waitUtils.waitForClickable(confirm, 15);
            btn.click();
            log.info("tapConfirm: element.click() fired");
            // Verify the screen advanced — if still on the confirm screen, coordinate-tap.
            if (isPresent(confirm, 3)) {
                log.warn("tapConfirm: element.click() did not advance — falling back to coordinate tap");
                org.openqa.selenium.Rectangle rect = btn.getRect();
                int cx = rect.x + rect.width / 2;
                int cy = rect.y + rect.height / 2;
                tapAtCoordinates(cx, cy);
                log.info("tapConfirm: coordinate tap at ({}, {})", cx, cy);
            }
        } catch (Exception e) {
            // Last resort: tap the bottom-centre of the screen (0.5w, 0.93h)
            log.warn("tapConfirm: locator failed — tapping bottom-centre of screen");
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            tapAtCoordinates(size.width / 2, (int) (size.height * 0.93));
        }
    }

    @Step("Tap re-order confirm")
    public void tapReorderConfirm() { tap(reorderConfirmButton); }

    @Step("Tap Done")
    public void tapDone() { tap(doneButton); }

    @Step("Tap first card in order history")
    public void tapFirstOrderCard() { tap(firstCardInLastOrder); }

    @Step("Tap Re-order button")
    public void tapReorder() {
        // Strategy 1: accessibility ID (explicit wait)
        java.util.List<WebElement> btns = waitUtils.findQuick(
                io.appium.java_client.AppiumBy.accessibilityId("testID-primary-action-main"), 5);
        if (!btns.isEmpty()) {
            btns.get(0).click();
            log.info("tapReorder: clicked via accessibilityId");
            return;
        }
        // Strategy 2: xpath contains text
        btns = waitUtils.findQuick(
                io.appium.java_client.AppiumBy.xpath(
                        "//*[contains(@text,'Reorder') or contains(@text,'reorder')]"), 3);
        if (!btns.isEmpty()) {
            btns.get(0).click();
            log.info("tapReorder: clicked via text");
            return;
        }
        // Strategy 3: xpath content-desc
        btns = waitUtils.findQuick(
                io.appium.java_client.AppiumBy.xpath(
                        "(//*[contains(@content-desc,'testID-primary-action')])[1]"), 3);
        if (!btns.isEmpty()) {
            btns.get(0).click();
            log.info("tapReorder: clicked via content-desc");
            return;
        }
        // Strategy 4: scroll and retry
        swipeDown();
        swipeDown();
        tap(reorderButton);
    }

    @Step("Tap back button")
    public void tapBack() { tap(backButton); }

    @Step("Swipe down on page")
    public void scrollDown() { swipeDown(); }

    // ══════════════════════════════════════════════════
    //  QUERIES (no assertions)
    // ══════════════════════════════════════════════════

    public String getTotalAmount() {
        String integer = getText(totalAmountInteger);
        String fraction = getText(totalAmountFraction);
        return integer + fraction;
    }

    public String getProviderName() { return getText(providerNameValue); }
    public String getProductName() { return getText(productNameValue); }
    public String getAccountNumber() { return getText(accountNumberValue); }
    public String getPurchaseAmount() { return getText(purchaseAmountValue); }
    public String getPurchaseDate() { return getText(purchaseDateValue); }
    public String getDuration() { return getText(durationValue); }
    public String getMobileNumberHeader() { return getText(mobileNumberHeader); }
    public String getReorderMobileNumber() { return getText(reorderMobileNumberValue); }
    public String getFirstCardServiceName() { return getText(firstCardServiceName); }

    public boolean isRechargeSuccessful() {
        // Success screen shows Done button (testID-primary-action-main) or 'Done' text.
        // Also dismiss NPS survey if it covers the success screen.
        By skipSurvey = io.appium.java_client.AppiumBy.accessibilityId("testID-tertiary-skipSurvey-main");
        if (isPresent(skipSurvey, 3)) {
            log.info("NPS survey detected on recharge success — tapping Skip");
            tap(skipSurvey);
        }
        return isDisplayed(doneButton, 30);
    }
    public boolean isLoaded() { return isDisplayed(zainButton, 10) || isDisplayed(stcButton, 10); }
    public boolean isOrderDetailsLoaded() { return isDisplayed(providerNameValue, 10); }
}
