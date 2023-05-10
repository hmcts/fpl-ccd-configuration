package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;

import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.UDO;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_OR_UDO_AND_NOP;

@Component
public class ListGatekeepingHearingDecider {

    public Optional<GatekeepingOrderEvent> buildEventToPublish(final CaseData caseData) {

        final DirectionsOrderType directionsOrderType;
        final StandardDirectionOrder directionOrder;

        if (nonNull(caseData.getGatekeepingOrderRouter())) {
            directionOrder = caseData.getStandardDirectionOrder();
            directionsOrderType = SDO;
        } else {
            directionOrder = caseData.getUrgentDirectionsOrder();
            directionsOrderType = UDO;
        }

        if (nonNull(directionOrder) && nonNull(directionOrder.getOrderDoc())) {
            return Optional.of(GatekeepingOrderEvent
                .builder()
                .caseData(caseData)
                .order(directionOrder.getOrderDoc())
                .languageTranslationRequirement(directionOrder.getTranslationRequirements())
                .notificationGroup(SDO_OR_UDO_AND_NOP)
                .orderTitle(directionOrder.asLabel())
                .directionsOrderType(directionsOrderType)
                .build());
        }

        return Optional.empty();
    }
}
