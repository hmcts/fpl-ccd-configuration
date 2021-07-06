package uk.gov.hmcts.reform.fpl.service.orders.amendment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.action.AmendOrderAction;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendOrderService {
    private final AmendedOrderStamper stamper;
    private final List<AmendOrderAction> amendmentActions;

    public Map<String, Object> updateOrder(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        AmendOrderAction amendmentAction = amendmentActions.stream()
            .filter(action -> action.accept(caseData))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to amend order for order with id \"%s\"",
                eventData.getManageOrdersAmendmentList().getValueCode()
            )));

        DocumentReference stampedOrder = stamper.amendDocument(eventData.getManageOrdersAmendedOrder());

        return amendmentAction.applyAmendedOrder(caseData, stampedOrder);
    }
}
