package uk.gov.hmcts.reform.fnp.exception;

public class PaymentRetryException extends RuntimeException {
    public PaymentRetryException(String message, Throwable cause) {
        super(message, cause);
    }

}
