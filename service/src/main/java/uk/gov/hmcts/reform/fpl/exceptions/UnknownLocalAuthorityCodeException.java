package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityCodeException extends RuntimeException {
    public UnknownLocalAuthorityCodeException(String message) {
        super(message);
    }
}
