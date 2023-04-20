package uk.gov.hmcts.reform.fpl.service.removeorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.removaltool.RemovableOrderOrApplicationNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.RemovedPlacement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemovePlacementApplicationService {
    private final ObjectMapper mapper;

    public DynamicList buildDynamicList(CaseData caseData) {
        return buildDynamicList(caseData, null);
    }

    public DynamicList buildDynamicList(CaseData caseData, UUID selected) {
        List<Element<Placement>> placements = defaultIfNull(
            caseData.getPlacementEventData().getPlacements(), new ArrayList<>());

        placements.sort(Comparator
            .comparing((Element<Placement> placement) -> placement.getValue().getPlacementUploadDateTime()));

        return asDynamicList(placements, selected, Placement::toLabel);
    }

    private Element<Placement> getSelectedPlacement(UUID removedPlacementId, List<Element<Placement>> placements) {
        return ElementUtils.findElement(removedPlacementId, placements)
            .orElseThrow(() -> new RemovableOrderOrApplicationNotFoundException(removedPlacementId));
    }

    public void populatePlacementApplication(CaseData caseData, CaseDetailsMap caseDetailsMap) {
        caseDetailsMap.put("placementApplicationToBeRemoved",
            getSelectedPlacement(getSelectedPlacementId(caseData), caseData.getPlacementEventData().getPlacements())
                .getValue());
    }

    public void removePlacementApplicationFromCase(CaseData caseData, CaseDetailsMap caseDetailsMap) {
        UUID removedPlacementId = getSelectedPlacementId(caseData);
        List<Element<Placement>> placements = caseData.getPlacementEventData().getPlacements();

        List<Element<Placement>> updatedPlacements = ElementUtils.removeElementWithUUID(placements, removedPlacementId);
        PlacementEventData updatedPlacementData = PlacementEventData.builder().placements(updatedPlacements).build();

        if (updatedPlacements.isEmpty()) {
            caseDetailsMap.remove("placements");
        } else {
            caseDetailsMap.put("placements", updatedPlacements);
        }

        List<Element<Placement>> placementsNonConfidential = updatedPlacementData.getPlacementsNonConfidential(false);
        if (placementsNonConfidential.isEmpty()) {
            caseDetailsMap.remove("placementsNonConfidential");
        } else {
            caseDetailsMap.put("placementsNonConfidential", placementsNonConfidential);
        }

        List<Element<Placement>> placementsNonConfidentialNotices = updatedPlacementData
            .getPlacementsNonConfidential(true);
        if (placementsNonConfidentialNotices.isEmpty()) {
            caseDetailsMap.remove("placementsNonConfidentialNotices");
        } else {
            caseDetailsMap.put("placementsNonConfidentialNotices", placementsNonConfidentialNotices);
        }

        Element<Placement> placementToBeRemoved = getSelectedPlacement(removedPlacementId, placements);

        List<Element<RemovedPlacement>> removedPlacements = caseData.getRemovalToolData().getRemovedPlacements();
        removedPlacements.add(element(removedPlacementId,
            RemovedPlacement.builder()
                .removalReason(caseData.getRemovalToolData().getReasonToRemovePlacementApplication())
                .placement(placementToBeRemoved.getValue())
                .build()));
        caseDetailsMap.put("removedPlacements", removedPlacements);
    }

    private UUID getSelectedPlacementId(CaseData caseData) {
        return getDynamicListSelectedValue(caseData.getRemovalToolData()
            .getRemovablePlacementApplicationList(), mapper);
    }
}
