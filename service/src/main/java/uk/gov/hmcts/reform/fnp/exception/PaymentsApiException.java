package uk.gov.hmcts.reform.fnp.exception;

public class PaymentsApiException extends RuntimeException {

    public PaymentsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
