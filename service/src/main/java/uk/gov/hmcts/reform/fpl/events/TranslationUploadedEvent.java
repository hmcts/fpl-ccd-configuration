package uk.gov.hmcts.reform.fpl.events;

import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
public class TranslationUploadedEvent {
    CaseData caseData;
}
