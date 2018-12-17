package uk.gov.hmcts.reform.fpl.exceptions;

public class AboutToStartOrSubmitCallbackException extends RuntimeException {

    public String userMessage;

    public AboutToStartOrSubmitCallbackException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
