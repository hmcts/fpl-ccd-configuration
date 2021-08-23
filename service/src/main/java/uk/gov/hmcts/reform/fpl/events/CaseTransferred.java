package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Data
@Builder
@RequiredArgsConstructor
public class CaseTransferred {

    private final CaseData caseData;
    private final CaseData caseDataBefore;
}
