package uk.gov.hmcts.reform.fpl.exceptions;

public class UnknownLocalAuthorityCodeException extends AboutToStartOrSubmitCallbackException {
    public UnknownLocalAuthorityCodeException(String message) {
        super("The local authority was not found", message);
    }
}
