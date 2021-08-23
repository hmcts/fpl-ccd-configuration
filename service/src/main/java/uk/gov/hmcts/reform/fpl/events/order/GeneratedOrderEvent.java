package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Optional;

@Value
public class GeneratedOrderEvent implements ManageOrdersEvent {
    CaseData caseData;
    DocumentReference orderDocument;
    LanguageTranslationRequirement languageTranslationRequirement;
    String orderTitle;

    public Optional<LanguageTranslationRequirement> getLanguageTranslationRequirement() {
        return Optional.ofNullable(languageTranslationRequirement);
    }
}
