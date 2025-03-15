package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProceedingStatus {
    @JsonProperty("Ongoing")
    ONGOING("Ongoing"),
    @JsonProperty("Previous")
    PREVIOUS("Previous");

    private final String value;
}
