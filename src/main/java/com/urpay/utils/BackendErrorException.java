package com.urpay.utils;

/**
 * Thrown when the app surfaces a backend/SIT error banner during a flow (e.g. a card-issuance
 * "Transaction Declined" / "Service is currently unavailable" banner) that makes the operation
 * impossible to complete.
 *
 * <p>The message is written to contain one of the backend phrases matched by the Allure defect
 * categoriser ({@code src/test/resources/categories.json} → "Backend service unavailable (SIT)"),
 * so these bucket correctly instead of masquerading as generic element timeouts.
 *
 * <p>Unchecked so it can surface from any flow step without checked-exception plumbing. Tests
 * catch it and convert it to a clean {@code Assert.fail(...)} so the report shows a genuine
 * FAILED (product/backend defect) rather than a misleading "broken".
 */
public class BackendErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BackendErrorException(String message) {
        super(message);
    }
}
