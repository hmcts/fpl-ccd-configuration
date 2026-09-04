package uk.gov.hmcts.reform.fpl.events;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;

@Data
public class NoticeOfChangeThirdPartyEvent {
    private final LocalAuthority oldThirdPartyOrg;
    private final LocalAuthority newThirdPartyOrg;
    private final CaseData caseData;
}
