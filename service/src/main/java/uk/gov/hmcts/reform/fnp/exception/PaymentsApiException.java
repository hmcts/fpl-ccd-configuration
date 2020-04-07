package uk.gov.hmcts.reform.fnp.exception;

public class PaymentsApiException extends RuntimeException {

    private final int status;

    public PaymentsApiException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
