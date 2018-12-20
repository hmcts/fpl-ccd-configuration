package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityDomainException extends AboutToStartOrSubmitCallbackException {
    public UnknownLocalAuthorityDomainException(String message) {
        super("The email address was not linked to a known Local Authority", message);
    }
}
