package com.urpay.pages.payments;

import org.openqa.selenium.WebElement;

import com.urpay.core.BasePage;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.qameta.allure.Step;

/**
 * Cards dashboard page — card list, create new card, navigate to card sections.
 *
 * Locators from Katalon Object Repository:
 *   Object Repository/android/PaymentAndCards/Cards/CreateNewCard/
 *   Object Repository/android/PaymentAndCards/Cards/ValidationOfCardDuplication/
 */
public class CardsPage extends BasePage {

    // ── Card List ──
    // ValidationOfCardDuplication/FirstCard.rs → testID-bankCard.data.0
    @AndroidFindBy(accessibility = "testID-bankCard.data.0")
    private WebElement firstCard;

    // ValidationOfCardDuplication/addNewCard.rs — Katalon: text="Request Card"
    @AndroidFindBy(xpath = "//*[@text='Request Card' or @text='Add new card' or @text='Add New Card']")
    private WebElement addNewCardButton;

    // CreateNewCard/ViewAllButton-HomePage.rs → testID-secondary-navigateToCardsNavigation-main
    @AndroidFindBy(accessibility = "testID-secondary-navigateToCardsNavigation-main")
    private WebElement viewAllButton;

    // cardsBanner.rs → testID-View.dceaa398-d6ae-4995-b509-9dfb27610fef
    @AndroidFindBy(accessibility = "testID-View.dceaa398-d6ae-4995-b509-9dfb27610fef")
    private WebElement cardsBanner;

    // ── Create New Card Flow ──
    // CreateNewCard/RequestCardButton.rs → testID-primary-nextStep-main
    @AndroidFindBy(accessibility = "testID-primary-nextStep-main")
    private WebElement requestCardButton;

    // CreateNewCard/NextButton.rs → testID-primary-navigateToNextStep-main
    @AndroidFindBy(accessibility = "testID-primary-navigateToNextStep-main")
    private WebElement nextButton;

    // CreateNewCard/AcceptCheckBox.rs → testID-check-box-main
    @AndroidFindBy(accessibility = "testID-check-box-main")
    private WebElement acceptCheckBox;

    // CreateNewCard/ConfirmBTN.rs → testID-primary-nextStep-main (same as requestCard)
    // Using xpath to disambiguate when both are on different screens
    @AndroidFindBy(accessibility = "testID-primary-nextStep-main")
    private WebElement confirmButton;

    // CreateNewCard/Back.rs → testID-primary-backToCardsDB-main
    @AndroidFindBy(accessibility = "testID-primary-backToCardsDB-main")
    private WebElement backToCardsButton;

    // ── Transactions Tab ──
    // CreateNewCard/TransactionsTab.rs — Katalon: testID-dashboard#Transactions-Transactions
    @AndroidFindBy(xpath = "//*[contains(@content-desc,'Transactions-Transactions') or @text='Transactions']")
    private WebElement transactionsTab;

    // CreateNewCard/LatestTransactionTitle.rs
    @AndroidFindBy(accessibility = "testID-second-secRow-0")
    private WebElement latestTransactionTitle;

    // ── Card Duplication Notification ──
    // ValidationOfCardDuplication/CardValidationPopUpNotification.rs
    @AndroidFindBy(accessibility = "testID-notification-message")
    private WebElement notificationMessage;

    // ══════════════════════════════════════════════════
    //  CARD LIST ACTIONS
    // ══════════════════════════════════════════════════

    @Step("Tap first card in cards list")
    public void tapFirstCard() {
        tap(firstCard);
    }

    @Step("Tap View All cards button")
    public void tapViewAll() {
        tap(viewAllButton);
    }

    @Step("Tap Add New Card button")
    public void tapAddNewCard() {
        tap(addNewCardButton);
    }

    // ══════════════════════════════════════════════════
    //  CREATE NEW CARD ACTIONS (Katalon: createNewMadaCard)
    // ══════════════════════════════════════════════════

    @Step("Tap Request Card button")
    public void tapRequestCard() {
        tap(requestCardButton);
    }

    @Step("Tap Next button")
    public void tapNext() {
        tap(nextButton);
    }

    @Step("Tap Accept Terms checkbox")
    public void tapAcceptCheckbox() {
        tap(acceptCheckBox);
    }

    @Step("Tap Confirm button")
    public void tapConfirm() {
        tap(confirmButton);
    }

    @Step("Tap Back to Cards button")
    public void tapBackToCards() {
        tap(backToCardsButton);
    }

    // ══════════════════════════════════════════════════
    //  TRANSACTIONS TAB
    // ══════════════════════════════════════════════════

    @Step("Tap Transactions tab")
    public void tapTransactionsTab() {
        tap(transactionsTab);
    }

    @Step("Tap latest transaction")
    public void tapLatestTransaction() {
        tap(latestTransactionTitle);
    }

    // ══════════════════════════════════════════════════
    //  QUERY METHODS (no assertions)
    // ══════════════════════════════════════════════════

    public boolean isCardsPageLoaded() {
        return isDisplayed(firstCard, 15) || isPresent(
                AppiumBy.accessibilityId("testID-primary-nextStep-main"), 5);
    }

    public boolean isBackToCardsVisible() {
        return isDisplayed(backToCardsButton, 15);
    }

    public String getLatestTransactionTitle() {
        return getText(latestTransactionTitle);
    }

    public String getNotificationMessage() {
        return getText(notificationMessage);
    }

    public boolean isNotificationVisible() {
        return isPresent(AppiumBy.accessibilityId("testID-notification-message"), 5);
    }

    public void scrollDown() {
        swipeDown();
    }
}
