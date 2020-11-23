package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManageDocumentTypeLA {
    FURTHER_EVIDENCE_DOCUMENTS("Further evidence documents"),
    CORRESPONDENCE("Correspondence"),
    C2("C2 supporting documents"),
    COURT_BUNDLE("Court bundle"),
    APPLICATION_DOCUMENTS("Application documents");

    private final String label;
}
