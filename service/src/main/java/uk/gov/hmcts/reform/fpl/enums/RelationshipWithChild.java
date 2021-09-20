package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum RelationshipWithChild {
    FATHER("Father"),
    SECOND_FEMALE_PARENT("Second female parent");

    private final String label;

    RelationshipWithChild(String label) {
        this.label = label;
    }
}
