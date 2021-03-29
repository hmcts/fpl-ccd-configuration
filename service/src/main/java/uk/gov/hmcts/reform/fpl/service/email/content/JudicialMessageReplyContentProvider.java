package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.JudicialMessageReplyTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import static org.apache.logging.log4j.util.Strings.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.JUDICIAL_MESSAGES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JudicialMessageReplyContentProvider extends AbstractEmailContentProvider {
    private final Time time;

    public JudicialMessageReplyTemplate buildJudicialMessageReplyTemplate(CaseData caseData,
                                                                          JudicialMessage judicialMessage) {
        JudicialMessageReplyTemplate.JudicialMessageReplyTemplateBuilder<?, ?> templateBuilder =
            JudicialMessageReplyTemplate.builder()
                .respondentLastName(getFirstRespondentLastName(caseData))
                .callout(buildCalloutWithNextHearing(caseData, time.now()))
                .latestMessage(judicialMessage.getLatestMessage())
                .caseUrl(getCaseUrl(caseData.getId(), JUDICIAL_MESSAGES));

        if (isNotEmpty(judicialMessage.getApplicationType())) {
            templateBuilder.hasApplication(YES.getValue());
            templateBuilder.applicationType(judicialMessage.getApplicationType());
        } else {
            templateBuilder.hasApplication(NO.getValue());
            templateBuilder.applicationType("");
        }

        return templateBuilder.build();
    }
}
