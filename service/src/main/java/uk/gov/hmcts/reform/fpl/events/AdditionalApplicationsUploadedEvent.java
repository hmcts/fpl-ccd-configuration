package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

@Getter
@RequiredArgsConstructor
public class AdditionalApplicationsUploadedEvent {
    private final CaseData caseData;
    private final C2DocumentBundle uploadedBundle;
}
