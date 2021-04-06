package uk.gov.hmcts.reform.fpl.service.orders.generator;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

public interface SingleOrderDocumentParameterGenerator {

    Order accept();

    DocmosisParameters generate(CaseDetails caseData);

    DocmosisTemplates template();
}
