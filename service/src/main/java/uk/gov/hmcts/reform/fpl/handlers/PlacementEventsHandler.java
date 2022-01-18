package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationChanged;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.RecipientsRequest;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.nullifyTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getElement;

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

    @EventListener
    public void noticeUploaded(PlacementNoticeAdded event) {
        // Notify LA
        notifyLocalAuthority(event.getCaseData(), event.getPlacement());
        // Notify Cafcass
        notifyCafcass(event.getCaseData(), event.getPlacement());
        // Notify selected respondents
        // TODO - need to merge DFPL-328 in to notify only the selected respondents
    }

    @Async
    @EventListener
    public void notifyCourt(PlacementApplicationSubmitted event) {
        notifyAdmin(event.getCaseData(), event.getPlacement());
    }

    @Async
    @EventListener
    public void notifyCourt(PlacementApplicationChanged event) {
        notifyAdmin(event.getCaseData(), event.getPlacement());
    }

    @Async
    @EventListener
    public void notifyParties(PlacementNoticeChanged event) {
        final CaseData caseData = event.getCaseData();

        final Placement placement = event.getPlacement();
        final PlacementNoticeDocument notice = event.getNotice();
        final PlacementNoticeDocument.RecipientType type = notice.getType();

        if (type == RecipientType.LOCAL_AUTHORITY) {
            notifyLocalAuthority(caseData, placement);
        }

        if (type == RecipientType.CAFCASS) {
            notifyCafcass(caseData, placement);
        }

        if (type == RecipientType.PARENT_FIRST || type == RecipientType.PARENT_SECOND) {
            notifyParent(caseData, placement, notice);
        }
    }

    private void notifyAdmin(CaseData caseData, Placement placement) {

        log.info("Send email to admin about {} child placement", placement.getChildName());

        final NotifyData notifyData = contentProvider.getApplicationChangedCourtData(caseData, placement);

        final String recipient = courtService.getCourtEmail(caseData);

        notificationService
            .sendEmail(PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    private void notifyLocalAuthority(CaseData caseData, Placement placement) {

        log.info("Send email to local authority about {} child placement notice", placement.getChildName());

        final NotifyData notifyData = contentProvider.getNoticeChangedData(caseData, placement);

        final RecipientsRequest recipientsRequest = RecipientsRequest.builder()
            .caseData(caseData)
            .build();

        final Collection<String> recipients = localAuthorityRecipients.getRecipients(recipientsRequest);

        notificationService
            .sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, recipients, notifyData, caseData.getId());
    }

    private void notifyCafcass(CaseData caseData, Placement placement) {

        log.info("Send email to cafcass about {} child placement notice", placement.getChildName());

        final NotifyData notifyData = contentProvider.getNoticeChangedCafcassData(caseData, placement);

        final String recipient = cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getEmail();

        notificationService
            .sendEmail(PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE, recipient, notifyData, caseData.getId());
    }

    private void notifyParent(CaseData caseData, Placement placement, PlacementNoticeDocument notice) {

        final Respondent parent = getElement(notice.getRespondentId(), caseData.getAllRespondents()).getValue();
        final RespondentSolicitor parentSolicitor = parent.getSolicitor();

        if (nonNull(parentSolicitor)) {

            log.info("Send email to parent solicitor about {} child placement notice", placement.getChildName());

            final NotifyData notifyData = contentProvider.getNoticeChangedData(caseData, placement);

            notificationService.sendEmail(PLACEMENT_NOTICE_UPLOADED_TEMPLATE, parentSolicitor.getEmail(), notifyData,
                caseData.getId());
        } else {
            log.info("Send letter to parent about {} child placement notice", placement.getChildName());

            sendDocumentService.sendDocuments(caseData, List.of(notice.getNotice()), List.of(parent.getParty()));
        }

    }

    private void updateCase(CaseData caseData) {
        final Map<String, Object> updates = Map.of("placementLastPaymentTime", time.now());

        coreCaseDataService.updateCase(caseData.getId(), nullifyTemporaryFields(updates, PlacementEventData.class));
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
