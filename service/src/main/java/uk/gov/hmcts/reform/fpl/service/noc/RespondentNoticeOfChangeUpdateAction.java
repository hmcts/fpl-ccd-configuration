package uk.gov.hmcts.reform.fpl.service.noc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.Map;

@Component
public class RespondentNoticeOfChangeUpdateAction implements NoticeOfChangeUpdateAction {

    private static final SolicitorRole.Representing REPRESENTING = SolicitorRole.Representing.RESPONDENT;

    @Override
    public boolean accepts(SolicitorRole.Representing representing) {
        return REPRESENTING == representing;
    }

    @Override
    public Map<String, Object> applyUpdates(WithSolicitor respondent, CaseData caseData,
                                            RespondentSolicitor solicitor) {
        respondent.setSolicitor(solicitor);
        ((Respondent) respondent).setLegalRepresentation(YesNo.YES.getValue());

        return Map.of("respondents1", caseData.getAllRespondents());
    }
}
