package uk.gov.hmcts.reform.fpl.events.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Getter
@RequiredArgsConstructor
public class AdditonalAppLicationDraftOrderUploadedEvent {
    private final CaseData caseData;
    private final CaseData caseDataBefore;
}