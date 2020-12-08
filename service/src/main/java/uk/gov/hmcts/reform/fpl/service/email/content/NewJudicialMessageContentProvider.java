package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCallout;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewJudicialMessageContentProvider extends AbstractEmailContentProvider {

    public NewJudicialMessageTemplate buildNewJudicialMessageTemplate(CaseData caseData) {
        JudicialMessageMetaData judicialMessageMetaData = caseData.getJudicialMessageMetaData();

        NewJudicialMessageTemplate.NewJudicialMessageTemplateBuilder<?, ?> templateBuilder
            = NewJudicialMessageTemplate.builder()
            .respondentLastName(getFirstRespondentLastName(caseData))
            .callout(buildCallout(caseData))
            .sender(judicialMessageMetaData.getSender())
            .note(caseData.getJudicialMessageNote())
            .caseUrl(getCaseUrl(caseData.getId(), "JudicialMessagesTab"));

        if (judicialMessageMetaData.getUrgency() != null) {
            templateBuilder.hasUrgency(YES.getValue());
            templateBuilder.urgency(judicialMessageMetaData.getUrgency());
        }

        return templateBuilder.build();
    }
}
