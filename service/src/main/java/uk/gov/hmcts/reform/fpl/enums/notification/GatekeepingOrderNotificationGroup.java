package uk.gov.hmcts.reform.fpl.enums.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.SDO_AND_NOP_ISSUED_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_CAFCASS;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.URGENT_AND_NOP_ISSUED_LA;

@Getter
@AllArgsConstructor
public enum GatekeepingOrderNotificationGroup {
    SDO_AND_NOP(SDO_AND_NOP_ISSUED_LA, SDO_AND_NOP_ISSUED_CTSC, SDO_AND_NOP_ISSUED_CAFCASS),
    URGENT_AND_NOP(URGENT_AND_NOP_ISSUED_LA, URGENT_AND_NOP_ISSUED_CTSC, URGENT_AND_NOP_ISSUED_CAFCASS),
    SDO("","","");

    private final String laTemplate;
    private final String ctscTemplate;
    private final String cafcassTemplate;
}
