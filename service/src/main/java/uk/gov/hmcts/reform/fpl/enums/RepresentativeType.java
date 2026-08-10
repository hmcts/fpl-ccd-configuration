package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum RepresentativeType {
    @CCD(label = "Local Authority")
    LOCAL_AUTHORITY,
    @CCD(label = "Respondent Solicitor")
    RESPONDENT_SOLICITOR,
    @CCD(label = "Child Solicitor")
    CHILD_SOLICITOR
}
