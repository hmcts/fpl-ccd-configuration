package uk.gov.hmcts.reform.fnp.exception;

public class RetryablePaymentException extends PaymentsApiException {
    public RetryablePaymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
