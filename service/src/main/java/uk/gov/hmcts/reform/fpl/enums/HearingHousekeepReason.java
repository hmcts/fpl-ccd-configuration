package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingHousekeepReasons", generate = true)
@Getter
@RequiredArgsConstructor
public enum HearingHousekeepReason {
    @CCD(label = "Listed in error")
    LIST_IN_ERROR("Listed in error"),
    @CCD(label = "Duplicate hearing event")
    DUPLICATE("Duplicate hearing event"),
    @CCD(label = "Other")
    OTHER("Other");

    private final String label;
}
