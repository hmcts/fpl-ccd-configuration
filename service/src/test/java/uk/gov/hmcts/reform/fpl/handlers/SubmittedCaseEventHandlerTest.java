package uk.gov.hmcts.reform.fpl.handlers;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.exception.RetryablePaymentException;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.PaymentService;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE_CHILD_NAME;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(MockitoExtension.class)
class SubmittedCaseEventHandlerTest {

    private static final long CASE_ID = 12345L;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HmctsEmailContentProvider hmctsEmailContentProvider;

    @Mock
    private CourtService courtService;

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Mock
    private EventService eventService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private FeatureToggleService toggleService;

    @InjectMocks
    private SubmittedCaseEventHandler submittedCaseEventHandler;

    @ParameterizedTest
    @MethodSource("hmctsTemplates")
    void shouldSendEmailToHmctsAdmin(String template, boolean toggle) {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);

        final String email = "test@test.com";
        final SubmitCaseHmctsTemplate parameters = mock(SubmitCaseHmctsTemplate.class);

        when(caseData.getId()).thenReturn(CASE_ID);
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);
        when(courtService.getCourtEmail(caseData)).thenReturn(email);
        when(hmctsEmailContentProvider.buildHmctsSubmissionNotification(caseData)).thenReturn(parameters);

        submittedCaseEventHandler.notifyAdmin(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(notificationService).sendEmail(template, email, parameters, CASE_ID);
    }

    @ParameterizedTest
    @MethodSource("cafcassTemplates")
    void shouldSendEmailToCafcass(String template, boolean toggle) {
        final CaseData caseData = mock(CaseData.class);
        final CaseData caseDataBefore = mock(CaseData.class);

        final SubmitCaseCafcassTemplate parameters = mock(SubmitCaseCafcassTemplate.class);
        final String email = "test@test.com";

        when(caseData.getCaseLocalAuthority()).thenReturn(LOCAL_AUTHORITY_CODE);
        when(caseData.getId()).thenReturn(CASE_ID);
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(toggle);
        when(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .thenReturn(new Cafcass(LOCAL_AUTHORITY_CODE, email));
        when(cafcassEmailContentProvider.buildCafcassSubmissionNotification(caseData)).thenReturn(parameters);

        submittedCaseEventHandler.notifyCafcass(new SubmittedCaseEvent(caseData, caseDataBefore));

        verify(notificationService).sendEmail(template, email, parameters, CASE_ID);
    }

    @Test
    void shouldExecuteAsynchronously() {
        assertClass(SubmittedCaseEventHandler.class).hasAsyncMethods("notifyAdmin", "notifyCafcass", "makePayment");
    }

    private static Stream<Arguments> hmctsTemplates() {
        return Stream.of(
            Arguments.of(HMCTS_COURT_SUBMISSION_TEMPLATE, false),
            Arguments.of(HMCTS_COURT_SUBMISSION_TEMPLATE_CHILD_NAME, true)
        );
    }

    private static Stream<Arguments> cafcassTemplates() {
        return Stream.of(
            Arguments.of(CAFCASS_SUBMISSION_TEMPLATE, false),
            Arguments.of(CAFCASS_SUBMISSION_TEMPLATE_CHILD_NAME, true)
        );
    }

    @Nested
    class Payment {

        @Test
        void shouldNotPayIfCaseIsReturned() {
            final CaseData caseData = CaseData.builder()
                .state(SUBMITTED)
                .build();
            final CaseData caseDataBefore = CaseData.builder()
                .state(RETURNED)
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseDataBefore);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(paymentService, eventService);
        }

        @Test
        void shouldNotPayAndEmitFailureEventIfPaymentDecisionsIsNotPresent() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
            verifyNoMoreInteractions(paymentService, eventService);
        }

        @Test
        void shouldNotPayAndEmitFailureEventIfPaymentDecisionsIsNo() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("No")
                .build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
            verifyNoMoreInteractions(paymentService, eventService);
        }

        @Test
        void shouldEmitFailureEventWhenPaymentFailed() {
            CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            final Exception exception = new PaymentsApiException("", new RuntimeException());

            doThrow(exception).when(paymentService).makePaymentForCaseOrders(caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
        }

        @Test
        void shouldEmitFailureEventWhenPaymentFailedOnRetryablePaymentException() {
            CaseData caseData = CaseData.builder()
                .id(RandomUtils.nextLong())
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();
            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            final Exception exception = new RetryablePaymentException("", new RuntimeException());

            doThrow(exception).when(paymentService).makePaymentForCaseOrders(caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verify(eventService).publishEvent(new FailedPBAPaymentEvent(caseData, List.of(C110A_APPLICATION),
                OrderApplicant.builder().type(LOCAL_AUTHORITY).name(caseData.getCaseLocalAuthorityName()).build()));
        }

        @Test
        void shouldPayWhenCaseIsOpenedAndPaymentDecisionIsYes() {
            CaseData caseData = CaseData.builder()
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
                .displayAmountToPay("Yes")
                .build();

            final SubmittedCaseEvent submittedCaseEvent = new SubmittedCaseEvent(caseData, caseData);

            submittedCaseEventHandler.makePayment(submittedCaseEvent);

            verifyNoMoreInteractions(eventService);
        }
    }

}
