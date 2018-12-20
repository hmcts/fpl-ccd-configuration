package uk.gov.hmcts.reform.fpl.exceptions;

public class AboutToStartOrSubmitCallbackException extends RuntimeException {

    private final String userMessage;

    AboutToStartOrSubmitCallbackException(String userMessage, String message) {
        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
