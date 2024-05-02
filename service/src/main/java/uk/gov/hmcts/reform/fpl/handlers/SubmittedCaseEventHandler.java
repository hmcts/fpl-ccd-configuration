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
import uk.gov.hmcts.reform.fpl.enums.RepresentativeType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.cafcass.NewApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.OutsourcedCaseContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.translations.TranslationRequestService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Set.of;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.OUTSOURCED_CASE_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.NEW_APPLICATION;

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
    private final TranslationRequestService translationRequestService;
    private final CafcassNotificationService cafcassNotificationService;
    private final FeatureToggleService featureToggleService;

    @Async
    @EventListener
    public void notifyAdmin(final SubmittedCaseEvent event) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            CaseData caseData = event.getCaseData();

            NotifyData notifyData = hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData);
            String recipient = courtService.getCourtEmail(caseData);

            notificationService.sendEmail(
                HMCTS_COURT_SUBMISSION_TEMPLATE, recipient, notifyData, caseData.getId()
            );
        } else {
            log.info("WA EMAIL SKIPPED - case submitted - {}", event.getCaseData().getId());
        }
    }

    @Async
    @EventListener
    public void notifyCafcass(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            Optional<String> recipientIsWelsh = cafcassLookupConfiguration.getCafcassWelsh(caseData
                .getCaseLaOrRelatingLa()).map(CafcassLookupConfiguration.Cafcass::getEmail);
            if (recipientIsWelsh.isPresent()) {
                NotifyData notifyData = cafcassEmailContentProvider.buildCafcassSubmissionNotification(caseData);
                notificationService.sendEmail(CAFCASS_SUBMISSION_TEMPLATE, recipientIsWelsh.get(),
                    notifyData, caseData.getId());
            }
        }
    }

    @Async
    @EventListener
    public void notifyCafcassSendGrid(final SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            Set<DocumentReference> documentReferences = Optional.ofNullable(caseData.getC110A().getSubmittedForm())
                    .map(documentReference ->
                        documentReference.toBuilder()
                            .type(NEW_APPLICATION.getLabel())
                            .build()
                    )
                    .map(Set::of)
                    .orElse(of());

            NewApplicationCafcassData newApplicationCafcassData = cafcassEmailContentProvider
                    .buildCafcassSubmissionSendGridData(caseData);
            cafcassNotificationService.sendEmail(caseData,
                    documentReferences,
                    NEW_APPLICATION,
                    newApplicationCafcassData);
        }
    }

    @EventListener
    public void notifyManagedLA(SubmittedCaseEvent event) {
        CaseData caseData = event.getCaseData();

        if (!caseData.getRepresentativeType().equals(RepresentativeType.LOCAL_AUTHORITY)) {
            log.info("Application has been made as a non-LA, skipping managed LA notification.");
            return;
        }

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

    @Async
    @EventListener
    public void notifyTranslationTeam(SubmittedCaseEvent event) {
        C110A c110A = event.getCaseData().getC110A();
        translationRequestService.sendRequest(event.getCaseData(),
            Optional.ofNullable(c110A.getTranslationRequirements()),
            c110A.getSubmittedForm(), c110A.asLabel()
        );
    }

}
