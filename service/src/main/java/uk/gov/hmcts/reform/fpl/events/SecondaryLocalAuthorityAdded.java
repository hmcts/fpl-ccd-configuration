package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Data
@Builder
@RequiredArgsConstructor
public class SecondaryLocalAuthorityAdded {

    private final CaseData caseData;
}
