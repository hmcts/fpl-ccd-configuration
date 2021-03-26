package uk.gov.hmcts.reform.fpl.service.orders;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.order.Order;

public class Order2DocumentParameterGenerator implements SingleOrderDocumentParameterGenerator {
    @Override
    public Order accept() {
        return Order.ORDER_2;
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
        // EXAMPLE
        return DocmosisTemplates.NOTICE_OF_HEARING;
    }
}
