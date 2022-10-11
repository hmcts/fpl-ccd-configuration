package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaseExtensionReasonList {
    @JsonProperty("TimetableForProceedings")
    TIMETABLE_FOR_PROCEEDINGS("Timetable for proceedings"),
    @JsonProperty("TimetableForChild")
    TIMETABLE_FOR_CHILD("Timetable for child"),
    @JsonProperty("DelayInCaseOrImpactOnChild")
    DELAY_IN_CASE_OR_IMPACT_ON_CHILD("Delay in case/impact on child");

    private String label;
}
