package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
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
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.ApplicantType.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith({MockitoExtension.class, TestLogsExtension.class})
class PlacementApplicationEventHandlerTest {

    private static final Long CASE_ID = 100L;

    @Mock
    private Time time;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @TestLogs
    private TestLogger logs = new TestLogger(PlacementApplicationEventHandler.class);

    @InjectMocks
    private PlacementApplicationEventHandler underTest;

    @Test
    void shouldNotTakePaymentWhenItIsNotRequired() {

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(NO)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationAdded event = new PlacementApplicationAdded(caseData);

        underTest.takePayment(event);

        assertThat(logs.get()).containsExactly("Payment not required for placement for case 100");

        verifyNoInteractions(paymentService, eventService, coreCaseDataService, userService, time);
    }

    @Test
    void shouldTakeCourtPaymentWhenRequiredAndUpdatePaymentTimestamp() {

        final LocalDateTime now = LocalDateTime.now();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationAdded event = new PlacementApplicationAdded(caseData);

        when(time.now()).thenReturn(now);
        when(userService.isHmctsAdminUser()).thenReturn(true);

        underTest.takePayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);

        verify(paymentService).makePaymentForPlacement(caseData, "HMCTS");
        verify(coreCaseDataService).updateCase(CASE_ID, expectedCaseUpdates);

        verifyNoInteractions(eventService);
    }

    @Test
    void shouldTakeLocalAuthorityPaymentWhenRequiredAndUpdatePaymentTimestamp() {

        final LocalDateTime now = LocalDateTime.now();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationAdded event = new PlacementApplicationAdded(caseData);

        when(time.now()).thenReturn(now);
        when(userService.isHmctsAdminUser()).thenReturn(false);

        underTest.takePayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);

        verify(paymentService).makePaymentForPlacement(caseData, "Test local authority");
        verify(coreCaseDataService).updateCase(CASE_ID, expectedCaseUpdates);

        verifyNoInteractions(eventService);
    }

    @ParameterizedTest
    @MethodSource("paymentExceptions")
    void shouldNotifyCtscWhenCourtInitiatedPlacementPaymentFailed(Exception paymentException) {

        final LocalDateTime now = LocalDateTime.now();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationAdded event = new PlacementApplicationAdded(caseData);

        when(time.now()).thenReturn(now);
        when(userService.isHmctsAdminUser()).thenReturn(true);
        doThrow(paymentException).when(paymentService).makePaymentForPlacement(any(), any());

        underTest.takePayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);


        verify(paymentService).makePaymentForPlacement(caseData, "HMCTS");
        verify(coreCaseDataService).updateCase(CASE_ID, expectedCaseUpdates);
        verify(eventService).publishEvent(FailedPBAPaymentEvent.builder()
            .caseData(caseData)
            .applicant(OrderApplicant.builder()
                .name(HMCTS.name())
                .type(HMCTS)
                .build())
            .applicationTypes(List.of(A50_PLACEMENT))
            .build());

    }

    @ParameterizedTest
    @MethodSource("paymentExceptions")
    void shouldNotifyCtscWhenLocalAuthorityInitiatedPlacementPaymentFailed(Exception paymentException) {

        final LocalDateTime now = LocalDateTime.now();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationAdded event = new PlacementApplicationAdded(caseData);

        when(time.now()).thenReturn(now);
        when(userService.isHmctsAdminUser()).thenReturn(false);
        doThrow(paymentException).when(paymentService).makePaymentForPlacement(any(), any());

        underTest.takePayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);

        verify(paymentService).makePaymentForPlacement(caseData, "Test local authority");
        verify(coreCaseDataService).updateCase(CASE_ID, expectedCaseUpdates);
        verify(eventService).publishEvent(FailedPBAPaymentEvent.builder()
            .caseData(caseData)
            .applicant(OrderApplicant.builder()
                .name("Test local authority")
                .type(LOCAL_AUTHORITY)
                .build())
            .applicationTypes(List.of(A50_PLACEMENT))
            .build());
    }

    @Test
    void shouldNotSendNotificationWhenUnrecognisedExceptionThrown() {

        final Exception unexpectedException = new RuntimeException();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationAdded event = new PlacementApplicationAdded(caseData);

        when(userService.isHmctsAdminUser()).thenReturn(false);
        doThrow(unexpectedException).when(paymentService).makePaymentForPlacement(any(), any());

        assertThatThrownBy(() -> underTest.takePayment(event)).isEqualTo(unexpectedException);

        verify(paymentService).makePaymentForPlacement(caseData, "Test local authority");
        verifyNoMoreInteractions(coreCaseDataService, eventService);
    }

    private static Stream<Exception> paymentExceptions() {
        return Stream.of(
            new PaymentsApiException("Payment Exception", new RuntimeException()),
            new FeeRegisterException(404, "Fee Exception", new RuntimeException()));
    }
}
