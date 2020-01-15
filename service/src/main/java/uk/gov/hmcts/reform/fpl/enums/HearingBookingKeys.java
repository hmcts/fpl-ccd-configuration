package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HearingBookingKeys {
    HEARING_DETAILS("hearingDetails"),
    PAST_HEARING_DETAILS("pastHearingDetails");

    private final String key;
}
