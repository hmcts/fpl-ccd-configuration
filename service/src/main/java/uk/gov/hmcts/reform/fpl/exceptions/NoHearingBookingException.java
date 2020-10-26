package uk.gov.hmcts.reform.fpl.exceptions;

import java.util.UUID;

public class NoHearingBookingException extends IllegalStateException {
    public NoHearingBookingException() {
        super("Hearing booking not found");
    }

    public NoHearingBookingException(UUID hearingBookingId) {
        super(String.format("Hearing booking with id %s not found", hearingBookingId));
    }
}
