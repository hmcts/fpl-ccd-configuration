package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityCodeException extends AboutToStartOrSubmitCallbackException {
    public UnknownLocalAuthorityCodeException(String message, String userMessage) {
        super(message, userMessage);
    }
}
