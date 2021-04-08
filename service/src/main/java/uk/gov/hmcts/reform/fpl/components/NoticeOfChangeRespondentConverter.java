package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;


@Component
public class NoticeOfChangeRespondentConverter {
    public NoticeOfChangeAnswers convert(Element<Respondent> respondentElement, Applicant applicant) {
        RespondentParty respondentParty = respondentElement.getValue().getParty();

        return NoticeOfChangeAnswers.builder()
                .respondentFirstName(respondentParty.getFirstName())
                .respondentLastName(respondentParty.getLastName())
                .respondentDOB(respondentParty.getDateOfBirth())
                .applicantName(applicant.getParty().getOrganisationName())
                .policyReference(respondentElement.getValue().getPolicyReference())
                .build();
    }
}
