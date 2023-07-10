package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManageDocumentAction {
    UPLOAD_DOCUMENTS, REMOVE_DOCUMENTS;
}
