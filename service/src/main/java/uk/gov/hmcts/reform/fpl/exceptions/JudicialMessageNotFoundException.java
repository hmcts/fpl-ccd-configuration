package uk.gov.hmcts.reform.fpl.exceptions;

import java.util.UUID;

public class JudicialMessageNotFoundException extends RuntimeException {
    public JudicialMessageNotFoundException(UUID messageId) {
        super(String.format("Judicial message with id %s not found", messageId));
    }
}
