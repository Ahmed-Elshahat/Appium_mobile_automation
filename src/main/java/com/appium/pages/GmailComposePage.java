/**
 * @author Ahmed-Elshahat
 */
package com.appium.pages;

import org.openqa.selenium.By;

public class GmailComposePage extends BasePage {

    private final By inputTo = By.id("com.google.android.gm:id/to");
    private final By inputSubject = By.id("com.google.android.gm:id/subject");
    private final By inputBody = By.xpath("//android.widget.EditText[@text='Compose email']");
    private final By btnSend = By.id("com.google.android.gm:id/send");

    public GmailComposePage enterTo(String email) {
        sendKeys(inputTo, email, "To Field");
        return this;
    }

    public GmailComposePage enterSubject(String subject) {
        sendKeys(inputSubject, subject, "Subject Field");
        return this;
    }

    public GmailComposePage enterBody(String body) {
        sendKeys(inputBody, body, "Body Field");
        return this;
    }

    public void clickSend() {
        click(btnSend, "Send Button");
    }
}
