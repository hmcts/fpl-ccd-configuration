package uk.gov.hmcts.reform.fpl.exceptions;

public class EmailFailedSendException extends RuntimeException {
    public EmailFailedSendException(Throwable cause) {
        super(cause);
    }
}
