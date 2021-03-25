package uk.gov.hmcts.reform.fpl.exceptions;

import java.util.UUID;

public class RespondentNotFoundException extends IllegalStateException {
    public RespondentNotFoundException() {
        super("Respondent not found");
    }

    public RespondentNotFoundException(UUID hearingBookingId) {
        super(String.format("Respondent with id %s not found", hearingBookingId));
    }
}
