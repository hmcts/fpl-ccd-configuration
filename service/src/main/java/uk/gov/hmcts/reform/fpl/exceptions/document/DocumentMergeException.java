package uk.gov.hmcts.reform.fpl.exceptions.document;

public class DocumentMergeException extends RuntimeException {

    public DocumentMergeException(String message, Throwable cause) {
        super(message, cause);
    }
}
