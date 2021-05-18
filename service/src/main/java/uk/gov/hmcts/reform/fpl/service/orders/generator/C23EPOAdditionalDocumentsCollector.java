package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C23EPOAdditionalDocumentsCollector implements AdditionalDocumentsCollector {

    @Override
    public Order accept() {
        return Order.C23_EMERGENCY_PROTECTION_ORDER;
    }

    @Override
    public List<DocumentReference> additionalDocuments(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        if (isNull(eventData.getManageOrdersPowerOfArrest())) {
            return List.of();
        }

        return List.of(eventData.getManageOrdersPowerOfArrest());
    }

}
