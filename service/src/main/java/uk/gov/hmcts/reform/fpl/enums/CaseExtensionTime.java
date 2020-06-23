package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import uk.gov.hmcts.ccd.sdk.types.FixedList;
import uk.gov.hmcts.ccd.sdk.types.HasCode;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@FixedList(generate = true, id = "CaseExtensionTimeList")
public enum CaseExtensionTime implements HasLabel, HasCode {
    @JsonProperty("EightWeekExtension")
    EIGHT_WEEK_EXTENSION("Extend by 8 Weeks", "EightWeekExtension"),
    @JsonProperty("OtherExtension")
    OTHER_EXTENSION("Enter a different date", "OtherExtension");


    private final String label;
    private final String code;

    CaseExtensionTime(String label, String code) {
        this.label = label;
        this.code = code;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getCode() {
        return code;
    }
}
