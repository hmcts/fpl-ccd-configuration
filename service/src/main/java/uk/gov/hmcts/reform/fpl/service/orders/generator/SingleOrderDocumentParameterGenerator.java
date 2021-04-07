package uk.gov.hmcts.reform.fpl.service.orders.generator;

import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

public interface SingleOrderDocumentParameterGenerator {

    Order accept();

    DocmosisParameters generate(CaseData caseData, OrderStatus status);

    DocmosisTemplates template();
}
