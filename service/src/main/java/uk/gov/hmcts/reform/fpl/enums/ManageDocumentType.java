package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ManageDocumentType {
    FURTHER_EVIDENCE_DOCUMENTS("Further evidence documents"),
    CORRESPONDENCE("Correspondence"),
    C2("C2 supporting documents");

    private final String label;
}
