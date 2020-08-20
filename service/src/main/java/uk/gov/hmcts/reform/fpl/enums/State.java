package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum State {
    @JsonProperty("Open")
    OPEN("Open"),

    @JsonProperty("Submitted")
    SUBMITTED("Submitted"),

    @JsonProperty("Gatekeeping")
    GATEKEEPING("Gatekeeping"),

    PREPARE_FOR_HEARING("PREPARE_FOR_HEARING"),
    CLOSED("CLOSED"),

    @JsonProperty("Deleted")
    DELETED("Deleted"),

    RETURNED("RETURNED"),

    ISSUE_RESOLUTION("ISSUE_RESOLUTION");

    private final String value;
}
