package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;

class TranslatedFileNameGeneratorTest {

    private final TranslatedFileNameGenerator underTest = new TranslatedFileNameGenerator();

    @Test
    void generateIfEnglishToWelsh() {
        String actual = underTest.generate(CaseData.builder()
            .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                .uploadTranslationsOriginalDoc(DocumentReference.builder()
                    .filename("mario.pdf")
                    .build())
                .build())
            .build(), ENGLISH_TO_WELSH);

        assertThat(actual).isEqualTo("mario-Welsh.pdf");
    }

    @Test
    void generateIfWelshToEnglish() {
        String actual = underTest.generate(CaseData.builder()
            .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                .uploadTranslationsOriginalDoc(DocumentReference.builder()
                    .filename("mario.pdf")
                    .build())
                .build())
            .build(), WELSH_TO_ENGLISH);

        assertThat(actual).isEqualTo("mario-English.pdf");
    }
}
