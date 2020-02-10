package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.PlacementDocumentType.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToSubmitEventControllerTest extends AbstractControllerTest {
    private static final UUID CONFIDENTIAL_DOC_ID = randomUUID();

    PlacementAboutToSubmitEventControllerTest() {
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

        assertThat(updatedCaseDetails).doesNotContainKey("placement");
        assertThat(updatedCaseDetails).doesNotContainKey("placementChildName");
        assertThat(updatedCaseDetails).doesNotContainKey("singleChild");

        List<String> placements = List.of("placements", "placementsWithoutPlacementOrder", "confidentialPlacements");

        placements.forEach(type -> assertThat(updatedCaseDetails.get(type)).isEqualTo(List.of(
            expectedPlacement(child1Placement, child1Application),
            expectedPlacement(child2NewPlacement, child2NewApplication)))
        );
    }

    @Test
    void shouldAddConfidentialPlacementAndPlacementsWithoutPlacementOrderToCaseDetailsWhenBothArePresentInPlacement() {
        Element<Child> child = testChild();
        DocumentReference childApplication = testDocument();
        Element<Placement> childPlacement = element(placement(child, childApplication));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "children1", List.of(child),
                "placement", childPlacement.getValue(),
                "childrenList", child.getId()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        Map<String, Object> updatedCaseDetails = callbackResponse.getData();

        CaseData caseData = mapper.convertValue(updatedCaseDetails, CaseData.class);

        assertThat(updatedCaseDetails).doesNotContainKey("placement");
        assertThat(updatedCaseDetails).doesNotContainKey("placementChildName");
        assertThat(updatedCaseDetails).doesNotContainKey("singleChild");

        assertThat(convertToList(updatedCaseDetails, "placementsWithoutPlacementOrder").get(0))
            .extracting("value")
            .isEqualTo(expectedPlacementWithoutPlacementOrder(childPlacement, childApplication));

        assertThat(unwrapElements(caseData.getPlacements())).containsOnly(childPlacement.getValue());

        assertThat(convertToList(updatedCaseDetails, "placements").get(0))
            .extracting("value")
            .isEqualTo(expectedPlacementWithoutConfidentialDocuments(childPlacement, childApplication));
    }

    private List convertToList(Map<String, Object> updatedCaseDetails, String string) {
        return mapper.convertValue(updatedCaseDetails.get(string), List.class);
    }

    private Placement placement(Element<Child> child, DocumentReference application) {
        return Placement.builder()
            .childId(child.getId())
            .childName(child.getValue().getParty().getFullName())
            .application(application)
            .confidentialDocuments(confidentialDocuments())
            .orderAndNotices(placementOrder())
            .build();
    }

    private List<Element<PlacementConfidentialDocument>> confidentialDocuments() {
        return List.of(element(CONFIDENTIAL_DOC_ID, PlacementConfidentialDocument.builder().type(ANNEX_B).build()));
    }

    private List<Element<PlacementOrderAndNotices>> placementOrder() {
        return wrapElements(PlacementOrderAndNotices.builder().type(PLACEMENT_ORDER).build());
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

    private Map<String, Object> expectedPlacementWithoutPlacementOrder(Element<Placement> placement,
                                                                       DocumentReference application) {
        return Map.of("placementChildName", placement.getValue().getChildName(),
            "placementChildId", placement.getValue().getChildId().toString(),
            "placementApplication", Map.of(
                "document_binary_url", application.getBinaryUrl(),
                "document_filename", application.getFilename(),
                "document_url", application.getUrl()),
            "placementConfidentialDocuments", List.of(Map.of(
                "id", CONFIDENTIAL_DOC_ID.toString(),
                "value", Map.of("type", "ANNEX_B"))));
    }

    private Map<String, Object> expectedPlacementWithoutConfidentialDocuments(Element<Placement> placement,
                                                                              DocumentReference application) {
        return Map.of("placementChildName", placement.getValue().getChildName(),
            "placementChildId", placement.getValue().getChildId().toString(),
            "placementApplication", Map.of(
                "document_binary_url", application.getBinaryUrl(),
                "document_filename", application.getFilename(),
                "document_url", application.getUrl()));
    }
}

