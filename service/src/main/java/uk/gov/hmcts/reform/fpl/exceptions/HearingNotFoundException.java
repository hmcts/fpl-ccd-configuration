package uk.gov.hmcts.reform.fpl.exceptions;

import java.util.UUID;

public class HearingNotFoundException extends RuntimeException {
    public HearingNotFoundException(String message) {
        super(message);
    }

    public HearingNotFoundException(UUID hearingId) {
        this(String.format("Hearing with id %s not found", hearingId));
    }
}
