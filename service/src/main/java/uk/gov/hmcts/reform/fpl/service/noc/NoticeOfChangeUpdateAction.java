package uk.gov.hmcts.reform.fpl.service.noc;

import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.Map;

public interface NoticeOfChangeUpdateAction {
    boolean accepts(SolicitorRole.Representing representing);

    Map<String, Object> applyUpdates(WithSolicitor solicitorContainer, CaseData caseData, RespondentSolicitor solicitor);
}
