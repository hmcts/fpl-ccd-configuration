package uk.gov.hmcts.reform.fpl.components;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.noticeofchange.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    public NoticeOfChangeAnswers generateForSubmission(Element<? extends WithSolicitor> respondentElement,
                                                       String applicantName) {
        Party respondentParty = respondentElement.getValue().toParty();

        return NoticeOfChangeAnswers.builder()
            .respondentFirstName(respondentParty.getFirstName())
            .respondentLastName(respondentParty.getLastName())
            .applicantName(applicantName)
            .build();
    }
}
