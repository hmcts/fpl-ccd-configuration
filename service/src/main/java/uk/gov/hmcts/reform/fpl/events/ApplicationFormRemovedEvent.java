package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Getter
@RequiredArgsConstructor
public class ApplicationFormRemovedEvent {
    private final CaseData caseData;
}
