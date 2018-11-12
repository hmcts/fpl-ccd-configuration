package uk.gov.hmcts.reform.fpl.exceptions;

public class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
