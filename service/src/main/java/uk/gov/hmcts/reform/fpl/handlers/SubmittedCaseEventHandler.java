package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubmittedCaseEventHandler {

    private final CourtService courtService;
    private final NotificationService notificationService;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final OutsourcedCaseContentProvider outsourcedCaseContentProvider;
    private final PaymentService paymentService;
    private final EventService eventService;

    @Async
    @EventListener
    public void notifyAdmin(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        NotifyData notifyData = hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData);
        String recipient = courtService.getCourtEmail(caseData);

        notificationService.sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE, recipient, notifyData, caseData.getId()
        );
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

        if (!caseData.isOutsourced()) {
            return;
        }

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder().caseData(caseData).build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        NotifyData templateData = outsourcedCaseContentProvider.buildNotifyLAOnOutsourcedCaseTemplate(caseData);

        notificationService.sendEmail(OUTSOURCED_CASE_TEMPLATE, recipients, templateData, caseData.getId());
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
        OrderApplicant applicant = OrderApplicant.builder().name(caseData.getCaseLocalAuthorityName())
            .type(ApplicantType.LOCAL_AUTHORITY).build();
        eventService.publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION), applicant));
    }
}
