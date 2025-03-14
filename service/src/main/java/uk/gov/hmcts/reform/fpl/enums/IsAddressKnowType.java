package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IsAddressKnowType {
    @JsonProperty("Yes")
    YES,
    @JsonProperty("No")
    NO,
    LIVE_IN_REFUGE;
}
