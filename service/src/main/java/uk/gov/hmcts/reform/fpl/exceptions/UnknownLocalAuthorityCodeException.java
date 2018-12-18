package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityCodeException extends AboutToStartOrSubmitCallbackException {
    public UnknownLocalAuthorityCodeException(String message) {
        super(message, "The local authority was not found");
    }
}
