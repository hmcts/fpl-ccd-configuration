package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageReplyTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialMessageReplyContentProvider extends AbstractEmailContentProvider {


    public NewJudicialMessageReplyTemplate buildNewJudicialMessageReplyTemplate(CaseData caseData,
                                                                           JudicialMessage judicialMessage) {
        NewJudicialMessageReplyTemplate.NewJudicialMessageReplyTemplateBuilder<?, ?> templateBuilder
            = NewJudicialMessageReplyTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .callout(buildCallout(caseData))
            .latestMessage(judicialMessage.getLatestMessage())
            .caseUrl(getCaseUrl(caseData.getId(), "JudicialMessagesTab"));

        return templateBuilder.build();
    }
}
