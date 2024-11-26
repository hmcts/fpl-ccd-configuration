package uk.gov.hmcts.reform.fpl.service.cmo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersApproved;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftOrdersRejected;
import uk.gov.hmcts.reform.fpl.events.cmo.ReviewCMOEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class DraftOrdersEventNotificationBuilder {
    public List<ReviewCMOEvent> buildEventsToPublish(CaseData caseData) {
        List<Element<HearingOrder>> orders = caseData.getOrdersToBeSent();

        if (orders == null || orders.isEmpty()) {
            return emptyList();
        }

        Optional<Element<HearingOrder>> optionalCmo = orders.stream()
            .filter(order -> order.getValue().getType().isCmo()).findFirst();

        List<Element<HearingOrder>> c21s = orders.stream()
            .filter(order -> !order.getValue().getType().isCmo())
            .toList();

        //If CMO is the only approved/rejected order, then publish specific event for CMO (and generic for others)
        if (optionalCmo.isPresent()) {
            HearingOrder cmo = optionalCmo.get().getValue();
            if (cmo.getStatus().equals(APPROVED) && noC21sHaveStatus(c21s, APPROVED)) {
                return getCmoApprovedAndNoC21sApproved(caseData, c21s, cmo);
            }

            if (cmo.getStatus().equals(RETURNED) && noC21sHaveStatus(c21s, RETURNED)) {
                return getCmoRejectedAndNoC21sRejected(caseData, c21s, cmo);
            }
        }

        Map<CMOStatus, List<Element<HearingOrder>>> statusToOrder = orders.stream()
            .collect(groupingBy(element -> element.getValue().getStatus()));

        List<Element<HearingOrder>> approvedOrders = statusToOrder.getOrDefault(APPROVED, emptyList());
        List<Element<HearingOrder>> rejectedOrders = statusToOrder.getOrDefault(RETURNED, emptyList());
        List<ReviewCMOEvent> eventsToPublish = new ArrayList<>();

        if (!approvedOrders.isEmpty()) {
            eventsToPublish.add(new DraftOrdersApproved(caseData,
                unwrapElements(approvedOrders).stream().filter(order -> !order.isConfidentialOrder()).toList(),
                approvedOrders.stream().filter(order -> order.getValue().isConfidentialOrder()).toList()));
        }

        if (!rejectedOrders.isEmpty()) {
            eventsToPublish.add(new DraftOrdersRejected(caseData, unwrapElements(rejectedOrders)));
        }

        return eventsToPublish;
    }

    private List<ReviewCMOEvent> getCmoRejectedAndNoC21sRejected(CaseData caseData,
                                                                 List<Element<HearingOrder>> c21s,
                                                                 HearingOrder cmo) {
        List<ReviewCMOEvent> eventsToPublish = new ArrayList<>();

        eventsToPublish.add(new CaseManagementOrderRejectedEvent(caseData, cmo));

        if (anyC21sHaveStatus(c21s, APPROVED)) {
            eventsToPublish.add(new DraftOrdersApproved(caseData, unwrapElements(c21s), List.of()));
        }

        return eventsToPublish;
    }

    private List<ReviewCMOEvent> getCmoApprovedAndNoC21sApproved(CaseData caseData,
                                                                 List<Element<HearingOrder>> c21s,
                                                                 HearingOrder cmo) {
        List<ReviewCMOEvent> eventsToPublish = new ArrayList<>();

        eventsToPublish.add(new CaseManagementOrderIssuedEvent(caseData, cmo));

        if (anyC21sHaveStatus(c21s, RETURNED)) {
            eventsToPublish.add(new DraftOrdersRejected(caseData, unwrapElements(c21s)));
        }

        return eventsToPublish;
    }

    private boolean anyC21sHaveStatus(List<Element<HearingOrder>> c21s, CMOStatus status) {
        return c21s.stream().anyMatch(c21 -> c21.getValue().getStatus().equals(status));
    }

    private boolean noC21sHaveStatus(List<Element<HearingOrder>> c21s, CMOStatus status) {
        return c21s.stream().noneMatch(c21 -> c21.getValue().getStatus().equals(status));
    }
}
