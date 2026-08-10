package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@AllArgsConstructor
@Getter
public enum ParentalResponsibilityType {
    @CCD(label = "Parental responsibility by the father")
    PR_BY_FATHER("Parental responsibility by the father"),
    @CCD(label = "Parental responsibility by second female parent")
    PR_BY_SECOND_FEMALE_PARENT("Parental responsibility by second female parent");

    private final String label;
}
