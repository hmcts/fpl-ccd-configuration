package uk.gov.hmcts.reform.fpl.events;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

@Data
public class NoticeOfChangeEvent {
    private final CaseData caseData;
    private final WithSolicitor oldParty;
    private final WithSolicitor newParty;
}
