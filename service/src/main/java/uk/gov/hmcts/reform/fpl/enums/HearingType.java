package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingType {
    CASE_MANAGEMENT("Case management"),
    FURTHER_CASE_MANAGEMENT("Further case management"),
    ISSUE_RESOLUTION("Issue resolution"),
    FINAL("Final"),
    INTERIM_CARE_ORDER("Interim care order"),
    OTHER("Other");

    final String label;
}
