package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    public NoticeOfChangeAnswers generateForSubmission(Element<Respondent> respondentElement, String applicantName) {
        RespondentParty respondentParty = respondentElement.getValue().getParty();

        return NoticeOfChangeAnswers.builder()
            .respondentFirstName(respondentParty.getFirstName())
            .respondentLastName(respondentParty.getLastName())
            .applicantName(applicantName)
            .build();
    }
}
