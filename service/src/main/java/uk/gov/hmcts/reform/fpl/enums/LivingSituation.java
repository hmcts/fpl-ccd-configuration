package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LivingSituation {
    LIVING_WITH_RESPONDENTS("Living with respondents"),
    LIVING_WITH_OTHER_FAMILY_FRIENDS("Living with other family or friends"),
    REMOVED_BY_POLICE_POWER_ENDS("Removed by Police, powers ending soon"),
    VOLUNTARILY_SECTION_CARE_ORDER("Voluntarily in section 20 care order"),
    HOSPITAL_SOON_TO_BE_DISCHARGED("In hospital and soon to be discharged"),
    OTHER("Other");

    private final String value;
}
