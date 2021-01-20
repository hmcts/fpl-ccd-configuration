package uk.gov.hmcts.reform.fpl.exceptions;

@SuppressWarnings("squid:MaximumInheritanceDept")
public abstract class LogAsWarningException extends AboutToStartOrSubmitCallbackException {
    protected LogAsWarningException(String userMessage, String message) {
        super(userMessage, message);
    }
}
