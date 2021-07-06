package uk.gov.hmcts.reform.fpl.service.orders.amendment.action;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Map;

public interface AmendOrderAction {
    boolean accept(CaseData caseData);

    Map<String, Object> applyAmendedOrder(CaseData caseData, DocumentReference amendedOrder);
}
