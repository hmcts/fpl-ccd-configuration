package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum YesNo {
    YES("Yes"),
    NO("No");

    private final String value;

    YesNo(String value) {
        this.value = value;
    }

    public static YesNo from(boolean val) {
        return val ? YES : NO;
    }
}
