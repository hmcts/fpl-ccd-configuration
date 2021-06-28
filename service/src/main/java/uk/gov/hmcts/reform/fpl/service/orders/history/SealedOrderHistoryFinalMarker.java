package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

@Component
public class SealedOrderHistoryFinalMarker {

    public YesNo calculate(CaseData caseData) {

        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        Order order = eventData.getManageOrdersType();

        switch (order.getIsFinalOrder()){
            case YES:
                return YesNo.YES;
            case MAYBE:
                return YesNo.fromString(eventData.getManageOrdersIsFinalOrder());
            case NO:
            default:
                return YesNo.NO;
        }
    }
}
