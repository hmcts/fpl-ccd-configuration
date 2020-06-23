package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.types.FixedList;
import uk.gov.hmcts.ccd.sdk.types.HasState;

@RequiredArgsConstructor
@Getter
@FixedList
public enum State implements HasState {
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

    ANY ("*"),
    RETURNED("RETURNED");



    private final String value;

    @Override
    public String getState() {
        return value;
    }
}
