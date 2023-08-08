package uk.gov.hmcts.reform.fpl.model.interfaces;

import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

public interface NotifyDocumentUploaded {
    DocumentReference getDocument();

    default String getName() {
        return getDocument().getFilename();
    }
}
