package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FailedPBAPaymentEventHandler {
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final NotificationService notificationService;
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    private final FailedPBAPaymentContentProvider notificationContent;
    private final WorkAllocationTaskService workAllocationTaskService;
    private final FeatureToggleService featureToggleService;

    @EventListener
    public void notifyApplicant(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();
        if (event.getApplicationTypes().contains(ApplicationType.C110A_APPLICATION)) {
            notifyApplicants(caseData, event, APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA);
        } else {
            OrderApplicant applicant = event.getApplicant();
            if (applicant.getType() == ApplicantType.LOCAL_AUTHORITY) {
                notifyDesignatedLocalAuthority(event, INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT);
            } else if (applicant.getType() == ApplicantType.SECONDARY_LOCAL_AUTHORITY) {
                notifySecondaryLocalAuthority(event, INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT);
            } else {
                Map<String, String> emails = getRespondentsEmails(caseData);
                if (isNotEmpty(emails.get(applicant.getName()))) {
                    notifyRespondent(event, emails.get(applicant.getName()));
                }
            }
        }
    }

    private void notifyApplicants(CaseData caseData, FailedPBAPaymentEvent event, String template) {
        if (caseData.getRepresentativeType().equals(RepresentativeType.LOCAL_AUTHORITY)) {
            notifyDesignatedLocalAuthority(event, template);
        } else {
            notifySolicitor(caseData, event, template);
        }
    }

    private void notifyDesignatedLocalAuthority(FailedPBAPaymentEvent event, String template) {
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .legalRepresentativesExcluded(true)
            .secondaryLocalAuthorityExcluded(true)
            .caseData(event.getCaseData())
            .build();

        notifyLocalAuthority(event, template, recipientsRequest);
    }

    private void notifySecondaryLocalAuthority(FailedPBAPaymentEvent event, String template) {
        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .legalRepresentativesExcluded(true)
            .designatedLocalAuthorityExcluded(true)
            .caseData(event.getCaseData())
            .build();

        notifyLocalAuthority(event, template, recipientsRequest);
    }

    private void notifyLocalAuthority(FailedPBAPaymentEvent event, String template, RecipientsRequest request) {
        final CaseData caseData = event.getCaseData();

        final FailedPBANotificationData parameters = notificationContent
            .getApplicantNotifyData(event.getApplicationTypes(), event.getCaseData());

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(request);

        notificationService.sendEmail(template, recipients, parameters, caseData.getId().toString());
    }

    private void notifyRespondent(FailedPBAPaymentEvent event, String email) {
        FailedPBANotificationData parameters = notificationContent
            .getApplicantNotifyData(event.getApplicationTypes(), event.getCaseData());

        notificationService.sendEmail(INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            email, parameters, event.getCaseData().getId().toString());
    }

    private void notifySolicitor(CaseData caseData, FailedPBAPaymentEvent event, String template) {
        String email = nonNull(getApplicant(caseData)) ? getApplicant(caseData).getEmail() : null;

        if (isNull(email)) {
            return;
        }

        FailedPBANotificationData parameters = notificationContent
            .getApplicantNotifyData(event.getApplicationTypes(), event.getCaseData());

        notificationService.sendEmail(template, email, parameters, caseData.getId().toString());
    }

    private Map<String, String> getRespondentsEmails(CaseData caseData) {
        Map<String, String> respondentEmails = new HashMap<>();

        caseData.getAllRespondents().forEach(respondent -> respondentEmails.put(
            respondent.getValue().getParty().getFullName(),
            isNull(respondent.getValue().getSolicitor()) ? EMPTY : respondent.getValue().getSolicitor().getEmail()));

        return respondentEmails;
    }

    public LocalAuthority getApplicant(CaseData caseData) {
        return caseData.getLocalAuthorities().stream()
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    @EventListener
    public void notifyCTSC(FailedPBAPaymentEvent event) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            CaseData caseData = event.getCaseData();

            FailedPBANotificationData parameters = notificationContent.getCtscNotifyData(caseData,
                event.getApplicationTypes(), event.getApplicant().getName());

            String email = ctscEmailLookupConfiguration.getEmail();

            if (event.getApplicationTypes().contains(ApplicationType.C110A_APPLICATION)) {
                notificationService.sendEmail(APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
                    caseData.getId());
            } else {
                notificationService.sendEmail(INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC, email, parameters,
                    caseData.getId());
            }
        } else {
            log.info("WA EMAIL SKIPPED - failed payment - {}", event.getCaseData().getId());
        }
    }

    @EventListener
    public void createWorkAllocationTask(FailedPBAPaymentEvent event) {
        CaseData caseData = event.getCaseData();
        workAllocationTaskService.createWorkAllocationTask(caseData, WorkAllocationTaskType.FAILED_PAYMENT);
    }
}
