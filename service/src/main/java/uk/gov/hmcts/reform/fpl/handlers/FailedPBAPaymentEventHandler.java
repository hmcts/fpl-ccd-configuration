package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.InterlocutoryApplicant;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FailedPBAPaymentEventHandler {
    private final InboxLookupService inboxLookupService;
    private final NotificationService notificationService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final FailedPBAPaymentContentProvider notificationContent;

    @EventListener
    public void notifyLocalAuthority(FailedPBAPaymentEvent event, Collection<String> emails) {
        CaseData caseData = event.getCaseData();

        FailedPBANotificationData parameters = notificationContent
            .getLocalAuthorityNotifyData(event.getApplicationType());

        /*Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder()
                .excludeLegalRepresentatives(true)
                .caseData(caseData)
                .build());*/

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA, emails, parameters,
            caseData.getId().toString());
    }

    private Map<String, Collection<String>> getApplicantsEmails(CaseData caseData) {

        Map<String, Collection<String>> applicantsEmails = new HashMap<>();
        List<InterlocutoryApplicant> applicantsFullNames = new ArrayList<>();

        // Main applicant
        if (isNotEmpty(caseData.getCaseLocalAuthorityName())) {
            Collection<String> emails = inboxLookupService.getRecipients(
                LocalAuthorityInboxRecipientsRequest.builder()
                    .excludeLegalRepresentatives(true)
                    .caseData(caseData)
                    .build());

            applicantsEmails.put(caseData.getCaseLocalAuthorityName() + ", Applicant", emails);
        }

        // respondents emails
        IncrementalInteger i = new IncrementalInteger(1);
        caseData.getAllRespondents().forEach(respondent -> applicantsEmails.put(
            respondent.getValue().getParty().getFullName() + ", Respondent " + i.getAndIncrement(),
            List.of(respondent.getValue().getSolicitor().getEmail())));

        return applicantsEmails;
    }

    @EventListener
    public void notifyApplicant(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        Map<String, Collection<String>> emails = getApplicantsEmails(caseData);

        if ((caseData.getCaseLocalAuthorityName() + ", Applicant").equals(event.getApplicant())) {
            notifyLocalAuthority(event, emails.get(caseData.getCaseLocalAuthorityName() + ", Applicant"));
        } else {

        }

    }

    Collection<String> emails = inboxLookupService.getRecipients(
        LocalAuthorityInboxRecipientsRequest.builder()
            .excludeLegalRepresentatives(true)
            .caseData(caseData)
            .build());

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,emails,parameters,
        caseData.getId().

    toString());
}

    @EventListener
    public void notifyCTSC(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();

        FailedPBANotificationData parameters = notificationContent.getCtscNotifyData(caseData,
            event.getApplicationType());

        String email = ctscEmailLookupConfiguration.getEmail();

        notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
            caseData.getId());
    }
}
