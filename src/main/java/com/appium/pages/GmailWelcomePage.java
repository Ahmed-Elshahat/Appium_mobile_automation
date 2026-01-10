/**
 * @author Ahmed-Elshahat
 */
package com.appium.pages;

import org.openqa.selenium.By;

public class GmailWelcomePage extends BasePage {

    private final By btnGotIt = By.id("com.google.android.gm:id/welcome_tour_got_it");
    private final By btnAddEmail = By.id("com.google.android.gm:id/setup_addresses_add_another");
    private final By btnTakeMeToGmail = By.id("com.google.android.gm:id/action_done");
    // Changed to text locator as ID seem unstable or different
    private final By lblSetupHeader = By.xpath("//android.widget.TextView[contains(@text, 'Set up')]");

    // Setup wizard options
    private final By btnGoogle = By.xpath("//android.widget.TextView[@text='Google']");
    private final By btnOutlook = By.xpath("//android.widget.TextView[@text='Outlook, Hotmail, and Live']");
    private final By btnYahoo = By.xpath("//android.widget.TextView[@text='Yahoo']");
    private final By btnExchange = By.xpath("//android.widget.TextView[@text='Exchange and Office 365']");
    private final By btnOther = By.xpath("//android.widget.TextView[@text='Other']");

    public GmailWelcomePage clickGotIt() {
        click(btnGotIt, "Got It Button");
        return this;
    }

    public boolean isAddEmailButtonDisplayed() {
        return isDisplayed(btnAddEmail);
    }

    public GmailWelcomePage clickAddEmail() {
        click(btnAddEmail, "Add Email Address Button");
        return this;
    }

    public boolean isSetupHeaderDisplayed() {
        return isDisplayed(lblSetupHeader);
    }

    public boolean isOptionDisplayed(String optionName) {
        if (optionName.equalsIgnoreCase("Google"))
            return isDisplayed(btnGoogle);
        if (optionName.contains("Outlook"))
            return isDisplayed(btnOutlook);
        if (optionName.equalsIgnoreCase("Yahoo"))
            return isDisplayed(btnYahoo);
        if (optionName.contains("Exchange"))
            return isDisplayed(btnExchange);
        if (optionName.equalsIgnoreCase("Other"))
            return isDisplayed(btnOther);
        return false;
    }

    public boolean isTakeMeToGmailEnabled() {
        return isDisplayed(btnTakeMeToGmail); // Usually it's visible but disabled if no account
    }

    private boolean isDisplayed(By locator) {
        try {
            waitForVisibility(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
