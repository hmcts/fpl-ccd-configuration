package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityDomainException extends AboutToStartOrSubmitCallbackException {
    public UnknownLocalAuthorityDomainException(String message, String userMessage) {
        super(message, userMessage);
    }
}
