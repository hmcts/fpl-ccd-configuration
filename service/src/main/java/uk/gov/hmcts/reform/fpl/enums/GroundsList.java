package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GroundsList {
    @JsonProperty("noCare")
    NO_CARE,
    @JsonProperty("beyondControl")
    BEYOND_PARENTAL_CONTROL
}
