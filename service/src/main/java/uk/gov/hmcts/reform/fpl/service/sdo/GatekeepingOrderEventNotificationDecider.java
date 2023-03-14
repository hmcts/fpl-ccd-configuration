package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING_LISTING;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.SDO_AND_NOP;
import static uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup.URGENT_AND_NOP;

@Component
public class GatekeepingOrderEventNotificationDecider {

    public Optional<GatekeepingOrderEvent> buildEventToPublish(CaseData caseData, State previousState) {
        GatekeepingOrderEvent.GatekeepingOrderEventBuilder event = GatekeepingOrderEvent.builder().caseData(caseData);
        StandardDirectionOrder sdo = defaultIfNull(
            caseData.getStandardDirectionOrder(), StandardDirectionOrder.builder().build()
        );
        UrgentHearingOrder urgentHearingOrder = caseData.getUrgentHearingOrder();

        if (sdo.isDraft() && (null == urgentHearingOrder || !GATEKEEPING_LISTING.equals(previousState))) {
            return Optional.empty();
        }

        if (null != sdo.getOrderDoc()) {
            event.order(sdo.getOrderDoc());
            event.languageTranslationRequirement(sdo.getTranslationRequirements());
            // if we are in the gatekeeeping-listing state send the NoP related notifications
            event.notificationGroup(GATEKEEPING_LISTING.equals(previousState) ? SDO_AND_NOP : SDO);
            event.orderTitle(sdo.asLabel());
        } else {
            event.order(urgentHearingOrder.getOrder());
            event.notificationGroup(URGENT_AND_NOP);
            event.languageTranslationRequirement(urgentHearingOrder.getTranslationRequirements());
            event.orderTitle(urgentHearingOrder.asLabel());
        }

        return Optional.of(event.build());
    }
}
