package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.events.order.ManageOrdersEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AmendedOrderEvent implements ManageOrdersEvent {
    private final CaseData caseData;
    private final DocumentReference amendedDocument;
    private final String amendedOrderType;
    private final List<Element<Other>> selectedOthers;
}
