package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum HearingType {

    @CCD(label = "Emergency protection order")
    EMERGENCY_PROTECTION_ORDER("Emergency protection order"),
    @CCD(label = "Fact finding")
    FACT_FINDING("Fact finding"),
    @CCD(label = "Case management")
    CASE_MANAGEMENT("Case management"),
    @CCD(label = "Further case management")
    FURTHER_CASE_MANAGEMENT("Further case management"),
    @CCD(label = "Issue resolution")
    ISSUE_RESOLUTION("Issue resolution"),
    @CCD(label = "Final")
    FINAL("Final"),
    @CCD(label = "Judgment after hearing")
    JUDGMENT_AFTER_HEARING("Judgment after hearing"),
    @CCD(label = "Interim care order")
    INTERIM_CARE_ORDER("Interim care order"),
    @CCD(label = "Discharge of care")
    ACCELERATED_DISCHARGE_OF_CARE("Discharge of care"),
    @CCD(label = "Family drug & alcohol court")
    FAMILY_DRUG_ALCOHOL_COURT("Family drug & alcohol court"),
    @CCD(label = "Placement hearing")
    PLACEMENT_HEARING("Placement hearing"),
    OTHER("Other");

    final String label;
}
