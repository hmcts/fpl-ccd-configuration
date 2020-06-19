package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum YesNo {
    YES("Yes"),
    NO("No"),
    DONT_KNOW("Don't know"),
    NOT_SPECIFIED("Not Specified");

    private final String value;

    YesNo(String value) {
        this.value = value;
    }

    public static YesNo from(boolean val) {
        return val ? YES : NO;
    }

    public static YesNo fromString(final String text) {
        return Stream.of(values())
            .filter(enumData -> enumData.value.equalsIgnoreCase(text))
            .findFirst()
            .orElse(NOT_SPECIFIED);
    }
}
