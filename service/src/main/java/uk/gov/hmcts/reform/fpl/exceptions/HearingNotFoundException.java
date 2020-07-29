package uk.gov.hmcts.reform.fpl.exceptions;

public class HearingNotFoundException extends RuntimeException {
    public HearingNotFoundException(String message) {
        super(message);
    }
}
