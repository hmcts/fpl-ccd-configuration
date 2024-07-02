package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

@Value
public class NonMolestationOrderEvent  implements ManageOrdersEvent {
    CaseData caseData;
    ManageOrdersEventData eventData;
    String orderTitle;
    DocumentReference orderDocument;
    LanguageTranslationRequirement languageTranslationRequirement;
}
