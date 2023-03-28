package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ListAdminEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.UDO;

@Component
public class ListAdminEventNotificationDecider {

    public Optional<ListAdminEvent> buildEventToPublish(CaseData caseData) {

        final GatekeepingOrderEventData gatekeepingOrderEventData = caseData.getGatekeepingOrderEventData();
        if (Objects.nonNull(gatekeepingOrderEventData) && gatekeepingOrderEventData.isSentToAdmin()) {
            final ListAdminEvent.ListAdminEventBuilder listAdminEventBuilder = ListAdminEvent.builder()
                .caseData(caseData)
                .isSentToAdmin(gatekeepingOrderEventData.isSentToAdmin())
                .sendToAdminReason(gatekeepingOrderEventData.getSendToAdminReason());

            final StandardDirectionOrder directionOrder;
            if (Objects.nonNull(caseData.getGatekeepingOrderRouter())) {
                directionOrder = caseData.getStandardDirectionOrder();
                listAdminEventBuilder.directionsOrderType(SDO);
            } else {
                directionOrder = caseData.getUrgentDirectionsOrder();
                listAdminEventBuilder.directionsOrderType(UDO);
            }

            if (Objects.nonNull(directionOrder) && Objects.nonNull(directionOrder.getOrderDoc())) {
                listAdminEventBuilder.order(directionOrder.getOrderDoc());
            }
            return Optional.of(listAdminEventBuilder.build());
        }

        return Optional.empty();
    }
}
