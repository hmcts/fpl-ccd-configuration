package uk.gov.hmcts.reform.fpl.exceptions;

public class EmptyFileException extends IllegalArgumentException {
    public EmptyFileException() {
        super("File cannot be empty");
    }
}
