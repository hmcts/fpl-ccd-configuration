package uk.gov.hmcts.reform.fpl.events.judicial;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class HandleHearingModificationRolesEvent {

    private final CaseData caseData;
    private final CaseData caseDataBefore;

}
