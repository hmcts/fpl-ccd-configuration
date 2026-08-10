package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum ManageDocumentAction {
    @CCD(label = "Upload new documents")
    UPLOAD_DOCUMENTS,
    @CCD(label = "Remove documents")
    REMOVE_DOCUMENTS;
}
