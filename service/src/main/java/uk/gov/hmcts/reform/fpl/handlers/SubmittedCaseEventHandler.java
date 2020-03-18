package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubmittedCaseEventHandler {

    private final NotificationService notificationService;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public Map<String, Object> buildEmailTemplatePersonalisationForLocalAuthority(final EventData eventData) {
        return hmctsEmailContentProvider
            .buildHmctsSubmissionNotification(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());
    }

    public String getEmailRecipientForCafcass(final String localAuthority) {
        return cafcassLookupConfiguration.getCafcass(localAuthority).getEmail();
    }

    public Map<String, Object> buildEmailTemplatePersonalisationForCafcass(final EventData eventData) {
        return cafcassEmailContentProvider
            .buildCafcassSubmissionNotification(eventData.getCaseDetails(), eventData.getLocalAuthorityCode());
    }

    @EventListener
    public void sendEmailToHmctsAdmin(final SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);

        notificationService.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE,
            getHmctsAdminEmail(eventData),
            buildEmailTemplatePersonalisationForLocalAuthority(eventData),
            eventData.getReference());
    }

    @EventListener
    public void sendEmailToCafcass(final SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);

        notificationService.sendEmail(CAFCASS_SUBMISSION_TEMPLATE,
            getEmailRecipientForCafcass(eventData.getLocalAuthorityCode()),
            buildEmailTemplatePersonalisationForCafcass(eventData),
            eventData.getReference());
    }

    private String getHmctsAdminEmail(EventData eventData) {
        String ctscValue = getCtscValue(eventData.getCaseDetails().getData());

        if (ctscValue.equals("Yes")) {
            return ctscEmailLookupConfiguration.getEmail();
        }

        return hmctsCourtLookupConfiguration.getCourt(eventData.getLocalAuthorityCode()).getEmail();
    }

    private String getCtscValue(Map<String, Object> caseData) {
        return caseData.get("sendToCtsc") != null ? caseData.get("sendToCtsc").toString() : "No";
    }
}
