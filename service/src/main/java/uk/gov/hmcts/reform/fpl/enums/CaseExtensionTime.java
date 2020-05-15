package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CaseExtensionTime {
    @JsonProperty("OtherExtension")
    OTHER_EXTENSION,
    @JsonProperty("EightWeekExtension")
    EIGHT_WEEK_EXTENSION;
}
