package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ParentalResponsibilityType {
    PR_BY_FATHER("Parental responsibility by the father"),
    PR_BY_SECOND_FEMALE_PARENT("Parental responsibility by second female parent");

    private final String label;
}
