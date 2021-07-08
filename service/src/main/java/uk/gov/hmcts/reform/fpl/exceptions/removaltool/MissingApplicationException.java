package uk.gov.hmcts.reform.fpl.exceptions.removaltool;

public class MissingApplicationException extends IllegalStateException {
    public MissingApplicationException(String uploadedDateTime) {
        super(String.format("Application bundle uploaded at %s has no C2 or other applications", uploadedDateTime));
    }
}
