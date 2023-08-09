package uk.gov.hmcts.reform.fpl.service.removeorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.RemovalToolData;
import uk.gov.hmcts.reform.fpl.model.RemovedPlacement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class RemovePlacementApplicationServiceTest {

    private static final Element<Placement> PLACEMENT_1 =
        element(Placement.builder()
            .placementUploadDateTime(LocalDateTime.now().minusDays(1))
            .childName("Child one")
            .build());
    private static final Element<Placement> PLACEMENT_2 =
        element(Placement.builder()
            .placementUploadDateTime(LocalDateTime.now().minusDays(2))
            .childName("Child two")
            .build());
    private static final Element<Placement> PLACEMENT_TO_BE_REMOVED =
        element(Placement.builder()
            .placementUploadDateTime(LocalDateTime.now())
            .childName("Child three")
            .build());

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RemovePlacementApplicationService underTest;

    //@Test
    void shouldBuildSortedDynamicList() {
        CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placements(new ArrayList<>(List.of(PLACEMENT_1, PLACEMENT_2, PLACEMENT_TO_BE_REMOVED)))
                .build())
            .build();

        DynamicList listOfPlacement = underTest.buildDynamicList(caseData);

        DynamicList expectedList = ElementUtils
            .asDynamicList(
                List.of(PLACEMENT_2, PLACEMENT_1, PLACEMENT_TO_BE_REMOVED),
                Placement::toLabel);

        assertThat(listOfPlacement).isEqualTo(expectedList);
    }

    //@Test
    void shouldPopulatePlacementApplication() {
        CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placements(new ArrayList<>(List.of(PLACEMENT_1, PLACEMENT_2, PLACEMENT_TO_BE_REMOVED)))
                .build())
            .removalToolData(RemovalToolData.builder()
                .removablePlacementApplicationList(
                    ElementUtils
                        .asDynamicList(
                            List.of(PLACEMENT_2, PLACEMENT_1, PLACEMENT_TO_BE_REMOVED),
                            PLACEMENT_TO_BE_REMOVED.getId(),
                            Placement::toLabel)
                )
                .build())
            .build();

        CaseDetailsMap actualCaseDetailMap =  CaseDetailsMap.caseDetailsMap(new HashMap<>());
        underTest.populatePlacementApplication(caseData, actualCaseDetailMap);

        assertThat(actualCaseDetailMap).isEqualTo(CaseDetailsMap.caseDetailsMap(Map.of(
            "placementApplicationToBeRemoved", PLACEMENT_TO_BE_REMOVED.getValue()
        )));
    }

    //@Test
    void shouldRemoveSelectedPlacementApplicationFromCase() {
        CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placements(new ArrayList<>(List.of(PLACEMENT_1, PLACEMENT_2, PLACEMENT_TO_BE_REMOVED)))
                .build())
            .removalToolData(RemovalToolData.builder()
                .reasonToRemovePlacementApplication("remove reason")
                .removablePlacementApplicationList(
                    ElementUtils
                        .asDynamicList(
                            List.of(PLACEMENT_2, PLACEMENT_1, PLACEMENT_TO_BE_REMOVED),
                            PLACEMENT_TO_BE_REMOVED.getId(),
                            Placement::toLabel)
                )
                .build())
            .build();

        CaseDetailsMap actualCaseDetailMap =  CaseDetailsMap.caseDetailsMap(new HashMap<>());
        underTest.removePlacementApplicationFromCase(caseData, actualCaseDetailMap);

        PlacementEventData expectedEventData = PlacementEventData.builder()
            .placements(List.of(PLACEMENT_1, PLACEMENT_2))
            .build();

        assertThat(actualCaseDetailMap).isEqualTo(
            CaseDetailsMap.caseDetailsMap(Map.of(
                "placements", expectedEventData.getPlacements(),
                "placementsNonConfidential", expectedEventData.getPlacementsNonConfidential(false),
                "placementsNonConfidentialNotices", expectedEventData.getPlacementsNonConfidential(true),
                "removedPlacements", List.of(element(PLACEMENT_TO_BE_REMOVED.getId(),
                    RemovedPlacement.builder()
                        .removalReason("remove reason")
                        .placement(PLACEMENT_TO_BE_REMOVED.getValue())
                        .build()))
            )));
    }

    //@Test
    void shouldRemoveSelectedPlacementApplicationFromCaseIfOnlyOnePlacementExist() {
        CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placements(new ArrayList<>(List.of(PLACEMENT_TO_BE_REMOVED)))
                .build())
            .removalToolData(RemovalToolData.builder()
                .reasonToRemovePlacementApplication("remove reason")
                .removablePlacementApplicationList(
                    ElementUtils
                        .asDynamicList(
                            List.of(PLACEMENT_TO_BE_REMOVED),
                            PLACEMENT_TO_BE_REMOVED.getId(),
                            Placement::toLabel)
                )
                .build())
            .build();

        CaseDetailsMap actualCaseDetailMap =  CaseDetailsMap.caseDetailsMap(Map.of(
            "placements", caseData.getPlacementEventData().getPlacements(),
            "placementsNonConfidential", caseData.getPlacementEventData().getPlacementsNonConfidential(false),
            "placementsNonConfidentialNotices", caseData.getPlacementEventData().getPlacementsNonConfidential(true)
        ));
        underTest.removePlacementApplicationFromCase(caseData, actualCaseDetailMap);

        assertThat(actualCaseDetailMap).isEqualTo(
            CaseDetailsMap.caseDetailsMap(Map.of(
                "removedPlacements", List.of(element(PLACEMENT_TO_BE_REMOVED.getId(),
                    RemovedPlacement.builder()
                        .removalReason("remove reason")
                        .placement(PLACEMENT_TO_BE_REMOVED.getValue())
                        .build()))
            )));
    }
}
