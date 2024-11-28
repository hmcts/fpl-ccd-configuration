package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ChildLivingSituation {
    LIVE_IN_REFUGE("Living in a refuge"),
    HOSPITAL_SOON_TO_BE_DISCHARGED("In hospital and soon to be discharged"),
    REMOVED_BY_POLICE_POWER_ENDS("Removed by Police, powers ending soon"),
    VOLUNTARILY_SECTION_CARE_ORDER("Voluntarily in section 20 care order"),
    NOT_SPECIFIED("Not Specified");

    private final String value;

    public static ChildLivingSituation fromString(final String text) {
        return Stream.of(values())
            .filter(livingSituation -> livingSituation.value.equalsIgnoreCase(text))
            .findFirst()
            .orElse(NOT_SPECIFIED);
    }
}
