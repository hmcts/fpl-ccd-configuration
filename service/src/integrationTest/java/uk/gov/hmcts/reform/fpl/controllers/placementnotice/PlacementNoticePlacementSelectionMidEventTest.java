package uk.gov.hmcts.reform.fpl.controllers.placementnotice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementNoticeController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(PlacementNoticeController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementNoticePlacementSelectionMidEventTest extends AbstractPlacementNoticeControllerTest {

    @Test
    void shouldPreparePlacement() {

        final Element<Placement> placement = element(Placement.builder()
            .childId(child1.getId())
            .placementRespondentsToNotify(newArrayList(father))
            .build());

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placement(placement.getValue())
            .placements(newArrayList(placement))
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .respondents1(List.of(mother, father))
            .placementEventData(placementEventData)
            .placementList(
                asDynamicList(placementEventData.getPlacements(), placement.getId(), Placement::getChildName))
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "placement-application"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getPlacement()).isNotNull();
        assertThat(actualPlacementData.getPlacement().getChildId()).isEqualTo(child1.getId());
    }


}
