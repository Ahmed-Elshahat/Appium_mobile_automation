package com.urpay.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * Applies {@link RetryAnalyzer} to EVERY @Test method so transient/flaky failures
 * (cloud cold-start logins, momentary element timeouts) retry inline.
 *
 * Why this matters for the P&amp;C suite: many tests use {@code dependsOnMethods}.
 * When a flaky login fails on the first attempt, all its dependents cascade-SKIP.
 * Retrying the login inline lets it pass and its dependents then RUN normally.
 *
 * Registered via the suite XML &lt;listeners&gt; block (processed before test collection).
 */
public class RetryListener implements IAnnotationTransformer {

    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
