package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.function.Supplier;

import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;


@Getter
@RequiredArgsConstructor
public enum LanguageTranslationRequirement {
    NO(false,
        () -> {
            throw new IllegalArgumentException();
        },
        () -> {
            throw new IllegalArgumentException();
        }),
    ENGLISH_TO_WELSH(true, () -> WELSH, () -> ENGLISH),
    WELSH_TO_ENGLISH(true, () -> ENGLISH, () -> WELSH);

    private final boolean needAction;
    private final Supplier<Language> targetLanguage;
    private final Supplier<Language> sourceLanguage;
}
