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

    // State label renamed to 'Case management' as of FPLA-1920.
    // State ID remains 'PREPARE_FOR_HEARING' to avoid breaking existing cases.
    PREPARE_FOR_HEARING("PREPARE_FOR_HEARING"),
    CLOSED("CLOSED"),

    @JsonProperty("Deleted")
    DELETED("Deleted"),

    RETURNED("RETURNED");

    private final String value;
}
