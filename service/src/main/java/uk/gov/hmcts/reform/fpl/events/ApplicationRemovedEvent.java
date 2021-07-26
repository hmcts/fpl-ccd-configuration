package uk.gov.hmcts.reform.fpl.events;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;

@Getter
@RequiredArgsConstructor
public class ApplicationRemovedEvent {
    private final CaseData caseData;
    private final AdditionalApplicationsBundle removedApplication;
}
