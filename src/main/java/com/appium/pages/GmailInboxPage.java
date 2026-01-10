/**
 * @author Ahmed-Elshahat
 */
package com.appium.pages;

import org.openqa.selenium.By;

public class GmailInboxPage extends BasePage {

    private final By btnCompose = By.id("com.google.android.gm:id/compose_button");
    private final By btnSearch = By.id("com.google.android.gm:id/search_actionbar_query_text");
    private final By btnNavDrawer = By.xpath("//android.widget.ImageButton[@content-desc='Open navigation drawer']");

    public boolean isComposeButtonDisplayed() {
        try {
            waitForVisibility(btnCompose);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public GmailComposePage clickCompose() {
        click(btnCompose, "Compose Button");
        return new GmailComposePage();
    }
}
