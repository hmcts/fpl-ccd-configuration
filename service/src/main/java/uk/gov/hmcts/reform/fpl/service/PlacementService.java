package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
public class PlacementService {

    public boolean hasSingleChild(CaseData caseData) {
        return caseData.getAllChildren().size() == 1;
    }

    public Element<Child> getChild(CaseData caseData, UUID childId) {
        return findElement(childId, caseData.getAllChildren()).orElse(null);
    }

    public DynamicList getChildrenList(CaseData caseData, Element<Child> selectedChild) {
        List<Element<Child>> children = caseData.getAllChildren();
        UUID selectedChildId = ofNullable(selectedChild).map(Element::getId).orElse(null);
        Function<Child, String> labelProducer = child -> child.getParty().getFullName();

        return ElementUtils.asDynamicList(children, selectedChildId, labelProducer);
    }

    public Placement getPlacement(CaseData caseData, Element<Child> child) {
        return findPlacement(caseData.getPlacements(), child.getId())
            .map(Element::getValue)
            .orElse(Placement.builder()
                .childId(child.getId())
                .childName(child.getValue().getParty().getFullName())
                .build());
    }

    public List<Element<Placement>> setPlacement(CaseData caseData, Placement placement) {
        List<Element<Placement>> placements = new ArrayList<>(caseData.getPlacements());

        findPlacement(placements, placement.getChildId())
            .ifPresentOrElse(existingPlacement -> {
                Element<Placement> newPlacement = element(existingPlacement.getId(), placement);
                placements.remove(existingPlacement);
                placements.add(newPlacement);
            }, () -> placements.add(element(placement)));

        return placements;
    }

    public List<Element<Placement>> withoutPlacementOrder(List<Element<Placement>> placements) {
        return placements.stream()
            .map(placement -> element(placement.getId(), placement.getValue().removePlacementOrder()))
            .collect(toList());
    }

    public List<Element<Placement>> withoutConfidentialData(List<Element<Placement>> placements) {
        return placements.stream()
            .map(placement -> element(placement.getId(), removeConfidentialDocuments(placement)))
            .collect(toList());
    }

    private Placement removeConfidentialDocuments(Element<Placement> placement) {
        return placement.getValue().removePlacementOrder().removeConfidentialDocuments();
    }

    private static Optional<Element<Placement>> findPlacement(List<Element<Placement>> placements, UUID childId) {
        return placements.stream()
            .filter(placement -> placement.getValue().getChildId().equals(childId))
            .findFirst();
    }
}
