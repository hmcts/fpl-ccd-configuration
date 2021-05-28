package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;

@Component
public class ManageOrdersCaseDataFixer {

    // this is to workaround an EXUI bug that don't retain hidden fields
    public CaseData fix(CaseData caseData) {

        if (State.CLOSED == caseData.getState()) {
            ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
            return caseData.toBuilder().manageOrdersEventData(
                manageOrdersEventData.toBuilder().manageOrdersType(C21_BLANK_ORDER).build()
            ).build();
        }

        return caseData;
    }

}
