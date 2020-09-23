package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubmittedCaseEventHandler {
    private final NotificationService notificationService;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final HmctsAdminNotificationHandler adminNotificationHandler;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Async
    @EventListener
    public void sendEmailToHmctsAdmin(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        notificationService.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE,
            adminNotificationHandler.getHmctsAdminEmail(caseData),
            buildEmailTemplatePersonalisationForLocalAuthority(caseData),
            caseData.getId().toString());
    }

    @Async
    @EventListener
    public void sendEmailToCafcass(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        notificationService.sendEmail(CAFCASS_SUBMISSION_TEMPLATE,
            getEmailRecipientForCafcass(caseData.getCaseLocalAuthority()),
            buildEmailTemplatePersonalisationForCafcass(caseData),
            caseData.getId().toString());
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
        applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(caseData, C110A_APPLICATION));
    }

    private SubmitCaseHmctsTemplate buildEmailTemplatePersonalisationForLocalAuthority(final CaseData caseData) {
        return hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData);
    }

    private String getEmailRecipientForCafcass(final String localAuthority) {
        return cafcassLookupConfiguration.getCafcass(localAuthority).getEmail();
    }

    private SubmitCaseCafcassTemplate buildEmailTemplatePersonalisationForCafcass(final CaseData caseData) {
        return cafcassEmailContentProvider.buildCafcassSubmissionNotification(caseData);
    }
}
