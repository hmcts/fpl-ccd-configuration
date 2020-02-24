package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToStartControllerTest extends AbstractControllerTest {

    PlacementAboutToStartControllerTest() {
        super("placement");
    }

    @Test
    void shouldProvideDefaultPlacementForSingleChild() {
        Element<Child> child = testChild();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", List.of(child)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        Map<String, Object> updatedCaseDetails = callbackResponse.getData();

        assertThat(updatedCaseDetails.get("singleChild")).isEqualTo(YesNo.YES.toString());
        assertThat(updatedCaseDetails.get("placementChildName")).isEqualTo(child.getValue().getParty().getFullName());
        assertThat(updatedCaseDetails.get("placement")).isEqualTo(Map.of(
            "placementChildName", child.getValue().getParty().getFullName(),
            "placementChildId", child.getId().toString()));

        assertThat(updatedCaseDetails).doesNotContainKey("childrenList");
    }

    @Test
    void shouldProvideExistingPlacementForSingleChild() {
        Element<Child> child = testChild();
        DocumentReference application = testDocument();
        Element<Placement> placement = element(testPlacement(child, application));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", List.of(child), "placements", List.of(placement)))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        Map<String, Object> updatedCase = callbackResponse.getData();

        assertThat(updatedCase.get("singleChild")).isEqualTo(YesNo.YES.toString());
        assertThat(updatedCase.get("placementChildName")).isEqualTo(child.getValue().getParty().getFullName());
        assertThat(updatedCase.get("placement")).isEqualTo(Map.of(
            "placementChildName", child.getValue().getParty().getFullName(),
            "placementChildId", child.getId().toString(),
            "placementApplication", Map.of(
                "document_binary_url", application.getBinaryUrl(),
                "document_filename", application.getFilename(),
                "document_url", application.getUrl()
            )
        ));

        assertThat(updatedCase).doesNotContainKey("childrenList");
    }

    @Test
    void shouldProvideChildrenListWhenMultipleChildren() {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        List<Element<Child>> children = List.of(child1, child2);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("children1", children))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        Map<String, Object> updatedCaseDetails = callbackResponse.getData();

        assertThat(updatedCaseDetails).doesNotContainKey("placementChildName");
        assertThat(updatedCaseDetails).doesNotContainKey("placement");
        assertThat(updatedCaseDetails.get("singleChild")).isEqualTo(YesNo.NO.toString());

        DynamicList childrenList = mapper.convertValue(updatedCaseDetails.get("childrenList"), DynamicList.class);
        DynamicList expectedChildrenList = asDynamicList(children, child -> child.getParty().getFullName());
        assertThat(childrenList).isEqualTo(expectedChildrenList);
    }
}
