package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Getter
@Builder
public class UpdateGuardianEvent {
    private final CaseData caseData;
}
