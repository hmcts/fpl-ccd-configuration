package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.ListAdminEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;

import java.util.Objects;
import java.util.Optional;

@Component
public class ListAdminEventNotificationDecider {

    public Optional<ListAdminEvent> buildEventToPublish(CaseData caseData) {

        final GatekeepingOrderEventData gatekeepingOrderEventData = caseData.getGatekeepingOrderEventData();
        if (Objects.nonNull(gatekeepingOrderEventData) && gatekeepingOrderEventData.isSentToAdmin()) {
            final ListAdminEvent.ListAdminEventBuilder listAdminEventBuilder = ListAdminEvent.builder()
                .caseData(caseData)
                .isSentToAdmin(gatekeepingOrderEventData.isSentToAdmin())
                .sendToAdminReason(gatekeepingOrderEventData.getSendToAdminReason());

            final StandardDirectionOrder standardDirectionOrder;
            if (Objects.nonNull(caseData.getGatekeepingOrderRouter())) {
                standardDirectionOrder = caseData.getStandardDirectionOrder();
            } else {
                standardDirectionOrder = caseData.getUrgentDirectionsOrder();
            }

            if (Objects.nonNull(standardDirectionOrder) && Objects.nonNull(standardDirectionOrder.getOrderDoc())) {
                listAdminEventBuilder.order(standardDirectionOrder.getOrderDoc());
            }
            return Optional.of(listAdminEventBuilder.build());
        }

        return Optional.empty();
    }
}
