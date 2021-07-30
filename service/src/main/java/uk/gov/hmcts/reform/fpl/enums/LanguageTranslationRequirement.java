package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;


@Getter
public enum LanguageTranslationRequirement {
    NO("NO", false),
    ENGLISH_TO_WELSH("ENGLISH_TO_WELSH", true),
    WELSH_TO_ENGLISH("WELSH_TO_ENGLISH", true);

    private final String value;
    private final boolean needAction;

    LanguageTranslationRequirement(String value, boolean needAction) {
        this.value = value;
        this.needAction = needAction;
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
