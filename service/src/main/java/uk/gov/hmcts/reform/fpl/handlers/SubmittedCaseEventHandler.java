package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.isInOpenState;

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
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper mapper;

    @Async
    @EventListener
    public void sendEmailToHmctsAdmin(final SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);

        notificationService.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE,
            adminNotificationHandler.getHmctsAdminEmail(eventData),
            buildEmailTemplatePersonalisationForLocalAuthority(eventData),
            eventData.getReference());
    }

    @Async
    @EventListener
    public void sendEmailToCafcass(final SubmittedCaseEvent event) {
        EventData eventData = new EventData(event);

        notificationService.sendEmail(CAFCASS_SUBMISSION_TEMPLATE,
            getEmailRecipientForCafcass(eventData.getLocalAuthorityCode()),
            buildEmailTemplatePersonalisationForCafcass(eventData),
            eventData.getReference());
    }

    @Async
    @EventListener
    public void makePayment(final SubmittedCaseEvent event) {
        final CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();

        if (!featureToggleService.isPaymentsEnabled()) {
            log.info("Payment not taken for case {} due to feature toggle.", caseDetails.getId());
            return;
        }

        if (!isInOpenState(event.getCallbackRequest().getCaseDetailsBefore())) {
            log.info("Payment not taken for case {} due to not open state.", caseDetails.getId());
            return;
        }

        switch (getPaymentDecision(caseDetails)) {
            case YES:
                makePaymentForCaseOrders(event);
                return;
            case NO:
                handlePaymentNotTaken(event);
                return;
            default:
                log.info("Payment not taken for case {}.", event.getCallbackRequest().getCaseDetails().getId());
        }
    }

    private void makePaymentForCaseOrders(final SubmittedCaseEvent event) {
        final CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        try {
            paymentService.makePaymentForCaseOrders(caseDetails.getId(), caseData);
        } catch (FeeRegisterException | PaymentsApiException ignore) {
            handlePaymentNotTaken(event);
        }
    }

    private YesNo getPaymentDecision(CaseDetails caseDetails) {
        return YesNo.fromString(caseDetails.getData().getOrDefault("displayAmountToPay", "").toString());
    }

    private void handlePaymentNotTaken(SubmittedCaseEvent submittedCaseEvent) {
        applicationEventPublisher.publishEvent(new FailedPBAPaymentEvent(submittedCaseEvent, C110A_APPLICATION));
    }

    private SubmitCaseHmctsTemplate buildEmailTemplatePersonalisationForLocalAuthority(final EventData eventData) {
        return hmctsEmailContentProvider.buildHmctsSubmissionNotification(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());
    }

    private String getEmailRecipientForCafcass(final String localAuthority) {
        return cafcassLookupConfiguration.getCafcass(localAuthority).getEmail();
    }

    private SubmitCaseCafcassTemplate buildEmailTemplatePersonalisationForCafcass(final EventData eventData) {
        return cafcassEmailContentProvider.buildCafcassSubmissionNotification(eventData.getCaseDetails(),
            eventData.getLocalAuthorityCode());
    }
}
