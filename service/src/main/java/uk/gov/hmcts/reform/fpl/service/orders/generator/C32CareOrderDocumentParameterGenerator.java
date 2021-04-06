package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.BaseDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

@Component
public class C32CareOrderDocumentParameterGenerator implements SingleOrderDocumentParameterGenerator {
    @Override
    public Order accept() {
        return Order.C32_CARE_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseDetails caseData) {
        return BaseDocmosisParameters.builder()
            .title("title 1")
            .whatever("whatever")
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }
}
