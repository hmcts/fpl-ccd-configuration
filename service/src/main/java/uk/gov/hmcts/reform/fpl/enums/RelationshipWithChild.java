package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum RelationshipWithChild {
    @CCD(label = "Father")
    FATHER("Father"),
    @CCD(label = "Second female parent")
    SECOND_FEMALE_PARENT("Second female parent");

    private final String label;

    RelationshipWithChild(String label) {
        this.label = label;
    }
}
