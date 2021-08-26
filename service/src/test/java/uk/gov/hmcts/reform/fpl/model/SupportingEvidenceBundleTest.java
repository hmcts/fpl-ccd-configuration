package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;

class SupportingEvidenceBundleTest {

    private SupportingEvidenceBundle underTest;

    @Nested
    class AsLabel {
        @Test
        void asLabelWithNoType() {
            underTest = SupportingEvidenceBundle.builder()
                .name("Name")
                .dateTimeUploaded(LocalDateTime.of(2012, 3, 1, 20, 4, 5))
                .build();

            assertThat(underTest.asLabel()).isEqualTo("Document - Name - 1 March 2012");
        }

        @Test
        void asLabelWithType() {
            underTest = SupportingEvidenceBundle.builder()
                .name("Name")
                .type(FurtherEvidenceType.APPLICANT_STATEMENT)
                .dateTimeUploaded(LocalDateTime.of(2012, 3, 1, 20, 4, 5))
                .build();

            assertThat(underTest.asLabel()).isEqualTo("Application statement - Name - 1 March 2012");
        }
    }

    @Nested
    class SentForTranslation {

        @Test
        void sent() {
            underTest = SupportingEvidenceBundle.builder()
                .translationRequirements(WELSH_TO_ENGLISH)
                .build();

            assertThat(underTest.sentForTranslation()).isTrue();

        }

        @Test
        void sentAndReceived() {
            underTest = SupportingEvidenceBundle.builder()
                .translationRequirements(WELSH_TO_ENGLISH)
                .translatedDocument(mock(DocumentReference.class))
                .build();

            assertThat(underTest.sentForTranslation()).isFalse();
        }

        @Test
        void neverRequested() {
            underTest = SupportingEvidenceBundle.builder()
                .translationRequirements(NO)
                .build();

            assertThat(underTest.sentForTranslation()).isFalse();
        }

        @Test
        void notSpecified() {
            underTest = SupportingEvidenceBundle.builder()
                .translationRequirements(null)
                .build();

            assertThat(underTest.sentForTranslation()).isFalse();
        }
    }

}
