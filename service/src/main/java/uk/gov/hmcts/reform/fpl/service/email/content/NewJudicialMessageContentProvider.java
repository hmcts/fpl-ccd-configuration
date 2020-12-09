package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewJudicialMessageContentProvider extends AbstractEmailContentProvider {

    public NewJudicialMessageTemplate buildNewJudicialMessageTemplate(CaseData caseData,
                                                                      JudicialMessage judicialMessage) {
        NewJudicialMessageTemplate.NewJudicialMessageTemplateBuilder<?, ?> templateBuilder
            = NewJudicialMessageTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .callout(buildCallout(caseData))
            .sender(judicialMessage.getSender())
            .note(judicialMessage.getNote())
            .caseUrl(getCaseUrl(caseData.getId(), "JudicialMessagesTab"));

        if (judicialMessage.getUrgency() != null) {
            templateBuilder.hasUrgency(YES.getValue());
            templateBuilder.urgency(judicialMessage.getUrgency());
        }

        return templateBuilder.build();
    }
}
