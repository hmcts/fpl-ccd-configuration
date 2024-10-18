package uk.gov.hmcts.reform.fpl.exceptions.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AuthorizationException extends RuntimeException {
    public AuthorizationException() {
        super("Auth error");
    }

    public AuthorizationException(String message) {
        super(message);
    }
}
