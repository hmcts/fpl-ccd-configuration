package uk.gov.hmcts.reform.fpl.controllers.placement;

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

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementChildSelectionMidEventTest extends AbstractCallbackTest {

    private final Element<Child> child1 = testChild("Alex", "Brown");
    private final Element<Child> child2 = testChild("George", "White");

    PlacementChildSelectionMidEventTest() {
        super("placement");
    }

    @Test
    void shouldPreparePlacementForSelectedChild() {

        final DynamicList childrenList = dynamicLists.from(1,
            of("Alex Brown", child1.getId()),
            of("George White", child2.getId()));

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placementChildrenList(childrenList)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "child-selection"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        final Placement expectedPlacement = Placement.builder()
            .childId(child2.getId())
            .childName("George White")
            .build();

        assertThat(actualPlacementData.getPlacementChildName()).isEqualTo("George White");
        assertThat(actualPlacementData.getPlacement()).isEqualTo(expectedPlacement);
    }

}
