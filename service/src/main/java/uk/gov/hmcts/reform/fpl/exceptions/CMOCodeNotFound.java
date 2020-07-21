package uk.gov.hmcts.reform.fpl.exceptions;

public class CMOCodeNotFound extends RuntimeException {
    public CMOCodeNotFound(String message) {
        super(message);
    }
}
