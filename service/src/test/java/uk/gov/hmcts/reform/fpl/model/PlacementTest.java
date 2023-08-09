package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class PlacementTest {

    @Nested
    class NonConfidential {

        private final PlacementSupportingDocument supportingDocument = PlacementSupportingDocument.builder()
            .type(BIRTH_ADOPTION_CERTIFICATE)
            .document(testDocumentReference())
            .build();

        private final PlacementConfidentialDocument confidentialDocument = PlacementConfidentialDocument.builder()
            .type(ANNEX_B)
            .document(testDocumentReference())
            .build();

        private final PlacementNoticeDocument noticeDocument1 = PlacementNoticeDocument.builder()
            .type(PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY)
            .response(testDocumentReference())
            .responseDescription("Response description")
            .build();

        private final PlacementNoticeDocument noticeDocument2 = PlacementNoticeDocument.builder()
            .type(PlacementNoticeDocument.RecipientType.CAFCASS)
            .build();

        @Test
        void shouldReturnNonConfidentialCopyWithoutNoticeResponses() {

            final Placement underTest = Placement.builder()
                .childName("Alex White")
                .childId(randomUUID())
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(supportingDocument))
                .confidentialDocuments(wrapElements(confidentialDocument))
                .noticeDocuments(wrapElements(noticeDocument1, noticeDocument2))
                .build();

            final Placement actualNonConfidentialPlacement = underTest.nonConfidential(false);

            final Placement expectedNonConfidentialPlacement = underTest.toBuilder()
                .confidentialDocuments(null)
                .noticeDocuments(wrapElements(noticeDocument1.toBuilder()
                    .response(null)
                    .responseDescription(null)
                    .build(), noticeDocument2))
                .build();

            assertThat(actualNonConfidentialPlacement).isEqualTo(expectedNonConfidentialPlacement);
        }

        @Test
        void shouldReturnNonConfidentialCopyWithNoticeResponses() {

            final Placement underTest = Placement.builder()
                .childName("Alex White")
                .childId(randomUUID())
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(supportingDocument))
                .confidentialDocuments(wrapElements(confidentialDocument))
                .noticeDocuments(wrapElements(noticeDocument1, noticeDocument2))
                .build();

            final Placement actualNonConfidentialPlacement = underTest.nonConfidential(true);

            final Placement expectedNonConfidentialPlacement = underTest.toBuilder()
                .confidentialDocuments(null)
                .build();

            assertThat(actualNonConfidentialPlacement).isEqualTo(expectedNonConfidentialPlacement);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldReturnNonConfidentialCopyWhenNoConfidentialDocuments(boolean withNoticeResponses) {

            final Placement underTest = Placement.builder()
                .childName("Alex White")
                .childId(randomUUID())
                .application(testDocumentReference())
                .build();

            final Placement actualNonConfidentialPlacement = underTest.nonConfidential(withNoticeResponses);

            assertThat(actualNonConfidentialPlacement).isEqualTo(underTest);
        }
    }

    @Nested
    class IsSubmitted {

        @Test
        void shouldReturnYesWhenPlacementApplicationHasBeenSubmitted() {

            final Placement underTest = Placement.builder()
                .placementUploadDateTime(LocalDateTime.now())
                .build();

            assertThat(underTest.isSubmitted()).isEqualTo(YES);

        }

        @Test
        void shouldReturnNoWhenPlacementApplicationHasNotBeenSubmitted() {

            final Placement underTest = Placement.builder()
                .placementUploadDateTime(null)
                .build();

            assertThat(underTest.isSubmitted()).isEqualTo(NO);

        }
    }

    @Nested
    class Label {

        @Test
        void shouldReturnLabelWithSubmissionDate() {

            final Placement underTest = Placement.builder()
                .childName("Alex Green")
                .placementUploadDateTime(LocalDateTime.of(2019, 5, 15, 13, 10))
                .build();

            assertThat(underTest.toLabel()).isEqualTo("A50, Alex Green, 15 May 2019, 1:10pm");
        }

        @Test
        void shouldReturnLabelWithoutSubmissionDate() {

            final Placement underTest = Placement.builder()
                .childName("Alex Green")
                .placementUploadDateTime(null)
                .build();

            assertThat(underTest.toLabel()).isEqualTo("A50, Alex Green");
        }
    }

    @Nested
    class UploadTime {

        @Test
        void shouldReturnLabelWithSubmissionDate() {

            final Placement underTest = Placement.builder()
                .placementUploadDateTime(LocalDateTime.of(2019, 5, 15, 13, 10))
                .build();

            assertThat(underTest.getUploadedDateTime()).isEqualTo("15 May 2019, 1:10pm");
        }

        @Test
        void shouldReturnLabelWithoutSubmissionDate() {

            final Placement underTest = Placement.builder()
                .placementUploadDateTime(null)
                .build();

            assertThat(underTest.getUploadedDateTime()).isNull();
        }
    }

    @Test
    void shouldReturnDefaultSortOrder() {

        final Placement underTest = Placement.builder().build();

        assertThat(underTest.getSortOrder()).isEqualTo(3);
    }

}
