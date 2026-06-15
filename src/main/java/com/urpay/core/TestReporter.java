package com.urpay.core;

/**
 * DIP: Abstraction for test reporting.
 * Decouples framework from concrete ExtentReports implementation.
 * Can be swapped with Allure, custom reporter, etc.
 */
public interface TestReporter {

    void initReports(String suiteName);

    void createTest(String testName);

    void info(String message);

    void pass(String message);

    void fail(String message);

    void fail(String message, String screenshotBase64);

    void skip(String message);

    void warning(String message);

    void attachScreenshot(String base64);

    void flush();
}
