package uk.gov.hmcts.reform.fpl.exceptions;

import java.util.UUID;

public class NoDocumentException extends IllegalStateException {
    public NoDocumentException() {
        super("No document found");
    }

    public NoDocumentException(UUID orderId) {
        super(String.format("Document with id %s not found", orderId));
    }
}
