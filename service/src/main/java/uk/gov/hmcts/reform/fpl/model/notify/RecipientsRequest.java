package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Value
@Builder
public class RecipientsRequest {

    CaseData caseData;

    boolean legalRepresentativesExcluded;
    boolean designatedLocalAuthorityExcluded;
    boolean secondaryLocalAuthorityExcluded;
}
