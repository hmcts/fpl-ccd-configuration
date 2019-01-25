package uk.gov.hmcts.reform.fpl.config.utils;

public enum LivingSituationListType {

    LIVING_WITH_RESPONDENTS("Living with respondents"),
    LIVING_WITH_OTHER_FAMILY_OR_FRIENDS("Living with other family or friends"),
    REMOVED_BY_POLICE_POWER_ENDING_SOON("Removed by Police, powers ending soon"),
    VOLUNTARILY_IN_SECTION_20_CARE_ORDER("Voluntarily in section 20 care order"),
    IN_HOSPITAL_AND_SOON_TO_BE_DISCHARGED("In hospital and soon to be discharged"),
    OTHER("Other");

    private final String label;

    LivingSituationListType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
