package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Value
@Builder
public class GatekeepingOrderEvent {
    CaseData caseData;
    DocumentReference order;
    GatekeepingOrderNotificationGroup notificationGroup;
}
