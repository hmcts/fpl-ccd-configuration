package uk.gov.hmcts.reform.fpl.service.orders.generator;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;

public interface OrderAdditionalDocumentsHolder {

    Order accept();

    List<DocumentReference> additionalDocuments(CaseData caseData);
}
