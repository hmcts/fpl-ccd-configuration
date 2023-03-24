package uk.gov.hmcts.reform.fpl.enums.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_OR_UDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_OR_UDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_OR_UDO_AND_NOP_ISSUED_LA;

@Getter
@AllArgsConstructor
public enum GatekeepingOrderNotificationGroup {

    SDO_OR_UDO_AND_NOP(SDO_OR_UDO_AND_NOP_ISSUED_LA, SDO_OR_UDO_AND_NOP_ISSUED_CTSC, SDO_OR_UDO_AND_NOP_ISSUED_CAFCASS);

    private final String laTemplate;
    private final String ctscTemplate;
    private final String cafcassTemplate;
}
