package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingType {

    EMERGENCY_PROTECTION_ORDER("Emergency protection order"),
    FACT_FINDING("Fact finding"),
    CASE_MANAGEMENT("Case management"),
    FURTHER_CASE_MANAGEMENT("Further case management"),
    ISSUE_RESOLUTION("Issue resolution"),
    FINAL("Final"),
    JUDGMENT_AFTER_HEARING("Judgment after hearing"),
    INTERIM_CARE_ORDER("Interim care order"),
    ACCELERATED_DISCHARGE_OF_CARE("Discharge of care"),
    FAMILY_DRUG_ALCOHOL_COURT("Family drug & alcohol court"),
    PLACEMENT_HEARING("Placement hearing"),
    OTHER("Other");

    final String label;
}
