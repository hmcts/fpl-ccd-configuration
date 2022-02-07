package uk.gov.hmcts.reform.fpl.controllers.placementnotice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.PlacementNoticeController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(PlacementNoticeController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementNoticeAboutToStartControllerTest extends AbstractPlacementNoticeControllerTest {

    @Test
    void shouldIdentifyIfNoPlacements() {

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .respondents1(List.of(mother, father))
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getHasExistingPlacements()).isEqualTo(NO);
        assertThat(updatedCaseData.getPlacementList().getListItems().size()).isEqualTo(0);
        assertThat(actualPlacementData.getPlacement()).isNull();
    }

    @Test
    void shouldInstantiateListCorrectly() {

        final Placement placement = Placement.builder()
            .childId(child1.getId())
            .placementRespondentsToNotify(newArrayList(father))
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placement(placement)
            .placements(wrapElements(placement))
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .respondents1(List.of(mother, father))
            .placementEventData(placementEventData)
            .build();

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        assertThat(actualPlacementData.getHasExistingPlacements()).isEqualTo(YES);
        assertThat(actualPlacementData.getPlacements().size()).isEqualTo(1);
    }

}
