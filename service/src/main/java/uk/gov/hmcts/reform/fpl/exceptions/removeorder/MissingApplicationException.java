package uk.gov.hmcts.reform.fpl.exceptions.removeorder;

public class MissingApplicationException extends IllegalStateException {
    public MissingApplicationException() {
        super("Removable order or application not found");
    }

    public MissingApplicationException(String uploadedDateTime) {
        super(String.format("Application bundle uploaded at %s has no C2 or other applications", uploadedDateTime));
    }
}
