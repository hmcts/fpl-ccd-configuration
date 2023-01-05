package uk.gov.hmcts.reform.fpl.exceptions.removaltool;

import java.util.UUID;

public class RemovableSentDocumentNotFoundException extends IllegalStateException {
    public RemovableSentDocumentNotFoundException(UUID docId) {
        super(String.format("Removable documents sent to parties with id %s not found", docId));
    }
}
