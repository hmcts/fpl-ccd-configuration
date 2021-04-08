package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.LocalAuthorityInboxRecipientsRequest;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.OutsourcedCaseTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.RespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.InboxLookupService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.RespondentSolicitorContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubmittedCaseEventHandler {
    private final NotificationService notificationService;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final InboxLookupService inboxLookupService;
    private final OutsourcedCaseContentProvider outsourcedCaseContentProvider;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RespondentSolicitorContentProvider respondentSolicitorContentProvider;

    @Async
    @EventListener
    public void notifyRegisteredRespondentSolicitors(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        List<RespondentSolicitor> registeredSolicitors = getRegisteredSolicitors(caseData);

        for (RespondentSolicitor registeredSolicitor : registeredSolicitors) {
            RespondentSolicitorTemplate notifyData = respondentSolicitorContentProvider
                .buildRespondentSolicitorSubmissionNotification(caseData, registeredSolicitor);

            notificationService.sendEmail(
                REGISTERED_RESPONDENT_SUBMISSION_TEMPLATE,
                registeredSolicitor.getEmail(),
                notifyData,
                caseData.getId()
            );
        }
    }

    @Async
    @EventListener
    public void notifyAdmin(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData);
        String recipient = adminNotificationHandler.getHmctsAdminEmail(caseData);

        notificationService.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyCafcass(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = cafcassEmailContentProvider.buildCafcassSubmissionNotification(caseData);
        String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService.sendEmail(CAFCASS_SUBMISSION_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    @EventListener
    public void notifyManagedLA(SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        if (caseData.getOutsourcingPolicy() == null) {
            return;
        }

        Collection<String> emails = inboxLookupService.getRecipients(
            LocalAuthorityInboxRecipientsRequest.builder().caseData(caseData).build());

        OutsourcedCaseTemplate templateData
            = outsourcedCaseContentProvider.buildNotifyLAOnOutsourcedCaseTemplate(caseData);

        notificationService.sendEmail(OUTSOURCED_CASE_TEMPLATE, emails, templateData, caseData.getId().toString());
    }

    @Async
    @EventListener
    public void makePayment(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        if (event.getCaseDataBefore().getState() != State.OPEN) {
            log.info("Payment not taken for case {} due to not open state.", caseData.getId());
            return;
        }

        if (YES == getPaymentDecision(caseData)) {
            makePaymentForCaseOrders(caseData);
        } else {
            handlePaymentNotTaken(caseData);
        }
    }

    private List<RespondentSolicitor> getRegisteredSolicitors(CaseData caseData) {
        return unwrapElements(caseData.getRespondents1())
            .stream()
            .filter(this::isRegisteredSolicitor)
            .map(Respondent::getSolicitor)
            .collect(Collectors.toUnmodifiableList());
    }

    private boolean isRegisteredSolicitor(Respondent respondent) {
        return StringUtils.equals(respondent.getLegalRepresentation(), YES.getValue())
            && !isNull(respondent.getSolicitor())
            && isNotEmpty(respondent.getSolicitor().getEmail())
            && !isNull(respondent.getSolicitor().getOrganisation())
            && isNotEmpty(respondent.getSolicitor().getOrganisation().getOrganisationID());
    }

    private void makePaymentForCaseOrders(final CaseData caseData) {
        try {
            paymentService.makePaymentForCaseOrders(caseData);
        } catch (FeeRegisterException | PaymentsApiException ignore) {
            handlePaymentNotTaken(caseData);
        }
    }

    private YesNo getPaymentDecision(CaseData caseData) {
        return YesNo.fromString(ObjectUtils.defaultIfNull(caseData.getDisplayAmountToPay(), ""));
    }

    private void handlePaymentNotTaken(CaseData caseData) {
        log.error("Payment not taken for case {}.", caseData.getId());
        applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(caseData, C110A_APPLICATION));
    }
}
