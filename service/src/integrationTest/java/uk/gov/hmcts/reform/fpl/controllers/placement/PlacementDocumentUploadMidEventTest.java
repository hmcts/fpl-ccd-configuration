package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementDocumentUploadMidEventTest extends AbstractCallbackTest {

    PlacementDocumentUploadMidEventTest() {
        super("placement");
    }

    private final PlacementSupportingDocument birtCertificate = PlacementSupportingDocument.builder()
        .document(testDocumentReference())
        .type(BIRTH_ADOPTION_CERTIFICATE)
        .build();

    private final PlacementSupportingDocument statementOfFacts = PlacementSupportingDocument.builder()
        .document(testDocumentReference())
        .type(STATEMENT_OF_FACTS)
        .build();

    private final PlacementConfidentialDocument annexB = PlacementConfidentialDocument.builder()
        .document(testDocumentReference())
        .type(ANNEX_B)
        .build();

    @Test
    void shouldReturnErrorsWhenRequiredDocumentsNotPresent() {

        final PlacementEventData placementData = PlacementEventData.builder()
            .placement(Placement.builder().build())
            .build();

        final CaseData caseData = CaseData.builder()
            .placementEventData(placementData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "documents-upload");

        assertThat(response.getErrors()).containsExactly(
            "Add required placement application",
            "Add required Birth/Adoption Certificate supporting document",
            "Add required Statement of facts supporting document",
            "Add required Annex B confidential document");
    }

    @Test
    void shouldNotReturnErrorsWhenRequiredDocumentsArePresent() {

        final PlacementEventData placementData = PlacementEventData.builder()
            .placement(Placement.builder()
                .application(testDocumentReference())
                .confidentialDocuments(wrapElements(annexB))
                .supportingDocuments(wrapElements(birtCertificate, statementOfFacts))
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .placementEventData(placementData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "documents-upload");

        assertThat(response.getErrors()).isEmpty();
    }

}
