package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.needTranslation;


class LanguageTranslationRequirementTest {

    @Test
    void needTranslationIfNull() {
        assertThat(needTranslation(null)).isEqualTo(YesNo.NO);
    }

    @Test
    void needTranslationIfNO() {
        assertThat(needTranslation(NO)).isEqualTo(YesNo.NO);
    }

    @Test
    void needTranslationIfEnglishToWelsh() {
        assertThat(needTranslation(ENGLISH_TO_WELSH)).isEqualTo(YesNo.YES);
    }

    @Test
    void needTranslationIfWelshToEnglish() {
        assertThat(needTranslation(WELSH_TO_ENGLISH)).isEqualTo(YesNo.YES);
    }


}
