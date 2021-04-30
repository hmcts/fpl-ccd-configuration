package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FurtherEvidenceType {
    APPLICANT_STATEMENT("Applicant's statement"),
    GUARDIAN_REPORTS("Child's guardian reports"),
    EXPERT_REPORTS("Expert reports"),
    OTHER_REPORTS("Other reports");

    private final String label;

}
