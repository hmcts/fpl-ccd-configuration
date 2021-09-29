package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.GUARDIANS_REPORT;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.OTHER_CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.FINAL_REPORTS_RELATING_TO_SIBLINGS;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class PlacementEventDataTest {

    @Nested
    class NonConfidentialPlacements {

        @Test
        void shouldReturnNonConfidentialVerisonOfExistingPlacements() {

            final PlacementSupportingDocument supportingDocument1 = PlacementSupportingDocument.builder()
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .document(testDocumentReference())
                .build();

            final PlacementSupportingDocument supportingDocument2 = PlacementSupportingDocument.builder()
                .type(STATEMENT_OF_FACTS)
                .document(testDocumentReference())
                .build();

            final PlacementSupportingDocument supportingDocument3 = PlacementSupportingDocument.builder()
                .type(FINAL_REPORTS_RELATING_TO_SIBLINGS)
                .document(testDocumentReference())
                .build();

            final PlacementConfidentialDocument confidentialDocument1 = PlacementConfidentialDocument.builder()
                .type(ANNEX_B)
                .document(testDocumentReference())
                .build();

            final PlacementConfidentialDocument confidentialDocument2 = PlacementConfidentialDocument.builder()
                .type(GUARDIANS_REPORT)
                .document(testDocumentReference())
                .build();

            final PlacementConfidentialDocument confidentialDocument3 = PlacementConfidentialDocument.builder()
                .type(OTHER_CONFIDENTIAL_DOCUMENTS)
                .document(testDocumentReference())
                .build();

            final Placement placement1 = Placement.builder()
                .supportingDocuments(wrapElements(supportingDocument1, supportingDocument2))
                .confidentialDocuments(wrapElements(confidentialDocument1))
                .build();

            final Placement placement2 = Placement.builder()
                .supportingDocuments(wrapElements(supportingDocument3))
                .confidentialDocuments(wrapElements(confidentialDocument2, confidentialDocument3))
                .build();

            final PlacementEventData underTest = PlacementEventData.builder()
                .placements(wrapElements(placement1, placement2))
                .build();

            final List<Element<Placement>> actualNonConfidentialPlacements = underTest.getPlacementsNonConfidential();

            final Placement nonConfidentialPlacement1 = Placement.builder()
                .supportingDocuments(wrapElements(supportingDocument1, supportingDocument2))
                .build();

            final Placement nonConfidentialPlacement2 = Placement.builder()
                .supportingDocuments(wrapElements(supportingDocument3))
                .build();

            assertThat(actualNonConfidentialPlacements)
                .extracting(Element::getValue)
                .containsExactly(nonConfidentialPlacement1, nonConfidentialPlacement2);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNonConfidentialVerisonOfExistingPlacements2(List<Element<Placement>> placements) {

            final PlacementEventData underTest = PlacementEventData.builder()
                .placements(placements)
                .build();

            final List<Element<Placement>> actualNonConfidentialPlacements = underTest.getPlacementsNonConfidential();

            assertThat(actualNonConfidentialPlacements).isEmpty();
        }
    }

    @Nested
    class PlacementSetter {

        @Test
        void shouldSetChildNameWhenPlacementIsSet() {

            final PlacementEventData underTest = PlacementEventData.builder().build();

            final Placement placement = Placement.builder()
                .childId(randomUUID())
                .childName("Alex Brown")
                .build();

            underTest.setPlacement(placement);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placement(placement)
                .placementChildName("Alex Brown")
                .build();

            assertThat(underTest).isEqualTo(expectedPlacementData);
        }

        @Test
        void shouldNotSetChildNameWhenPlacementDoesNotHaveChildName() {

            final PlacementEventData underTest = PlacementEventData.builder().build();

            final Placement placement = Placement.builder()
                .childId(randomUUID())
                .build();

            underTest.setPlacement(placement);

            final PlacementEventData expectedPlacementData = PlacementEventData.builder()
                .placement(placement)
                .build();

            assertThat(underTest).isEqualTo(expectedPlacementData);
        }
    }

}
