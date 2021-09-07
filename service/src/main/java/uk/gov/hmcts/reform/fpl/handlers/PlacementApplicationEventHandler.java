package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.nullifyTemporaryFields;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementApplicationEventHandler {

    private final Time time;
    private final UserService userService;
    private final EventService eventService;
    private final PaymentService paymentService;
    private final CoreCaseDataService coreCaseDataService;

    @EventListener
    public void pay(PlacementApplicationAdded event) {
        final CaseData caseData = event.getCaseData();

        if (YesNo.NO.equals(caseData.getPlacementEventData().getPlacementPaymentRequired())) {
            log.info("Payment not required for placement (case {})", caseData.getId());
        } else {

            try {
                paymentService.makePaymentForPlacement(caseData, caseData.getPlacementEventData()
                    .getPlacementPayment());
            } catch (FeeRegisterException | PaymentsApiException ignore) {
                log.error("Payment not taken for placement {}.", caseData.getId());

                handlePaymentNotTaken(caseData);
            }

            final Map<String, Object> updates = Map.of("placementLastPaymentTime", time.now());

            coreCaseDataService.updateCase(caseData.getId(), nullifyTemporaryFields(updates, PlacementEventData.class));
        }
    }

    private void handlePaymentNotTaken(CaseData caseData) {

        final FailedPBAPaymentEvent event = FailedPBAPaymentEvent.builder()
            .caseData(caseData)
            .applicant(getApplicant(caseData))
            .applicationTypes(List.of(A50_PLACEMENT))
            .build();

        eventService.publishEvent(event);
    }

    private OrderApplicant getApplicant(CaseData caseData) {
        if (userService.isHmctsAdminUser()) {
            return OrderApplicant.builder()
                .name("HMCST")
                .type(HMCTS)
                .build();
        }

        return OrderApplicant.builder()
            .name(caseData.getCaseLocalAuthorityName())
            .type(LOCAL_AUTHORITY)
            .build();
    }

}
