package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemovalService {

    private final OrderRemovalActions orderRemovalActions;

    @SuppressWarnings("unchecked")
    public DynamicList buildDynamicListOfOrders(CaseData caseData, UUID selected) {
        List<Element<RemovableOrder>> blankOrders = getRemovableOrderList(caseData).stream()
            .filter(order -> order.getValue().isRemovable())
            .map(order -> (Element<RemovableOrder>) order)
            .collect(Collectors.toList());

        return asDynamicList(blankOrders, selected, RemovableOrder::asLabel);
    }

    public DynamicList buildDynamicListOfOrders(CaseData caseData) {
        return buildDynamicListOfOrders(caseData, null);
    }

    public void populateSelectedOrderFields(CaseData caseData,
                                            CaseDetailsMap data,
                                            UUID removedOrderId,
                                            RemovableOrder removableOrder) {
        orderRemovalActions.getAction(removableOrder)
            .populateCaseFields(caseData, data, removedOrderId, removableOrder);
    }

    public void removeOrderFromCase(CaseData caseData,
                                    CaseDetailsMap data,
                                    UUID removedOrderId,
                                    RemovableOrder removableOrder) {
        orderRemovalActions.getAction(removableOrder)
            .remove(caseData, data, removedOrderId, removableOrder);
    }

    public RemovableOrder getRemovedOrderByUUID(CaseData caseData, UUID removedOrderId) {
        return getRemovableOrderList(caseData).stream()
            .filter(orderElement -> removedOrderId.equals(orderElement.getId()))
            .map(Element::getValue)
            .findAny()
            .orElseThrow(() -> new RemovableOrderNotFoundException(removedOrderId));
    }

    public Element<AdditionalApplicationsBundle> getRemovedApplicationById(CaseData caseData, UUID selectedBundleId) {
        return caseData.getAdditionalApplicationsBundle().stream()
            .filter(orderElement -> selectedBundleId.equals(orderElement.getId()))
            .findAny()
            .orElseThrow(() -> new RemovableOrderNotFoundException(selectedBundleId));
    }

    public DynamicList buildDynamicListOfApplications(CaseData caseData) {
        return buildDynamicListOfApplications(caseData, null);
    }

    public DynamicList buildDynamicListOfApplications(CaseData caseData, UUID selected) {
        List<Element<AdditionalApplicationsBundle>> applications = caseData.getAdditionalApplicationsBundle();

        applications.sort(Comparator
            .comparing((Element<AdditionalApplicationsBundle> bundle) -> bundle.getValue().getUploadedDateTime()));

        return asDynamicList(applications, selected, AdditionalApplicationsBundle::toLabel);
    }

    public Map<String, Object> populateApplicationFields(Element<AdditionalApplicationsBundle> bundleElement) {
        return Map.of(
            "applicationTypeToBeRemoved", bundleElement.getValue().toLabel(),
            "c2ApplicationToBeRemoved", bundleElement.getValue().getC2DocumentBundle().getDocument(),
            "otherApplicationToBeRemoved", bundleElement.getValue().getOtherApplicationsBundle().getDocument(),
            "orderDateToBeRemoved", bundleElement.getValue().getUploadedDateTime()
        );

    }

    public Optional<StandardDirectionOrder> getRemovedSDO(
        List<Element<StandardDirectionOrder>> hiddenSDOs,
        List<Element<StandardDirectionOrder>> previousHiddenSDOs
    ) {
        return getRemovedOrder(hiddenSDOs, previousHiddenSDOs);
    }

    public Optional<HearingOrder> getRemovedCMO(
        List<Element<HearingOrder>> hiddenCMOs,
        List<Element<HearingOrder>> previousHiddenCMOs
    ) {
        return getRemovedOrder(hiddenCMOs, previousHiddenCMOs);
    }

    private <T extends RemovableOrder> Optional<T> getRemovedOrder(
        List<Element<T>> hiddenOrders, List<Element<T>> previousHiddenOrders
    ) {
        if (!Objects.equals(hiddenOrders, previousHiddenOrders)) {
            return hiddenOrders.stream()
                .filter(order -> !previousHiddenOrders.contains(order))
                .findFirst()
                .map(Element::getValue);
        }
        return Optional.empty();
    }

    private List<Element<? extends RemovableOrder>> getRemovableOrderList(CaseData caseData) {
        List<Element<? extends RemovableOrder>> orders = new ArrayList<>();
        orders.addAll(caseData.getOrderCollection());
        orders.addAll(caseData.getSealedCMOs());
        orders.addAll(getDraftHearingOrders(caseData));

        if (!FINAL_HEARING.equals(caseData.getState()) && caseData.getStandardDirectionOrder() != null) {
            StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();

            orders.add(element(standardDirectionOrder.getCollectionId(), standardDirectionOrder));
        }

        return orders;
    }

    private List<Element<HearingOrder>> getDraftHearingOrders(CaseData caseData) {
        List<Element<HearingOrder>> draftHearingOrders = caseData.getOrdersFromHearingOrderDraftsBundles();
        caseData.getDraftUploadedCMOs().forEach(
            draftCMO -> {
                if (draftHearingOrders.stream().noneMatch(cmo -> cmo.getId().equals(draftCMO.getId()))) {
                    draftHearingOrders.add(draftCMO);
                }
            }
        );

        return draftHearingOrders;
    }
}
