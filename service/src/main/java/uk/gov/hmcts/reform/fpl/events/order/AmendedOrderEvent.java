package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
public class AmendedOrderEvent implements ManageOrdersEvent {
    private final CaseData caseData;
    private final DocumentReference amendedDocument;
    private final String amendedOrderType;
    private final List<Element<Other>> selectedOthers;
}
