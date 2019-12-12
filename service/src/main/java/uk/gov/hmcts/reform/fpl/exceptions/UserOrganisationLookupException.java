package uk.gov.hmcts.reform.fpl.exceptions;

public class UserOrganisationLookupException extends RuntimeException {
    public UserOrganisationLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
