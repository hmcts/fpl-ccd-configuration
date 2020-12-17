package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;

@Getter
@RequiredArgsConstructor
public class NewJudicialMessageReplyEvent {
    private final CaseData caseData;
    private final JudicialMessage judicialMessage;
}
