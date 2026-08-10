package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
public enum FurtherEvidenceType {
    @CCD(
            label = "Applicant statement - for example witness, social work, initial or position statements, or police disclosure documents"
    )
    APPLICANT_STATEMENT("Application statement"),
    @CCD(label = "Child's guardian reports")
    GUARDIAN_REPORTS("Child's guardian reports"),
    @CCD(label = "Expert reports")
    EXPERT_REPORTS("Expert reports"),
    @CCD(label = "Other reports")
    OTHER_REPORTS("Other reports"),
    @CCD(label = "Notice of Acting / Notice of Issue")
    NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE("Notice of Acting / Notice of Issue");

    private final String label;

}
