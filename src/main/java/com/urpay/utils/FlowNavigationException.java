package com.urpay.utils;

/**
 * Thrown when a flow cannot reach an expected screen and there is <b>no</b> backend/SIT error
 * evidence to blame — i.e. the failure is most likely an automation, app-state, locator, IVR or
 * timing defect rather than a backend outage.
 *
 * <p>Addresses code-review finding <b>F3</b>: absence of a success screen alone must not be
 * misclassified as a backend defect. Use {@link BackendErrorException} only when
 * {@link BackendErrorGuard} confirms a real backend banner; otherwise raise this so the failure is
 * categorized as a framework/navigation problem (with diagnostics) instead of masking a genuine
 * backend outage bucket.
 *
 * <p>Unchecked so it can surface from any flow step without checked-exception plumbing.
 */
public class FlowNavigationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FlowNavigationException(String message) {
        super(message);
    }

    public FlowNavigationException(String message, Throwable cause) {
        super(message, cause);
    }
}
