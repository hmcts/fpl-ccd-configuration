package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementDocumentUploadMidEventTest extends AbstractPlacementControllerTest {

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
                .supportingDocuments(wrapElements(birthCertificate, statementOfFacts))
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .placementEventData(placementData)
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "documents-upload");

        assertThat(response.getErrors()).isEmpty();
    }

}
