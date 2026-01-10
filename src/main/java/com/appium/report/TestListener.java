package com.appium.report;

import com.appium.utils.ScreenshotUtils;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        ScreenshotUtils.saveScreenshot();
    }

    // Can override other methods like onTestStart, onTestSuccess if logging is
    // needed
}
