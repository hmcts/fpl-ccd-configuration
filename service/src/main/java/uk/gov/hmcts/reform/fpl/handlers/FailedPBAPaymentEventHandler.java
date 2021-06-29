package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FailedPBAPaymentEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final FailedPBAPaymentContentProvider notificationContent;

    @EventListener
    public void notifyApplicant(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        String applicant = defaultIfNull(event.getApplicantName(), "");

        if (ApplicationType.C110A_APPLICATION == event.getApplicationType()) {
            notifyLocalAuthority(event, APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA);
        } else if (ApplicationType.C2_APPLICATION == event.getApplicationType()) {
            if ((caseData.getCaseLocalAuthorityName() + ", Applicant").equals(applicant)) {
                notifyLocalAuthority(event, INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT);
            } else {
                Map<String, Collection<String>> emails = getRespondentsEmails(caseData);
                if (isNotEmpty(emails.get(applicant))) {
                    notifyRespondent(event, emails.get(applicant));
                }
            }
        }
    }

    private void notifyLocalAuthority(FailedPBAPaymentEvent event, String template) {
        CaseData caseData = event.getCaseData();

        FailedPBANotificationData parameters = notificationContent
            .getLocalAuthorityNotifyData(event.getApplicationType(), event.getCaseData().getId());

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .excludeLegalRepresentatives(true)
                .caseData(caseData)
                .build());

        notificationService.sendEmail(template, emails, parameters, caseData.getId().toString());
    }

    private void notifyRespondent(FailedPBAPaymentEvent event, Collection<String> emails) {
        FailedPBANotificationData parameters = notificationContent
            .getLocalAuthorityNotifyData(event.getApplicationType(), event.getCaseData().getId());

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            emails, parameters, event.getCaseData().getId().toString());
    }

    private Map<String, Collection<String>> getRespondentsEmails(CaseData caseData) {
        Map<String, Collection<String>> respondentEmails = new HashMap<>();

        IncrementalInteger i = new IncrementalInteger(1);
        caseData.getAllRespondents().forEach(respondent -> respondentEmails.put(
            respondent.getValue().getParty().getFullName() + ", Respondent " + i.getAndIncrement(),
            List.of(respondent.getValue().getSolicitor().getEmail())));

        return respondentEmails;
    }

    @EventListener
    public void notifyCTSC(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        FailedPBANotificationData parameters = notificationContent.getCtscNotifyData(caseData,
            event.getApplicationType(), event.getApplicantName());

        String email = ctscEmailLookupConfiguration.getEmail();

        if (event.getApplicationType() == ApplicationType.C110A_APPLICATION) {
            notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
                caseData.getId());
        } else {
            notificationService.sendEmail(INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
                caseData.getId());
        }
    }
}
