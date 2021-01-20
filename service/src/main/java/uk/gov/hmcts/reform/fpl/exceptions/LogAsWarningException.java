package uk.gov.hmcts.reform.fpl.exceptions;

public abstract class LogAsWarningException extends AboutToStartOrSubmitCallbackException {
    public LogAsWarningException(String userMessage, String message) {
        super(userMessage, message);
    }
}
