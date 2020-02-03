package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementMidEventControllerTest extends AbstractControllerTest {

    PlacementMidEventControllerTest() {
        super("placement");
    }

    @Test
    void shouldProvideDefaultPlacementForSelectedChild() {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        List<Element<Child>> children = List.of(child1, child2);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "children1", children,
                "childrenList", child2.getId()))
            .build();

        Map<String, Object> updatedCase = postMidEvent(caseDetails).getData();

        assertThat(updatedCase.get("placementChildName")).isEqualTo(child2.getValue().getParty().getFullName());
        assertThat(updatedCase.get("placement")).isEqualTo(Map.of(
            "placementChildName", child2.getValue().getParty().getFullName(),
            "placementChildId", child2.getId().toString()));

        assertChildrenList(updatedCase, children, child2);
    }

    @Test
    void shouldProvideExistingPlacementForSelectedChild() {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        List<Element<Child>> children = List.of(child1, child2);
        DocumentReference application = testDocument();

        Element<Placement> child1Placement = element(testPlacement(child1, application));
        Element<Placement> child2Placement = element(testPlacement(child2, application));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "children1", children,
                "placements", List.of(child1Placement, child2Placement),
                "childrenList", child2.getId()
            ))
            .build();

        Map<String, Object> updatedCase = postMidEvent(caseDetails).getData();

        assertThat(updatedCase.get("placementChildName")).isEqualTo(child2.getValue().getParty().getFullName());
        assertThat(updatedCase.get("placement")).isEqualTo(Map.of(
            "placementChildName", child2.getValue().getParty().getFullName(),
            "placementChildId", child2.getId().toString(),
            "placementApplication", Map.of(
                "document_binary_url", application.getBinaryUrl(),
                "document_filename", application.getFilename(),
                "document_url", application.getUrl()
            )
        ));

        assertChildrenList(updatedCase, children, child2);
    }

    private void assertChildrenList(Map<String, Object> caseDetails,
                                    List<Element<Child>> children,
                                    Element<Child> selectedChild) {
        Function<Child, String> labelProducer = child -> child.getParty().getFullName();
        DynamicList childrenList = mapper.convertValue(caseDetails.get("childrenList"), DynamicList.class);
        DynamicList expectedChildrenList = asDynamicList(children, selectedChild.getId(), labelProducer);

        assertThat(childrenList).isEqualTo(expectedChildrenList);
    }
}
