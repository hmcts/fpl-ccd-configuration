package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.stream.Stream;

@Getter
public enum YesNo {

    YES("Yes", "Ie"),
    NO("No", "Na"),
    DONT_KNOW("Don't know", "Ddim yn gwybod"),
    NOT_SPECIFIED("Not Specified", "Heb ei nodi");

    private final String value;
    private final String welshValue;

    YesNo(String value, String welshValue) {
        this.value = value;
        this.welshValue = welshValue;
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

    public static boolean isYesOrNo(String value) {
        return YesNo.YES.getValue().equalsIgnoreCase(value)
               || YesNo.NO.getValue().equalsIgnoreCase(value);
    }

    public String getValue(Language language) {
        return language == Language.WELSH ? welshValue : value;
    }

}
