package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Getter
@RequiredArgsConstructor
@Builder
public class AfterSubmissionCaseDataUpdated {

    private final CaseData caseData;
    private final CaseData caseDataBefore;

}
