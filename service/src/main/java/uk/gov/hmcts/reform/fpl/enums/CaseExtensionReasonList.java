package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaseExtensionReasonList {
    TIMETABLE_FOR_PROCEEDINGS("Timetable for proceedings"),
    TIMETABLE_FOR_CHILD("Timetable for child"),
    DELAY_IN_CASE_OR_IMPACT_ON_CHILD("Delay in case/impact on child"),
    INTERNATIONAL_ASPECT("International Aspect");

    private String label;
}
