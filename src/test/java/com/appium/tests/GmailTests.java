package com.appium.tests;

import com.appium.pages.GmailWelcomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GmailTests extends BaseTest {

    // Helper to bypass Welcome screen if present
    private void ensureOnSetupScreen(GmailWelcomePage page) {
        // We blindly try to click Got It. If it fails (not present), we assume we are
        // good or logic handles it.
        // Better: Check if Got It is displayed, then click.
        try {
            // Locating blindly might be slow due to implicit wait, but safe for stability.
            // Using a try-catch block for the click
            page.clickGotIt();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        } catch (Exception e) {
            // Assume we are already past it or it wasn't found
            System.out.println("Got It button not found or click failed: " + e.getMessage());
        }
    }

    @Test(priority = 1, description = "Verify that the 'Got It' button is displayed on launch")
    public void testWelcomeScreenGotItDisplay() {
        GmailWelcomePage welcomePage = new GmailWelcomePage();
        // Just verifying it exists or is clickable
        // If we want to strictly verify availability without moving state:
        // Assert.assertTrue(welcomePage.isGotItDisplayed()); // Method needed in PO

        // Check if we can click it
        welcomePage.clickGotIt();
        Assert.assertTrue(welcomePage.isAddEmailButtonDisplayed(), "Should navigate to Add Email screen");
    }

    @Test(priority = 2, description = "Navigate to Setup Screen")
    public void testNavigateToSetup() {
        GmailWelcomePage welcomePage = new GmailWelcomePage();
        ensureOnSetupScreen(welcomePage);
        Assert.assertTrue(welcomePage.isAddEmailButtonDisplayed(), "Add Email button should be visible");
    }

    @Test(priority = 3, description = "Verify Setup Header")
    public void testSetupHeaderDisplay() {
        GmailWelcomePage welcomePage = new GmailWelcomePage();
        ensureOnSetupScreen(welcomePage);
        Assert.assertTrue(welcomePage.isSetupHeaderDisplayed(), "Setup Header should be visible");
    }

    @Test(priority = 4, description = "Verify Add Email Button")
    public void testAddEmailButtonDisplay() {
        GmailWelcomePage welcomePage = new GmailWelcomePage();
        ensureOnSetupScreen(welcomePage);
        Assert.assertTrue(welcomePage.isAddEmailButtonDisplayed(), "Add Email button should be visible");
    }

    @org.testng.annotations.DataProvider
    public Object[][] getEmailData() throws java.io.IOException {
        java.util.List<java.util.HashMap<String, String>> data = com.appium.utils.JsonUtils
                .getJsonDataToMap("gmail_data.json");
        Object[][] testData = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            testData[i][0] = data.get(i);
        }
        return testData;
    }

    @Test(priority = 5, dataProvider = "getEmailData", description = "Verify Email Provider Options from JSON")
    public void testEmailProviderOptions(java.util.HashMap<String, String> inputData) {
        GmailWelcomePage welcomePage = new GmailWelcomePage();
        ensureOnSetupScreen(welcomePage);
        welcomePage.clickAddEmail();

        String provider = inputData.get("emailProvider");
        Assert.assertTrue(welcomePage.isOptionDisplayed(provider), "Option " + provider + " should be visible");
    }

    @Test(priority = 10, description = "Verify Add Email Back Navigation")
    public void testAddEmailBackNavigation() {
        GmailWelcomePage welcomePage = new GmailWelcomePage();
        ensureOnSetupScreen(welcomePage);
        welcomePage.clickAddEmail();
        Assert.assertTrue(welcomePage.isOptionDisplayed("Google"));

        com.appium.drivers.DriverManager.getDriver().navigate().back();

        Assert.assertTrue(welcomePage.isAddEmailButtonDisplayed(), "Should be back on Setup screen");
    }
}
