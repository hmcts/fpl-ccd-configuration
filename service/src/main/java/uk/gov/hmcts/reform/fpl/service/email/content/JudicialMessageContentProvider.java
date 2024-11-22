package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.notify.NewJudicialMessageTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.JUDICIAL_MESSAGES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildCalloutWithNextHearing;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JudicialMessageContentProvider extends AbstractEmailContentProvider {
    private final Time time;
    private final EmailNotificationHelper helper;

    public NewJudicialMessageTemplate buildNewJudicialMessageTemplate(CaseData caseData,
                                                                      JudicialMessage judicialMessage) {
        return NewJudicialMessageTemplate.builder()
            .lastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .callout(buildCalloutWithNextHearing(caseData, time.now()))
            .sender(judicialMessage.getSender())
            .latestMessage(judicialMessage.getLatestMessage())
            .caseUrl(getCaseUrl(caseData.getId(), JUDICIAL_MESSAGES))
            .hasApplication(isNotEmpty(judicialMessage.getApplicationType()) ? YES.getValue() : NO.getValue())
            .applicationType(
                isNotEmpty(judicialMessage.getApplicationType()) ? judicialMessage.getApplicationType() : "")
            .hasUrgency(isNotEmpty(judicialMessage.getUrgency()) ? YES.getValue() : NO.getValue())
            .urgency(isNotEmpty(judicialMessage.getUrgency()) ? judicialMessage.getUrgency() : "")
            .build();
    }
}
