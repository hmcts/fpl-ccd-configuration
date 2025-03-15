package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.PlacementApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.RepresentativesInbox;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Set.of;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.PLACEMENT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.PLACEMENT_NOTICE;
import static uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService.UPDATE_CASE_EVENT;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.nullifyTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlacementEventsHandler {

    private final Time time;
    private final UserService userService;
    private final EventService eventService;
    private final CourtService courtService;
    private final PaymentService paymentService;
    private final CoreCaseDataService coreCaseDataService;
    private final NotificationService notificationService;
    private final PlacementContentProvider contentProvider;
    private final SendDocumentService sendDocumentService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final LocalAuthorityRecipientsService localAuthorityRecipients;
    private final CafcassNotificationService cafcassNotificationService;
    private final RepresentativesInbox representativesInbox;
    private final FeatureToggleService featureToggleService;


    @EventListener
    public void takeApplicationPayment(PlacementApplicationSubmitted event) {
        final CaseData caseData = event.getCaseData();

        if (NO == caseData.getPlacementEventData().getPlacementPaymentRequired()) {
            log.info("Payment not required for placement for case {}", caseData.getId());
        } else {

            final OrderApplicant applicant = getApplicant(caseData);

            try {
                paymentService.makePaymentForPlacement(caseData, applicant.getName());

                updateCase(caseData);
            } catch (FeeRegisterException | PaymentsApiException ignore) {
                log.error("Payment not taken for placement for case {}.", caseData.getId());

                handlePaymentNotTaken(caseData, applicant);

                updateCase(caseData);
            }
        }
    }

    @Async
    @EventListener
    public void notifyCafcassOfNewApplicationSendGrid(final PlacementApplicationSubmitted event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            PlacementApplicationCafcassData placementApplicationCafcassData =
                contentProvider.buildNewPlacementApplicationNotificationCafcassData(
                    caseData,
                    event.getPlacement()
                );

            Set<DocumentReference> docsToSend = of(event.getPlacement().getApplication());
            docsToSend.forEach(el -> el.setType(PLACEMENT_APPLICATION.getLabel()));

            cafcassNotificationService.sendEmail(caseData,
                docsToSend,
                PLACEMENT_APPLICATION,
                placementApplicationCafcassData);
        }
    }

    @Async
    @EventListener
    public void notifyCafcassOfNewApplicationGovNotify(final PlacementApplicationSubmitted event) {
        final CaseData caseData = event.getCaseData();
        final Placement placement = event.getPlacement();

        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            Optional<String> recipientIsWelsh = cafcassLookupConfiguration.getCafcassWelsh(caseData
                .getCaseLocalAuthority()).map(CafcassLookupConfiguration.Cafcass::getEmail);
            if (recipientIsWelsh.isPresent()) {
                log.info("Send email to cafcass about {} new placement application", placement.getChildName());

                final NotifyData notifyData = contentProvider.getApplicationChangedCourtData(caseData, placement);
                notificationService.sendEmail(PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE,
                    recipientIsWelsh.get(), notifyData, caseData.getId());
            }
        }
    }

    @Async
    @EventListener
    public void notifyLocalAuthorityOfNewNotice(PlacementNoticeAdded event) {
        final CaseData caseData = event.getCaseData();
        final Placement placement = event.getPlacement();

        log.info("Send email to local authority about {} child placement notice", placement.getChildName());

        final NotifyData notifyData = contentProvider.getNoticeChangedData(caseData, placement);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService
            .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, recipients, notifyData, caseData.getId());
    }

    @Async
    @EventListener
    public void notifyCafcassOfNewNotice(PlacementNoticeAdded event) {
        final CaseData caseData = event.getCaseData();
        final Placement placement = event.getPlacement();

        if (CafcassHelper.isNotifyingCafcassWelsh(caseData, cafcassLookupConfiguration)) {
            Optional<String> recipientIsWelsh = cafcassLookupConfiguration.getCafcassWelsh(caseData
                .getCaseLocalAuthority()).map(CafcassLookupConfiguration.Cafcass::getEmail);
            if (recipientIsWelsh.isPresent()) {
                log.info("Send email to cafcass about {} child placement notice", placement.getChildName());

                final NotifyData notifyData = contentProvider.getNoticeChangedCafcassData(caseData, placement);

                notificationService.sendEmail(PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE, recipientIsWelsh.get(),
                    notifyData, caseData.getId());
            }
        }
    }

    @Async
    @EventListener
    public void notifyCafcassOfNewNoticeSendGrid(PlacementNoticeAdded event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcassEngland(caseData, cafcassLookupConfiguration)) {
            PlacementApplicationCafcassData placementApplicationCafcassData =
                contentProvider.buildNewPlacementApplicationNotificationCafcassData(
                    caseData,
                    event.getPlacement()
                );

            Set<DocumentReference> docsToSend = of(event.getPlacement().getPlacementNotice());
            docsToSend.forEach(e -> e.setType(PLACEMENT_NOTICE.getLabel()));

            cafcassNotificationService.sendEmail(caseData,
                docsToSend,
                PLACEMENT_NOTICE,
                placementApplicationCafcassData);
        }
    }

    @Async
    @EventListener
    public void notifyRespondentsOfNewNotice(PlacementNoticeAdded event) {
        final CaseData caseData = event.getCaseData();
        final Placement placement = event.getPlacement();

        if (placement.getPlacementRespondentsToNotify() != null) {
            for (Element<Respondent> respondent : placement.getPlacementRespondentsToNotify()) {
                Optional<Element<Respondent>> resp = caseData.getAllRespondents().stream().filter(
                    el -> el.getId().equals(respondent.getId())).findFirst();

                resp.ifPresent(respondentElement -> notifyRespondent(
                    caseData, placement, respondentElement.getValue()));
            }
        }
    }

    @Async
    @EventListener
    public void notifyChildSolicitorsOfNewNotice(PlacementNoticeAdded event) {
        final CaseData caseData = event.getCaseData();
        final Placement placement = event.getPlacement();

        final Set<String> recipients = representativesInbox.getChildrenSolicitorEmails(caseData, DIGITAL_SERVICE);
        if (!recipients.isEmpty()) {
            final NotifyData notifyData = contentProvider.getNoticeChangedData(caseData, placement);

            notificationService.sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, recipients, notifyData,
                caseData.getId());
        }
    }

    @Async
    @EventListener
    public void notifyCourtOfNewApplication(PlacementApplicationSubmitted event) {
        notifyAdmin(event.getCaseData(), event.getPlacement());
    }

    @Async
    @EventListener
    public void notifyCourtOfChangedApplication(PlacementApplicationChanged event) {
        notifyAdmin(event.getCaseData(), event.getPlacement());
    }

    private void notifyAdmin(CaseData caseData, Placement placement) {
        if (featureToggleService.isWATaskEmailsEnabled()) {
            log.info("Send email to admin about {} child placement", placement.getChildName());

            final NotifyData notifyData = contentProvider.getApplicationChangedCourtData(caseData, placement);

            final String recipient = courtService.getCourtEmail(caseData);

            notificationService
                .sendEmail(PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE, recipient, notifyData, caseData.getId());
        } else {
            log.info("WA EMAIL SKIPPED - placement application uploaded/changed - {}", caseData.getId());
        }
    }

    private void notifyRespondent(CaseData caseData, Placement placement, Respondent respondent) {

        final RespondentSolicitor parentSolicitor = respondent.getSolicitor();

        if (nonNull(parentSolicitor)) {

            log.info("Send email to respondent ({}) solicitor about {} child placement notice",
                respondent.getParty().getFullName(), placement.getChildName());

            final NotifyData notifyData = contentProvider.getNoticeChangedData(caseData, placement);

            notificationService.sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, parentSolicitor.getEmail(), notifyData,
                caseData.getId());
        } else if (!respondent.isDeceasedOrNFA()) {
            log.info("Send letter, application and supporting documents to "
                            + "respondent ({}) about {} child placement notice",
                    respondent.getParty().getFullName(), placement.getChildName());

            List<DocumentReference> placementNoticeAndSupportingDocuments =
                    unwrapElements(placement.getSupportingDocuments())
                    .stream()
                    .map(PlacementSupportingDocument::getDocument)
                    .collect(Collectors.toList());

            placementNoticeAndSupportingDocuments.addAll(
                    List.of(placement.getPlacementNotice(), placement.getApplication()));

            sendDocumentService.sendDocuments(
                    caseData, placementNoticeAndSupportingDocuments, List.of(respondent.getParty()));
        }

    }

    private void updateCase(CaseData caseData) {
        coreCaseDataService.performPostSubmitCallback(caseData.getId(), UPDATE_CASE_EVENT, this::getUpdates);
    }

    public Map<String, Object> getUpdates(CaseDetails caseDetails) {
        final Map<String, Object> updates = Map.of("placementLastPaymentTime", time.now());
        return nullifyTemporaryFields(updates, PlacementEventData.class);
    }

    private void handlePaymentNotTaken(CaseData caseData, OrderApplicant applicant) {

        final FailedPBAPaymentEvent event = FailedPBAPaymentEvent.builder()
            .caseData(caseData)
            .applicant(applicant)
            .applicationTypes(List.of(A50_PLACEMENT))
            .build();

        eventService.publishEvent(event);
    }

    private OrderApplicant getApplicant(CaseData caseData) {

        if (userService.isHmctsAdminUser()) {
            return OrderApplicant.builder()
                .name(HMCTS.name())
                .type(HMCTS)
                .build();
        }

        return OrderApplicant.builder()
            .name(caseData.getCaseLocalAuthorityName())
            .type(LOCAL_AUTHORITY)
            .build();
    }

}
