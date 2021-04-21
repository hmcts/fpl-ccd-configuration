package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManageDocumentType {
    FURTHER_EVIDENCE_DOCUMENTS("Further evidence documents for main application"),
    CORRESPONDENCE("Correspondence"),
    ADDITIONAL_APPLICATIONS_DOCUMENTS("Documents for additional applications");

    private final String label;
}
