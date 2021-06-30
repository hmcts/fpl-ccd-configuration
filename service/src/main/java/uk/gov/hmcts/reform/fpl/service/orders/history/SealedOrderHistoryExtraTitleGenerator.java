package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

@Component
public class SealedOrderHistoryExtraTitleGenerator {

    public String generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        switch (manageOrdersEventData.getManageOrdersType()) {
            case OTHER_ORDER:
                return manageOrdersEventData.getManageOrdersUploadTypeOtherTitle();
            case C21_BLANK_ORDER:
                return manageOrdersEventData.getManageOrdersTitle();
            default:
                return null;
        }
    }
}
