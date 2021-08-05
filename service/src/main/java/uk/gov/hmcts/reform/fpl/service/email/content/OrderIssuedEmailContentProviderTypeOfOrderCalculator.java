package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.orders.history.SealedOrderHistoryService;

import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.GENERATED_ORDER;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderIssuedEmailContentProviderTypeOfOrderCalculator {

    private final SealedOrderHistoryService sealedOrderHistoryService;

    // This needs to be refactor, as we don't want to have business logic here, just passing the doc
    public String getTypeOfOrder(CaseData caseData, IssuedOrderType issuedOrderType) {
        if (issuedOrderType == GENERATED_ORDER) {
            GeneratedOrder lastGeneratedOrder = sealedOrderHistoryService.lastGeneratedOrder(caseData);
            if (lastGeneratedOrder.isNewVersion()) {
                if (lastGeneratedOrder.getType().equalsIgnoreCase("other")) {
                    return lastGeneratedOrder.getTitle();
                }
                return lastGeneratedOrder.getType().toLowerCase();
            }
            // legacy
            return Iterables.getLast(caseData.getOrderCollection()).getValue().getType().toLowerCase();
        }
        return issuedOrderType.getLabel().toLowerCase();
    }

}
