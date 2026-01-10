/**
 * @author Ahmed-Elshahat
 */
package com.appium.tests;

import com.appium.drivers.DriverFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    @BeforeMethod
    public void setup() {
        DriverFactory.initDriver();
        startRecording();
    }

    @AfterMethod(alwaysRun = true) // Ensure driver quits even if test fails
    public void tearDown() {
        stopVideoAndAttach();
        DriverFactory.quitDriver();
    }

    private void startRecording() {
        try {
            org.openqa.selenium.WebDriver driver = com.appium.drivers.DriverManager.getDriver();
            if (driver instanceof io.appium.java_client.screenrecording.CanRecordScreen) {
                ((io.appium.java_client.screenrecording.CanRecordScreen) driver).startRecordingScreen();
            }
        } catch (Exception e) {
            System.err.println("Failed to start video recording: " + e.getMessage());
        }
    }

    private void stopVideoAndAttach() {
        try {
            org.openqa.selenium.WebDriver driver = com.appium.drivers.DriverManager.getDriver();
            if (driver instanceof io.appium.java_client.screenrecording.CanRecordScreen) {
                String base64Video = ((io.appium.java_client.screenrecording.CanRecordScreen) driver)
                        .stopRecordingScreen();
                if (base64Video != null) {
                    io.qameta.allure.Allure.addAttachment("Video", "video/mp4",
                            new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(base64Video)), "mp4");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to stop/attach video recording: " + e.getMessage());
        }
    }
}
