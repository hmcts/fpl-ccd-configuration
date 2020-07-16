package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.EventData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class SubmittedCaseEventHandlerTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private HmctsAdminNotificationHandler adminNotificationHandler;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler;

    @Test
    void shouldSendEmailToHmctsAdmin() {
        final String expectedEmail = "test@test.com";
        final CallbackRequest request = callbackRequest();
        final SubmitCaseHmctsTemplate expectedTemplate = new SubmitCaseHmctsTemplate();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);

        when(adminNotificationHandler.getHmctsAdminEmail(new EventData(submittedCaseEvent))).thenReturn(expectedEmail);
        when(hmctsEmailContentProvider.buildHmctsSubmissionNotification(request.getCaseDetails(), LOCAL_AUTHORITY_CODE))
            .thenReturn(expectedTemplate);

        submittedCaseEventHandler.sendEmailToHmctsAdmin(submittedCaseEvent);

        verify(notificationService).sendEmail(
            HMCTS_COURT_SUBMISSION_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            request.getCaseDetails().getId().toString());
    }

    @Test
    void shouldSendEmailToCafcass() {
        final String expectedEmail = "test@test.com";
        final CallbackRequest request = callbackRequest();
        final CafcassLookupConfiguration.Cafcass cafcass =
            new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, expectedEmail);
        final SubmitCaseCafcassTemplate expectedTemplate = new SubmitCaseCafcassTemplate();
        final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);

        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE)).thenReturn(cafcass);
        when(cafcassEmailContentProvider.buildCafcassSubmissionNotification(request.getCaseDetails(),
            LOCAL_AUTHORITY_CODE)).thenReturn(expectedTemplate);

        submittedCaseEventHandler.sendEmailToCafcass(submittedCaseEvent);

        verify(notificationService).sendEmail(
            CAFCASS_SUBMISSION_TEMPLATE,
            expectedEmail,
            expectedTemplate,
            request.getCaseDetails().getId().toString());
    }

    @Nested
    class Payment {

        final CaseData caseData = CaseData.builder().build();

        @BeforeEach
        void init() {
            when(mapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        }

        @ParameterizedTest
        @EnumSource(value = State.class, mode = EXCLUDE, names = {"OPEN"})
        void shouldNotPayIfCaseStateIsDifferentThanOpen(State state) {
            final CallbackRequest request = callbackRequest(state, Map.of("displayAmountToPay", "Yes"));
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(paymentService, applicationEventPublisher);
        }

        @Test
        void shouldNotPayAndNotEmitFailureEventIfPaymentDecisionIsNotPresent() {
            final CallbackRequest request = callbackRequest(OPEN, emptyMap());
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(paymentService, applicationEventPublisher);
        }

        @Test
        void shouldNotPayAndEmitFailureEventIfPaymentDecisionsIsNo() {
            final CallbackRequest request = callbackRequest(OPEN, Map.of("displayAmountToPay", "No"));
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(applicationEventPublisher)
                .publishEvent(new FailedPBAPaymentEvent(submittedCaseEvent, C110A_APPLICATION));
            verifyNoMoreInteractions(paymentService, applicationEventPublisher);
        }

        @Test
        void shouldEmitFailureEventWhenPaymentFailed() {
            final CallbackRequest request = callbackRequest(OPEN, Map.of("displayAmountToPay", "Yes"));
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);
            final Exception exception = new PaymentsApiException("", new RuntimeException());

            doThrow(exception).when(paymentService).makePaymentForCaseOrders(1L, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(applicationEventPublisher)
                .publishEvent(new FailedPBAPaymentEvent(submittedCaseEvent, C110A_APPLICATION));
        }

        @Test
        void shouldPayWhenCaseIsOpenedAndPaymentDesicionIsYes() {
            final CallbackRequest request = callbackRequest(OPEN, Map.of("displayAmountToPay", "Yes"));
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(request);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(applicationEventPublisher);
        }
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(SubmittedCaseEventHandler.class).hasAsyncMethods(
            "sendEmailToHmctsAdmin",
            "sendEmailToCafcass",
            "makePayment");
    }

    private static CallbackRequest callbackRequest() {
        return callbackRequest(OPEN, emptyMap());
    }

    private static CallbackRequest callbackRequest(State state, Map<String, Object> data) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseLocalAuthority", LOCAL_AUTHORITY_CODE);
        caseData.putAll(data);

        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(1L)
                .state(state.getValue())
                .data(caseData)
                .build())
            .caseDetailsBefore(
                CaseDetails.builder()
                    .id(1L)
                    .state(state.getValue())
                    .data(caseData)
                    .build()
            )
            .build();
    }
}
