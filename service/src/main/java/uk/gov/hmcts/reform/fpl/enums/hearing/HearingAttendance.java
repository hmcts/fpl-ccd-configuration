package uk.gov.hmcts.reform.fpl.enums.hearing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingAttendance {
    IN_PERSON("In person"),
    VIDEO("Remote - video call"),
    PHONE("Remote - phone call");

    private final String label;
}
