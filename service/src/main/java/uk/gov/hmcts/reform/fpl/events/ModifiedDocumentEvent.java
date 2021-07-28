package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

public interface ModifiedDocumentEvent {
    CaseData getCaseData();

    DocumentReference getAmendedDocument();

    String getAmendedOrderType();

    List<Element<Other>> getSelectedOthers();
}
