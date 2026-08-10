package uk.gov.hmcts.reform.fpl.enums.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingUrgencyTypeFixedList", generate = true)
@Getter
@RequiredArgsConstructor
public enum HearingUrgencyType {
    @CCD(label = "Standard (between days 12-18)")
    STANDARD("Standard (between days 12-18)"),
    @CCD(label = "Same day")
    SAME_DAY("Same day"),
    @CCD(label = "Urgent (not same day)")
    URGENT("Urgent (not same day)");

    private final String label;
}
