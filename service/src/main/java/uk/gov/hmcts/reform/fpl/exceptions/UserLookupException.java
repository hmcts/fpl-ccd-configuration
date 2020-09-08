package uk.gov.hmcts.reform.fpl.exceptions;

public class UserLookupException extends RuntimeException {

    public UserLookupException(String message) {
        super(message);
    }
}
