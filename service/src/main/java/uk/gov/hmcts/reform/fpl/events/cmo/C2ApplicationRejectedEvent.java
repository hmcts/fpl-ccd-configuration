package uk.gov.hmcts.reform.fpl.events.cmo;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;

@Getter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class C2ApplicationRejectedEvent implements ReviewCMOEvent {
    private final CaseData caseData;
    private final AdditionalApplicationsBundle selectedAdditionalApplicationBundle;
    private final C2DocumentBundle c2DocumentRefused;
    private final String refusalOrderTitle;
}
