package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fpl.events.FailedPBAPaymentEvent;
import uk.gov.hmcts.reform.fpl.events.PlacementApplicationSubmitted;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Placement;
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

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService.UPDATE_CASE_EVENT;

@ExtendWith({MockitoExtension.class, TestLogsExtension.class})
class PlacementEventsHandlerPaymentTest {

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
    private TestLogger logs = new TestLogger(PlacementEventsHandler.class);

    @InjectMocks
    private PlacementEventsHandler underTest;

    @Test
    void shouldNotTakePaymentWhenItIsNotRequired() {

        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(NO)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

        underTest.takeApplicationPayment(event);

        assertThat(logs.get()).containsExactly("Payment not required for placement for case 100");

        verifyNoInteractions(paymentService, eventService, coreCaseDataService, userService, time);
    }

    @Test
    void shouldTakeCourtPaymentWhenRequiredAndUpdatePaymentTimestamp() {

        final LocalDateTime now = LocalDateTime.now();

        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

        when(userService.isHmctsAdminUser()).thenReturn(true);

        underTest.takeApplicationPayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);

        verify(paymentService).makePaymentForPlacement(caseData, "HMCTS");
        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID), eq(UPDATE_CASE_EVENT), any());

        verifyNoInteractions(eventService);
    }

    @Test
    void shouldTakeLocalAuthorityPaymentWhenRequiredAndUpdatePaymentTimestamp() {
        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

        when(userService.isHmctsAdminUser()).thenReturn(false);

        underTest.takeApplicationPayment(event);

        verify(paymentService).makePaymentForPlacement(caseData, "Test local authority");
        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID), eq(UPDATE_CASE_EVENT), any());

        verifyNoInteractions(eventService);
    }

    @Test
    void doesExpectedCaseUpdates() {
        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final LocalDateTime now = LocalDateTime.now();
        when(time.now()).thenReturn(now);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> caseMap =
            underTest.getUpdates(CaseDetails.builder().data(mapper.convertValue(caseData, MAP_TYPE)).build());

        assertThat(caseMap).containsAllEntriesOf(expectedCaseUpdates);
    }

    @ParameterizedTest
    @MethodSource("paymentExceptions")
    void shouldNotifyCtscWhenCourtInitiatedPlacementPaymentFailed(Exception paymentException) {

        final LocalDateTime now = LocalDateTime.now();

        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

        when(userService.isHmctsAdminUser()).thenReturn(true);
        doThrow(paymentException).when(paymentService).makePaymentForPlacement(any(), any());

        underTest.takeApplicationPayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);


        verify(paymentService).makePaymentForPlacement(caseData, "HMCTS");
        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID), eq(UPDATE_CASE_EVENT), any());
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

        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

        when(userService.isHmctsAdminUser()).thenReturn(false);
        doThrow(paymentException).when(paymentService).makePaymentForPlacement(any(), any());

        underTest.takeApplicationPayment(event);

        final Map<String, Object> expectedCaseUpdates = new HashMap<>();
        expectedCaseUpdates.put("placementLastPaymentTime", now);
        expectedCaseUpdates.put("placementPaymentRequired", null);
        expectedCaseUpdates.put("placementPayment", null);
        expectedCaseUpdates.put("placement", null);

        verify(paymentService).makePaymentForPlacement(caseData, "Test local authority");
        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_ID), eq(UPDATE_CASE_EVENT), any());
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

        final Placement placement = Placement.builder()
            .childId(randomUUID())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placement(placement)
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName("Test local authority")
            .placementEventData(placementEventData)
            .build();

        final PlacementApplicationSubmitted event = new PlacementApplicationSubmitted(caseData, placement);

        when(userService.isHmctsAdminUser()).thenReturn(false);
        doThrow(unexpectedException).when(paymentService).makePaymentForPlacement(any(), any());

        assertThatThrownBy(() -> underTest.takeApplicationPayment(event)).isEqualTo(unexpectedException);

        verify(paymentService).makePaymentForPlacement(caseData, "Test local authority");
        verifyNoMoreInteractions(coreCaseDataService, eventService);
    }

    private static Stream<Exception> paymentExceptions() {
        return Stream.of(
            new PaymentsApiException("Payment Exception", new RuntimeException()),
            new FeeRegisterException(404, "Fee Exception", new RuntimeException()));
    }
}
