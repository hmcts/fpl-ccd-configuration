package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum NextHearingType {
    FURTHER_CASE_MGMT_HEARING("Further case management hearing"),
    ISSUES_RESOLUTION_HEARING("Issues resolution hearing"),
    FINAL_HEARING("Final hearing");

    private final String nextHearingStatusValue;

    NextHearingType(String nextHearingStatusValue) {
        this.nextHearingStatusValue = nextHearingStatusValue;
    }
}
