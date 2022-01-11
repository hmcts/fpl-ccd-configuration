package uk.gov.hmcts.reform.fpl.exceptions;

//extends AboutToStartOrSubmitCallbackException
public class EncryptedPdfUploadedException extends LogAsWarningException {

    public EncryptedPdfUploadedException(String message) {
        super("Encrypted PDF file was uploaded which cannot be processed.", message);
    }
}
