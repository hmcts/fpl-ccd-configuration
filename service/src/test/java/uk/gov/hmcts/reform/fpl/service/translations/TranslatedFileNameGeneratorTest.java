package uk.gov.hmcts.reform.fpl.service.translations;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class TranslatedFileNameGeneratorTest {

    private final TranslatedFileNameGenerator underTest = new TranslatedFileNameGenerator();

    @Test
    void generate() {
        String actual = underTest.generate(CaseData.builder()
            .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                .uploadTranslationsOriginalDoc(DocumentReference.builder()
                    .filename("mario.pdf")
                    .build())
                .build())
            .build());

        assertThat(actual).isEqualTo("mario-Welsh.pdf");
    }
}
