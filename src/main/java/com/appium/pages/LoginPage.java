package com.appium.pages;

import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private final By inputUsername = By.id("com.example:id/username");
    private final By inputPassword = By.id("com.example:id/password");
    private final By btnLogin = By.id("com.example:id/login");
    private final By lblError = By.id("com.example:id/error");

    public LoginPage enterUsername(String username) {
        sendKeys(inputUsername, username, "Username Field");
        return this;
    }

    public LoginPage enterPassword(String password) {
        sendKeys(inputPassword, password, "Password Field");
        return this;
    }

    public void clickLogin() {
        click(btnLogin, "Login Button");
    }

    public String getErrorMessage() {
        return getText(lblError);
    }
}
