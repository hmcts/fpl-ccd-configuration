package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum UrgencyTimeFrameType {
    @CCD(label = "On the same day")
    SAME_DAY("Same day", 0),
    @CCD(label = "Within 2 days")
    WITHIN_2_DAYS("Within 2 days", 2),
    @CCD(label = "Within 5 days")
    WITHIN_5_DAYS("Within 5 days", 5);

    private final String label;
    private final int count;
}
