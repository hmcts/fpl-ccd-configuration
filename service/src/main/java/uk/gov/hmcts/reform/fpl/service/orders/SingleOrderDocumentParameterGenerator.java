package uk.gov.hmcts.reform.fpl.service.orders;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.order.Order;

public interface SingleOrderDocumentParameterGenerator {

    Order accept();

    DocmosisParameters generate(CaseDetails caseData);

    DocmosisTemplates template();
}
