package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Getter
@RequiredArgsConstructor
public class SendOrderReminderEvent {

    private final CaseData caseData;

}
