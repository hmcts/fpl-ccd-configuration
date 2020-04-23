package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum ChildLivingSituation {
    HOSPITAL_SOON_TO_BE_DISCHARGED("In hospital and soon to be discharged"),
    REMOVED_BY_POLICE_POWER_ENDS("Removed by Police, powers ending soon"),
    VOLUNTARILY_SECTION_CARE_ORDER("Voluntarily in section 20 care order");

    private final String value;

    ChildLivingSituation(String value) {
        this.value = value;
    }

    public static ChildLivingSituation fromString(final String text) {
        return Stream.of(values())
            .filter(livingSituation -> livingSituation.value.equalsIgnoreCase(text))
            .findFirst()
            .orElse(null);
    }
}
