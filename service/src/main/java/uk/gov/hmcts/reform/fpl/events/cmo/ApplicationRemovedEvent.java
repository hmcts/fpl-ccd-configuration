package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Data
public class ApplicationRemovedEvent {
    private final CaseData caseData;
    private final String removalReason;
}
