package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToSubmitControllerTest extends AbstractControllerTest {

    PlacementAboutToSubmitControllerTest() {
        super("placement");
    }

    @Test
    void shouldUpdateChildPlacement() {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        DocumentReference child1Application = testDocument();
        DocumentReference child2Application = testDocument();

        Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
        Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

        DocumentReference child2NewApplication = testDocument();
        Element<Placement> child2NewPlacement = element(child2Placement.getId(),
            testPlacement(child2, child2NewApplication));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "children1", List.of(child1, child2),
                "placements", List.of(child2Placement, child1Placement),
                "placement", child2NewPlacement.getValue(),
                "childrenList", child2.getId()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        Map<String, Object> updatedCaseDetails = callbackResponse.getData();

        assertThat(updatedCaseDetails).containsKey("placement");
        assertThat(updatedCaseDetails).doesNotContainKey("placementChildName");
        assertThat(updatedCaseDetails).doesNotContainKey("singleChild");

        assertThat(updatedCaseDetails.get("placements")).isEqualTo(List.of(
            expectedPlacement(child1Placement, child1Application),
            expectedPlacement(child2NewPlacement, child2NewApplication)
        ));
    }

    private Map<String, Object> expectedPlacement(Element<Placement> placement, DocumentReference application) {
        return Map.of("id", placement.getId().toString(),
            "value", Map.of("placementChildName", placement.getValue().getChildName(),
                "placementChildId", placement.getValue().getChildId().toString(),
                "placementApplication", Map.of(
                    "document_binary_url", application.getBinaryUrl(),
                    "document_filename", application.getFilename(),
                    "document_url", application.getUrl()
                )));
    }
}
