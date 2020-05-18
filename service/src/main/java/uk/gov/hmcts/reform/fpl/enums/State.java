package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum State {
    OPEN("Open"),
    SUBMITTED("Submitted"),
    GATEKEEPING("Gatekeeping"),
    PREPARE_FOR_HEARING("PREPARE_FOR_HEARING"),
    DELETED("Deleted");

    private final String value;
}
