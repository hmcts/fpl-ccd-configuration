package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EPOExclusionRequirementTypes", generate = true)
public enum EPOExclusionRequirementType {
    @CCD(label = "No")
    NO_TO_EXCLUSION,
    @CCD(label = "Yes, starting on the date on the order")
    STARTING_ON_SAME_DATE,
    @CCD(label = "Yes, starting on a different date")
    STARTING_ON_DIFFERENT_DATE
}
