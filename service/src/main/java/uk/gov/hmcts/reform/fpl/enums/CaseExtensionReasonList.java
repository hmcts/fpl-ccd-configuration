package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@AllArgsConstructor
public enum CaseExtensionReasonList {
    @CCD(label = "Timetable for proceedings")
    @JsonProperty("TimetableForProceedings")
    TIMETABLE_FOR_PROCEEDINGS("Timetable for proceedings"),
    @CCD(label = "Timetable for child")
    @JsonProperty("TimetableForChild")
    TIMETABLE_FOR_CHILD("Timetable for child"),
    @CCD(label = "Delay in case/impact on child")
    @JsonProperty("DelayInCaseOrImpactOnChild")
    DELAY_IN_CASE_OR_IMPACT_ON_CHILD("Delay in case/impact on child"),
    @CCD(label = "International Aspect")
    @JsonProperty("InternationalAspect")
    INTERNATIONAL_ASPECT("International Aspect"),
    NO_EXTENSION("No extension for child");

    private String label;
}
