package uk.gov.hmcts.reform.fpl.exceptions;

public class DocumentSealFailureException extends RuntimeException {
    public DocumentSealFailureException(Throwable cause) {
        super(cause);
    }
}
