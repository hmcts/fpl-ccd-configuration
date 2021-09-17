package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.MANY;
import static uk.gov.hmcts.reform.fpl.enums.Cardinality.ONE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.assertions.DynamicListAssert.assertThatDynamicList;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementAboutToStartControllerTest extends AbstractCallbackTest {

    private final Element<Child> child1 = testChild("Alex", "Brown");
    private final Element<Child> child2 = testChild("George", "White");

    private final Placement child1Placement = Placement.builder()
        .childId(child1.getId())
        .build();

    private final Placement child2Placement = Placement.builder()
        .childId(child2.getId())
        .build();

    PlacementAboutToStartControllerTest() {
        super("placement");
    }

    @Test
    void shouldPrepareCaseDataWhenManyChildrenWithoutPlacement() {

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementChildrenCardinality()).isEqualTo(MANY);
        assertThat(actualPlacementData.getPlacementChildName()).isNull();
        assertThat(actualPlacementData.getPlacement()).isNull();
        assertThatDynamicList(actualPlacementData.getPlacementChildrenList()).hasSize(2)
            .hasElement(child1.getId(), "Alex Brown")
            .hasElement(child2.getId(), "George White");
    }

    @Test
    void shouldPrepareCaseDataWhenSingleChildWithoutPlacement() {

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(child1Placement))
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));
        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacementChildrenCardinality()).isEqualTo(ONE);
        assertThat(actualPlacementData.getPlacementChildName()).isEqualTo("George White");
        assertThat(actualPlacementData.getPlacement()).isEqualTo(Placement.builder()
            .childId(child2.getId())
            .childName("George White")
            .build());
        assertThat(actualPlacementData.getPlacementChildrenList()).isNull();
    }

    @Test
    void shouldReturnErrorWhenNoChildrenWithoutPlacement() {

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(child1Placement, child2Placement))
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getErrors()).containsExactly("There are no children available for placement application");
    }

}
