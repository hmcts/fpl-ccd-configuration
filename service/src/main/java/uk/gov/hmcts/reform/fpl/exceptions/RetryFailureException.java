package uk.gov.hmcts.reform.fpl.exceptions;

public class RetryFailureException extends RuntimeException {

    public RetryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
