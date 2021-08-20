package uk.gov.hmcts.reform.fpl.exceptions;

public class LocalAuthorityNotFound extends RuntimeException {

    public LocalAuthorityNotFound(String message) {
        super(message);
    }
}
