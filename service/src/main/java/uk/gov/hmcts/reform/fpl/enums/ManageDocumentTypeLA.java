package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManageDocumentTypeLA {
    FURTHER_EVIDENCE_DOCUMENTS,
    CORRESPONDENCE,
    C2,
    COURT_BUNDLE,
    APPLICATION;
}
