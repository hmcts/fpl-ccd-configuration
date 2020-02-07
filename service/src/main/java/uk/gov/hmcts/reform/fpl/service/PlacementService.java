package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Collections.emptyList;
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

    private static Optional<Element<Placement>> findPlacement(List<Element<Placement>> placements, UUID childId) {
        return placements.stream()
            .filter(placement -> placement.getValue().getChildId().equals(childId))
            .findFirst();
    }

    public List<String> getPlacementOrderAndNoticesDocumentUrls(List<Element<Placement>> placements,
                                                                PlacementOrderAndNoticesType type) {
        return placements.isEmpty() ? emptyList() : placements
            .stream()
            .map(x -> x.getValue().getOrderAndNotices())
            .flatMap(Collection::stream)
            .filter(y -> y.getValue().getType() == type)
            .map(element -> element.getValue().getDocument().getBinaryUrl())
            .collect(toList());
    }
}
