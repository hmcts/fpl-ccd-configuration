package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityDomainException extends RuntimeException {
    public UnknownLocalAuthorityDomainException(String message) {
        super(message);
    }
}
