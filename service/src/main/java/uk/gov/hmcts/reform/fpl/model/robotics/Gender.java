package uk.gov.hmcts.reform.fpl.model.robotics;

import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
public enum Gender {
    BOY("MALE", "Male", "Gwryw"),
    GIRL("FEMALE", "Female", "Benyw"),
    MALE("MALE", "Male", "Gwryw"),
    FEMALE("FEMALE", "Female", "Benyw"),
    OTHER("OTHER", "They identify in another way", "Maent yn uniaethu mewn ffordd arall"),
    NONE("NONE", "", "");

    private final String value;
    private final String label;
    private final String welshLabel;

    Gender(String value, String label, String welshLabel) {
        this.value = value;
        this.label = label;
        this.welshLabel = welshLabel;
    }


    public static String convertStringToGender(final String stringValue) {
        for (Gender gender : values()) {
            if (gender.getValue().equalsIgnoreCase(stringValue) || gender.name().equalsIgnoreCase(stringValue)) {
                return gender.getValue();
            }
        }
        return null;
    }

    public static Gender fromLabel(String label) {
        if (isBlank(label)) {
            return NONE;
        }
        return Stream.of(Gender.values())
            .filter(gender -> gender.label.equalsIgnoreCase(label))
            .findFirst()
            .orElse(OTHER);
    }

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }

}
