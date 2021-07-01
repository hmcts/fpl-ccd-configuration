package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

@Component
// TODO: pretty similar
public class ChildSolicitorNoticeOfChangeAnswersConverter {
    public NoticeOfChangeAnswers generateForSubmission(Element<Child> respondentElement, Applicant applicant) {


        ChildParty respondentParty = respondentElement.getValue().getParty();

        return NoticeOfChangeAnswers.builder()
                .respondentFirstName(respondentParty.getFirstName())
                .respondentLastName(respondentParty.getLastName())
                .applicantName(applicant.getParty().getOrganisationName())
                .build();
    }
}
