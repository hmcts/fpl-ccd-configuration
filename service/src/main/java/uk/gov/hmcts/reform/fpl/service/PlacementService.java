package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper.toCCDMoneyGBP;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementService {

    private final Time time;
    private final FeeService feeService;
    private final DocumentSealingService sealingService;

    public PlacementEventData init(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final List<Element<Child>> childrenWithoutPlacement = getChildrenWithoutPlacement(caseData);

        if (childrenWithoutPlacement.size() == 1) {
            final Element<Child> child = childrenWithoutPlacement.get(0);

            final Placement placement = Placement.builder()
                .childName(child.getValue().asLabel())
                .childId(child.getId())
                .build();

            placementData.setPlacement(placement);
            placementData.setPlacementSingleChild(YES);
            placementData.setPlacementChildName(placement.getChildName());

        } else {
            placementData.setPlacementChildrenList(asDynamicList(childrenWithoutPlacement, Child::asLabel));
        }

        return placementData;
    }


    public PlacementEventData preparePlacement(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final DynamicList childrenList = placementData.getPlacementChildrenList();

        final Placement placement = Placement.builder()
            .childName(childrenList.getValueLabel())
            .childId(childrenList.getValueCodeAsUUID())
            .build();

        placementData.setPlacement(placement);
        placementData.setPlacementChildName(childrenList.getValueLabel());

        return placementData;
    }

    public PlacementEventData preparePayment(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final boolean isPaymentRequired = isPaymentRequired(placementData);

        placementData.setPlacementPaymentRequired(YesNo.from(isPaymentRequired));

        if (isPaymentRequired) {
            final FeesData feesData = feeService.getFeesDataForPlacement();
            placementData.setPlacementFee(toCCDMoneyGBP(feesData.getTotalAmount()));
        }

        return placementData;
    }

    public PlacementEventData savePlacement(CaseData caseData) {

        final PlacementEventData placementData = caseData.getPlacementEventData();

        final Placement placement = placementData.getPlacement();

        final DocumentReference sealedApplication = sealingService.sealDocument(placement.getApplication());

        placement.setApplication(sealedApplication);

        placementData.getPlacements().add(element(placement));

        return placementData;
    }

    private boolean isPaymentRequired(PlacementEventData eventData) {

        return ofNullable(eventData)
            .map(PlacementEventData::getPlacementLastPaymentTime)
            .map(LocalDateTime::toLocalDate)
            .map(lastPayment -> !lastPayment.isEqual(time.now().toLocalDate()))
            .orElse(true);
    }

    private List<Element<Child>> getChildrenWithoutPlacement(CaseData caseData) {
        final PlacementEventData eventData = caseData.getPlacementEventData();

        final List<UUID> childrenWithPlacement = eventData.getPlacements()
            .stream()
            .map(placement -> placement.getValue().getChildId())
            .collect(toList());

        return caseData.getAllChildren().stream()
            .filter(child -> !childrenWithPlacement.contains(child.getId()))
            .collect(toList());
    }
}
