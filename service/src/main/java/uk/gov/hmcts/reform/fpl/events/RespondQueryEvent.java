package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Getter
@Builder
@RequiredArgsConstructor
public class RespondQueryEvent {
    private final CaseData caseData;
    private final String userId;
    private final String queryDate;
}
