package uk.gov.hmcts.reform.fnp.exception;

public class RetryablePaymentException extends RuntimeException {
    public RetryablePaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
