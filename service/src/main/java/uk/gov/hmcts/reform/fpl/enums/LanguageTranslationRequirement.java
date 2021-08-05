package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

import java.util.function.Supplier;


@Getter
public enum LanguageTranslationRequirement {
    NO(false, () -> {
        throw new IllegalArgumentException();
    }),
    ENGLISH_TO_WELSH(true, () -> "Welsh"),
    WELSH_TO_ENGLISH(true, () -> "English");

    private final boolean needAction;
    private final Supplier<String> targetLanguage;

    LanguageTranslationRequirement(boolean needAction, Supplier<String> targetLanguage) {
        this.needAction = needAction;
        this.targetLanguage = targetLanguage;
    }

}
