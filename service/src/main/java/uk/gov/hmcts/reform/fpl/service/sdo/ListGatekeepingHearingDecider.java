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
        final StandardDirectionOrder standardDirectionOrder;

        if (nonNull(caseData.getGatekeepingOrderRouter())) {
            standardDirectionOrder = caseData.getStandardDirectionOrder();
            directionsOrderType = SDO;
        } else {
            standardDirectionOrder = caseData.getUrgentDirectionsOrder();
            directionsOrderType = UDO;
        }

        if (nonNull(standardDirectionOrder) && nonNull(standardDirectionOrder.getOrderDoc())) {
            return Optional.of(GatekeepingOrderEvent
                .builder()
                .caseData(caseData)
                .order(standardDirectionOrder.getOrderDoc()).order(standardDirectionOrder.getOrderDoc())
                .languageTranslationRequirement(standardDirectionOrder.getTranslationRequirements())
                .notificationGroup(SDO_OR_UDO_AND_NOP)
                .orderTitle(standardDirectionOrder.asLabel())
                .directionsOrderType(directionsOrderType)
                .build());
        }

        return Optional.empty();
    }
}
