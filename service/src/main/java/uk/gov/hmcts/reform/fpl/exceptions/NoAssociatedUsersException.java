package uk.gov.hmcts.reform.fpl.exceptions;

public class NoAssociatedUsersException extends AboutToStartOrSubmitCallbackException {
    public NoAssociatedUsersException(String message) {
        super("No users were found for the local authority", message);
    }
}
