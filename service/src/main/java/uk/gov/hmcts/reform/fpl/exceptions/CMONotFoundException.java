package uk.gov.hmcts.reform.fpl.exceptions;

public class CMONotFoundException extends RuntimeException {
    public CMONotFoundException(String message) {
        super(message);
    }
}
