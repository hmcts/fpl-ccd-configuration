package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.notification.GatekeepingOrderNotificationGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.Optional;

@Value
@Builder(toBuilder = true)
public class GatekeepingOrderEvent {
    CaseData caseData;
    DocumentReference order;
    GatekeepingOrderNotificationGroup notificationGroup;
    LanguageTranslationRequirement languageTranslationRequirement;

    public Optional<LanguageTranslationRequirement> getLanguageTranslationRequirement() {
        return Optional.ofNullable(languageTranslationRequirement);
    }
}
