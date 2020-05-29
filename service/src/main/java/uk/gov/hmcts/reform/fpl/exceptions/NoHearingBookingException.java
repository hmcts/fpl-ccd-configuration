package uk.gov.hmcts.reform.fpl.exceptions;

public class NoHearingBookingException extends IllegalStateException {
    public NoHearingBookingException() {
        super("Expected to have at least one hearing booking");
    }
}
