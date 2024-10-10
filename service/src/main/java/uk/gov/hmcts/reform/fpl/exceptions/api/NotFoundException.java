package uk.gov.hmcts.reform.fpl.exceptions.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Not found");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
