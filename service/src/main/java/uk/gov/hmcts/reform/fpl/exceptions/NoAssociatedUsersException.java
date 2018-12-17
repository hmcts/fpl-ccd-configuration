package uk.gov.hmcts.reform.fpl.exceptions;

public class NoAssociatedUsersException extends AboutToStartOrSubmitCallbackException {
    public NoAssociatedUsersException(String message, String userMessage) {
        super(message, userMessage);
    }
}
