package uk.gov.hmcts.reform.fpl.exceptions;

public class JobException extends RuntimeException {

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
