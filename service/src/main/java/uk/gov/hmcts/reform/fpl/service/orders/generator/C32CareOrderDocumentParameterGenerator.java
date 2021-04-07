package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C32CareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

@Component
public class C32CareOrderDocumentParameterGenerator implements SingleOrderDocumentParameterGenerator {
    @Override
    public Order accept() {
        return Order.C32_CARE_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData, OrderStatus status) {

        return C32CareOrderDocmosisParameters.builder()
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }
}
