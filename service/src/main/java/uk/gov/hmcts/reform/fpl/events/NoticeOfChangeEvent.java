package uk.gov.hmcts.reform.fpl.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class NoticeOfChangeEvent {
    private final CaseData caseData;
    private final ConfidentialParty<?> oldRespondent;
    private final ConfidentialParty<?> newRespondent;
}
