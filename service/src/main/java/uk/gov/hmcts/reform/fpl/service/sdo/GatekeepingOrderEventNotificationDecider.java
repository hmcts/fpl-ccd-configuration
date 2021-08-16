package uk.gov.hmcts.reform.fpl.service.sdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.GatekeepingOrderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
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

        if (sdo.isDraft() && (null == urgentHearingOrder || !isInGatekeeping(previousState))) {
            return Optional.empty();
        }

        if (null != sdo.getOrderDoc()) {
            event.order(sdo.getOrderDoc());
            event.languageTranslationRequirement(sdo.getTranslationRequirements());
            // if we are in the gatekeeping state send the NoP related notifications
            event.notificationGroup(isInGatekeeping(previousState) ? SDO_AND_NOP : SDO);
            event.orderTitle(sdo.asLabel());
        } else {
            event.order(urgentHearingOrder.getOrder());
            event.notificationGroup(URGENT_AND_NOP);
            event.languageTranslationRequirement(urgentHearingOrder.getTranslationRequirements());
            event.orderTitle(urgentHearingOrder.asLabel());
        }

        return Optional.of(event.build());
    }

    private boolean isInGatekeeping(State previousState) {
        return GATEKEEPING == previousState;
    }
}
