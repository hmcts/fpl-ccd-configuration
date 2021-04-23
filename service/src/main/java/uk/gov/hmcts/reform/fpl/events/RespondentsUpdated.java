package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Getter
@RequiredArgsConstructor
public class RespondentsUpdated {
    private final CaseData caseData;
    private final CaseData caseDataBefore;

}
