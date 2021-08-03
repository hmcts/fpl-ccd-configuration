package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

import java.util.function.Supplier;


@Getter
public enum LanguageTranslationRequirement {
    NO("NO", false, () -> {
        throw new IllegalArgumentException();
    }),
    ENGLISH_TO_WELSH("ENGLISH_TO_WELSH", true, () -> "Welsh"),
    WELSH_TO_ENGLISH("WELSH_TO_ENGLISH", true, () -> "English");

    private final String value;
    private final boolean needAction;
    private final Supplier<String> targetLanguage;

    LanguageTranslationRequirement(String value, boolean needAction, Supplier<String> targetLanguage) {
        this.value = value;
        this.needAction = needAction;
        this.targetLanguage = targetLanguage;
    }

    public static LanguageTranslationRequirement fromString(String value) {
        for (LanguageTranslationRequirement requirement : LanguageTranslationRequirement.values()) {
            if (requirement.value.equalsIgnoreCase(value)) {
                return requirement;
            }
        }
        return NO;
    }

}
