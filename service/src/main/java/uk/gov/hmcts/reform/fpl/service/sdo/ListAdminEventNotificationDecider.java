package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType;
import uk.gov.hmcts.reform.fpl.events.ListAdminEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.UDO;

@Component
public class ListAdminEventNotificationDecider {

    public Optional<ListAdminEvent> buildEventToPublish(CaseData caseData) {

        final GatekeepingOrderEventData gatekeepingOrderEventData = caseData.getGatekeepingOrderEventData();
        final StandardDirectionOrder directionOrder;
        final DirectionsOrderType directionsOrderType;

        if (nonNull(caseData.getGatekeepingOrderRouter())) {
            directionOrder = caseData.getStandardDirectionOrder();
            directionsOrderType = SDO;
        } else {
            directionOrder = caseData.getUrgentDirectionsOrder();
            directionsOrderType = UDO;
        }

        if (nonNull(gatekeepingOrderEventData) && gatekeepingOrderEventData.isSentToAdmin()
            && nonNull(directionOrder) && nonNull(directionOrder.getOrderDoc())) {
            return Optional.of(ListAdminEvent.builder()
                .caseData(caseData)
                .isSentToAdmin(gatekeepingOrderEventData.isSentToAdmin())
                .sendToAdminReason(gatekeepingOrderEventData.getSendToAdminReason())
                .order(directionOrder.getOrderDoc())
                .directionsOrderType(directionsOrderType)
                .build());
        }

        return Optional.empty();
    }
}
