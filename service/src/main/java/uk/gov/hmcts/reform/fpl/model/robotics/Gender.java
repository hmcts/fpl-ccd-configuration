package uk.gov.hmcts.reform.fpl.model.robotics;

import lombok.Getter;

@Getter
public enum Gender {
    BOY("MALE"),
    GIRL("FEMALE"),
    MALE("MALE"),
    FEMALE("FEMALE");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public static String convertStringToGender(final String stringValue) {
        for (Gender gender : values()) {
            if (gender.getValue().equalsIgnoreCase(stringValue) || gender.name().equalsIgnoreCase(stringValue)) {
                return gender.getValue();
            }
        }
        return null;
    }
}
