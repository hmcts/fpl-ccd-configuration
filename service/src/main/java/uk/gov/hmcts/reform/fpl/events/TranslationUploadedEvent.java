package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Value
public class TranslationUploadedEvent implements ModifiedDocumentEvent {
    CaseData caseData;
    DocumentReference amendedDocument;
    String amendedOrderType;
    List<Element<Other>> selectedOthers;
}
