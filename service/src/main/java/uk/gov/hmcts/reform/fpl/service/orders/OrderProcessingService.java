package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.AmendOrderService;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.AMEND;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OrderProcessingService {
    private final AmendOrderService amendOrderService;
    private final SealedOrderHistoryService historyService;

    public Map<String, Object> process(CaseData caseData) {
        return AMEND == caseData.getManageOrdersEventData().getManageOrdersOperation()
               ? amendOrderService.updateOrder(caseData)
               : historyService.generate(caseData);
    }
}
