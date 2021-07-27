package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;


@Getter
public enum LanguageTranslationRequirement {
    NO("NO"),
    ENGLISH_TO_WELSH("ENGLISH_TO_WELSH"),
    WELSH_TO_ENGLISH("WELSH_TO_ENGLISH");

    private final String value;

    LanguageTranslationRequirement(String value) {
        this.value = value;
    }

    public static LanguageTranslationRequirement fromString(String value) {
        for(LanguageTranslationRequirement requirement : LanguageTranslationRequirement.values()) {
            if (requirement.value.equalsIgnoreCase(value)) {
                return requirement;
            }
        }
        return NO;
    }

}
