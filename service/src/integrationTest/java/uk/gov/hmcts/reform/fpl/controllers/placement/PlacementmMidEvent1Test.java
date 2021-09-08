package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementmMidEvent1Test extends AbstractCallbackTest {

    private final Element<Child> child1 = testChild("Alex", "Brown");
    private final Element<Child> child2 = testChild("George", "White");

    private final Placement child1Placement = Placement.builder()
        .childId(child1.getId())
        .build();

    private final Placement child2Placement = Placement.builder()
        .childId(child2.getId())
        .build();

    PlacementmMidEvent1Test() {
        super("placement");
    }

    @Test
    void shouldPrepareCaseDataWhenManyChildrenWithoutPlacement() {

        DynamicList dynamicList = dynamicLists.from(1,
            Pair.of("Alex Brown", child1.getId()),
            Pair.of("George White", child2.getId()));

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placementChildrenList(dynamicList)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "child"));
        final PlacementEventData placementEventData = updatedCaseData.getPlacementEventData();

        assertThat(placementEventData.getPlacementChildName()).isEqualTo("George White");
        assertThat(placementEventData.getPlacement()).isEqualTo(Placement.builder()
            .childId(child2.getId())
            .childName("George White")
            .build());
    }

}
