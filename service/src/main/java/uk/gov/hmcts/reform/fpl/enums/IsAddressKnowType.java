package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "IsAddressKnowList", generate = true)
@Getter
@RequiredArgsConstructor
public enum IsAddressKnowType {
    @JsonProperty("Yes")
    YES,
    @JsonProperty("No")
    NO,
    @CCD(label = "They are living in a refuge")
    LIVE_IN_REFUGE;
}
