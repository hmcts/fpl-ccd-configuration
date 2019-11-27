package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum CMONextHearingStatus {
    FURTHER_CASE_MANAGEMENT("Further case management hearing"),
    ISSUES_RESOLUTION_HEARING("Issues resolution hearing"),
    FINAL_HEARING("Final hearing");

    private final String nextHearingStatusValue;

    CMONextHearingStatus(String nextHearingStatusValue) {
        this.nextHearingStatusValue = nextHearingStatusValue;
    }
}
