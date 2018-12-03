package uk.gov.hmcts.reform.fpl.exceptions;

public class NoAssociatedUsersException extends RuntimeException {
    public NoAssociatedUsersException(String message) {
        super(message);
    }
}
