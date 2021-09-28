package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationAdded;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationEdited;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.BaseCaseNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementApplicationContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.nullifyTemporaryFields;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementApplicationEventHandler {

    private final Time time;
    private final UserService userService;
    private final EventService eventService;
    private final CourtService courtService;
    private final PaymentService paymentService;
    private final CoreCaseDataService coreCaseDataService;
    private final NotificationService notificationService;
    private final PlacementApplicationContentProvider placementApplicationContentProvider;

    @EventListener
    public void takePayment(PlacementApplicationAdded event) {
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

    public void notifyAdmin(PlacementApplicationAdded event) {
        notifyAdmin(event.getCaseData());
    }

    public void notifyAdmin(PlacementApplicationEdited event) {
        notifyAdmin(event.getCaseData());
    }

    private void notifyAdmin(CaseData caseData) {

        final BaseCaseNotifyData notifyData = placementApplicationContentProvider
            .buildPlacementApplicationNotificationParameters(caseData);
        final String recipient = courtService.getCourtEmail(caseData);

        notificationService
            .sendEmail(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, recipient, notifyData, caseData.getId());
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
