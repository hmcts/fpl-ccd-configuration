package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FurtherEvidenceType {
    APPLICANT_STATEMENT("Application statement"),
    GUARDIAN_REPORTS("Child's guardian reports"),
    EXPERT_REPORTS("Expert reports"),
    OTHER_REPORTS("Other reports"),
    NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE("Notice of Acting / Notice of Issue");

    private final String label;

}
