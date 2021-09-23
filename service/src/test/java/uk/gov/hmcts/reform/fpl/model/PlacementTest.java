package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

        @Test
        void shouldReturnNonConfidentialCopy() {

            final PlacementSupportingDocument supportingDocument = PlacementSupportingDocument.builder()
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .document(testDocumentReference())
                .build();

            final PlacementConfidentialDocument confidentialDocument = PlacementConfidentialDocument.builder()
                .type(ANNEX_B)
                .document(testDocumentReference())
                .build();

            final Placement underTest = Placement.builder()
                .childName("Alex White")
                .childId(randomUUID())
                .application(testDocumentReference())
                .supportingDocuments(wrapElements(supportingDocument))
                .confidentialDocuments(wrapElements(confidentialDocument))
                .build();

            final Placement actualNonConfidentialPlacement = underTest.nonConfidential();

            final Placement expectedNonConfidentialPlacement = underTest.toBuilder()
                .confidentialDocuments(null)
                .build();

            assertThat(actualNonConfidentialPlacement).isEqualTo(expectedNonConfidentialPlacement);
        }

        @Test
        void shouldReturnNonConfidentialCopyWhenNoConfidentialDocuments() {

            final Placement underTest = Placement.builder()
                .childName("Alex White")
                .childId(randomUUID())
                .application(testDocumentReference())
                .build();

            final Placement actualNonConfidentialPlacement = underTest.nonConfidential();

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

}
