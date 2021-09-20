package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.util.List;

public interface AmendableListItemProvider {
    List<Element<? extends AmendableOrder>> provideListItems(CaseData caseData);
}
