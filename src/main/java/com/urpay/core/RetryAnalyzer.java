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
        // A "Service is currently unavailable" backend/SIT outage is DETERMINISTIC — a retry only
        // re-runs the whole login + navigation to hit the same down route. Skip it: fail fast once.
        if (isDeterministicOutage(result.getThrowable())) {
            log.info("Not retrying '{}' — deterministic backend outage (service unavailable); "
                    + "a retry cannot recover it.", result.getMethod().getMethodName());
            return false;
        }
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            log.info("Retrying test '{}' — attempt {}/{}",
                    result.getMethod().getMethodName(), retryCount, MAX_RETRIES);
            return true;
        }
        return false;
    }

    /** True if the failure is the categorized "Service is currently unavailable" SIT outage. */
    private boolean isDeterministicOutage(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            String m = c.getMessage();
            if (m != null && m.contains("currently unavailable")) {
                return true;
            }
        }
        return false;
    }
}
