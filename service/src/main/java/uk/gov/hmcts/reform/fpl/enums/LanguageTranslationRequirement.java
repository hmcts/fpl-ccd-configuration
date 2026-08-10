package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

import java.util.function.Supplier;

import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@Getter
@RequiredArgsConstructor
public enum LanguageTranslationRequirement {
    @CCD(label = "No")
    NO(false,
        () -> {
            throw new IllegalArgumentException();
        },
        () -> {
            throw new IllegalArgumentException();
        }),
    @CCD(label = "Yes - English to Welsh")
    ENGLISH_TO_WELSH(true, () -> WELSH, () -> ENGLISH),
    @CCD(label = "Yes - Welsh to English")
    WELSH_TO_ENGLISH(true, () -> ENGLISH, () -> WELSH);

    private final boolean needAction;
    private final Supplier<Language> targetLanguage;
    private final Supplier<Language> sourceLanguage;
}
