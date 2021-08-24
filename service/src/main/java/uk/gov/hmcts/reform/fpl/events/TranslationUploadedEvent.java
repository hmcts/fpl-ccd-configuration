package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class TranslationUploadedEvent implements ModifiedDocumentEvent {
    CaseData caseData;
    DocumentReference originalDocument;
    DocumentReference amendedDocument;
    String amendedOrderType;
    List<Element<Other>> selectedOthers;
    LanguageTranslationRequirement translationRequirements;

}
