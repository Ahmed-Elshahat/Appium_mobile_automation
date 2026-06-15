package com.urpay.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG retry analyzer for flaky tests.
 *
 * Usage:
 *   @Test(retryAnalyzer = RetryAnalyzer.class)
 *   public void testSomething() { ... }
 *
 * Or register globally via TestExecutionListener/suite XML.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRIES = 2;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            log.info("Retrying test '{}' — attempt {}/{}",
                    result.getMethod().getMethodName(), retryCount, MAX_RETRIES);
            return true;
        }
        return false;
    }
}
