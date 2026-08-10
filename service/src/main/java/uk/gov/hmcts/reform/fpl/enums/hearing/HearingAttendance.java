package uk.gov.hmcts.reform.fpl.enums.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
public enum HearingAttendance {
    @CCD(label = "In person")
    IN_PERSON("In person"),
    @CCD(label = "Remote - video call")
    VIDEO("Remote - video call"),
    @CCD(label = "Remote - phone call")
    PHONE("Remote - phone call");

    private final String label;
}
