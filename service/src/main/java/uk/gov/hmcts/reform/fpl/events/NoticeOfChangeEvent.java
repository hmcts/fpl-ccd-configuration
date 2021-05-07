package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class NoticeOfChangeEvent {
    private final CaseData caseData;
    private final RespondentSolicitor oldRespondentSolicitor;
    private final RespondentSolicitor newRespondentSolicitor;
}
