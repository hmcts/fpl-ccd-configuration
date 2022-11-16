package uk.gov.hmcts.reform.fpl.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ResendCafcassEmailService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.email.content.NoticeOfHearingEmailContentProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class ResendCafcassEmailsTest {

    @Autowired
    private ObjectMapper mapper;

    @Mock
    private CafcassNotificationService cafcassNotificationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NoticeOfHearingEmailContentProvider noticeOfHearingEmailContentProvider;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ResendCafcassEmailService resendCafcassEmailService;

    @Mock
    private JobExecutionContext executionContext;

    public ResendCafcassEmails underTest;

    private List<Long> casesWithHearings = List.of(3L, 4L);
    private List<Long> casesWithOrders = List.of(1L, 2L, 4L, 5L, 6L, 7L, 8L);

    @BeforeEach
    void init() {
        CaseConverter converter = new CaseConverter(mapper);
        underTest = new ResendCafcassEmails(converter, cafcassNotificationService, featureToggleService,
            noticeOfHearingEmailContentProvider, coreCaseDataService, resendCafcassEmailService);

        JobDetail jobDetail = mock(JobDetail.class);
        JobKey jobKey = mock(JobKey.class);
        when(executionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobKey.getName()).thenReturn("Resent Cafcass Emails");

        when(featureToggleService.isResendCafcassEmailsEnabled()).thenReturn(true);

        // mock hearings
        CaseDetails case1 = buildOrderResentCase(1L, "Child assessment order");
        CaseDetails case2 = buildOrderResentCase(2L, "Care order");
        CaseDetails case3 = buildHearingResentCase(3L);
        CaseDetails case4 = buildCaseWithAllIssues(4L);
        CaseDetails case5 = buildCaseWithSealedCMO(5L);
        CaseDetails case6 = buildCaseWithDraftOrder(6L);
        CaseDetails case7 = buildCaseWithGeneratedOrderDateTime(7L, "Order title");
        CaseDetails case8 = buildCaseWithGeneratedOrderDateOfIssue(8L, "Order title");

        // mock cases with hearings
        when(resendCafcassEmailService.getNoticeOfHearingDateTimes(longThat(arg -> !casesWithHearings.contains(arg))))
            .thenReturn(List.of());

        // mock cases with no hearings
        when(resendCafcassEmailService.getNoticeOfHearingDateTimes(longThat(arg -> casesWithHearings.contains(arg))))
            .thenReturn(List.of(LocalDateTime.of(2022, 1, 1, 10, 30)));

        // mock orders
        when(resendCafcassEmailService.getOrderDates(longThat(arg -> casesWithOrders.contains(arg))))
            .thenReturn(List.of(LocalDate.of(2022, 1, 1)));

        // mock no orders
        when(resendCafcassEmailService.getOrderDates(longThat(arg -> !casesWithOrders.contains(arg))))
            .thenReturn(List.of());

        when(coreCaseDataService.findCaseDetailsByIdNonUser("1")).thenReturn(case1);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("2")).thenReturn(case2);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("3")).thenReturn(case3);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("4")).thenReturn(case4);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("5")).thenReturn(case5);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("6")).thenReturn(case6);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("7")).thenReturn(case7);
        when(coreCaseDataService.findCaseDetailsByIdNonUser("8")).thenReturn(case8);
    }

    @Test
    void shouldResendGeneratedOrders() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(1L, 2L));

        underTest.execute(executionContext);

        verify(cafcassNotificationService, times(2)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldResendGeneratedOrderWithDateTime() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(7L));

        underTest.execute(executionContext);
        verify(cafcassNotificationService, times(1)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldResendGeneratedOrderWithDateOfIssue() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(8L));

        underTest.execute(executionContext);
        verify(cafcassNotificationService, times(1)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldResendNoticeOfHearings() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(3L));

        underTest.execute(executionContext);

        verify(cafcassNotificationService, times(1)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldResendDraftOrders() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(6L));

        underTest.execute(executionContext);

        verify(cafcassNotificationService, times(1)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldResendSealedCMOs() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(5L));

        underTest.execute(executionContext);

        verify(cafcassNotificationService, times(1)).sendEmail(any(), any(), any(), any());
    }

    @Test
    void shouldResendAll() {
        when(resendCafcassEmailService.getAllCaseIds()).thenReturn(Set.of(1L, 2L, 3L, 4L, 5L, 6L));

        underTest.execute(executionContext);

        verify(cafcassNotificationService, times(7)).sendEmail(any(), any(), any(), any());
    }

    private CaseDetails buildOrderResentCase(Long caseId, String orderTitle) {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .orderCollection(List.of(element(
                GeneratedOrder.builder()
                    .document(testDocumentReference())
                    .title(orderTitle)
                    .approvalDate(LocalDate.of(2022, 1, 1))
                    .build()),
                element(
                    GeneratedOrder.builder()
                        .document(testDocumentReference())
                        .approvalDate(LocalDate.of(2022, 1, 10))
                        .build()
                )
            ))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails buildCaseWithGeneratedOrderDateTime(Long caseId, String orderTitle) {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .orderCollection(List.of(element(
                    GeneratedOrder.builder()
                        .document(testDocumentReference())
                        .title(orderTitle)
                        .approvalDateTime(LocalDateTime.of(2022, 1, 1, 10, 30))
                        .build())
            ))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails buildCaseWithGeneratedOrderDateOfIssue(Long caseId, String orderTitle) {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .orderCollection(List.of(element(
                    GeneratedOrder.builder()
                        .document(testDocumentReference())
                        .title(orderTitle)
                        .dateOfIssue("1 January 2022")
                        .build())
            ))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails buildHearingResentCase(Long caseId) {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .hearingDetails(List.of(element(
                HearingBooking.builder()
                    .startDate(LocalDateTime.of(2022, 1, 1, 10, 30))
                    .noticeOfHearing(testDocumentReference())
                    .build()
            )))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails buildCaseWithAllIssues(Long caseId) {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .hearingDetails(List.of(element(
                HearingBooking.builder()
                    .startDate(LocalDateTime.of(2022, 1, 1, 10, 30))
                    .noticeOfHearing(testDocumentReference())
                    .build()
            )))
            .orderCollection(List.of(element(
                    GeneratedOrder.builder()
                        .document(testDocumentReference())
                        .title("Test order")
                        .approvalDate(LocalDate.of(2022, 1, 1))
                        .build())
            ))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails buildCaseWithSealedCMO(Long caseId) {
        HearingBooking booking = HearingBooking.builder()
            .type(HearingType.FURTHER_CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2022, 3, 2, 10, 30))
            .noticeOfHearing(testDocumentReference())
            .build();

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .hearingDetails(List.of(element(booking)))
            .sealedCMOs(List.of(element(
                HearingOrder.builder()
                    .hearing(booking.toLabel())
                    .dateIssued(LocalDate.of(2022, 1, 1))
                    .order(testDocumentReference())
                    .title("test sealed cmo")
                    .build()
            )))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails buildCaseWithDraftOrder(Long caseId) {
        Element<HearingBooking> booking = element(UUID.randomUUID(), HearingBooking.builder()
            .type(HearingType.FURTHER_CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(2022, 3, 2, 10, 30))
            .noticeOfHearing(testDocumentReference())
            .build());

        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(element(HearingOrder.builder()
            .dateSent(LocalDate.of(2022, 1, 1))
            .type(HearingOrderType.C21)
            .order(testDocumentReference())
            .title("draft order with hearing")
            .build()));

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .state(State.CASE_MANAGEMENT)
            .hearingDetails(List.of(booking))
            .hearingOrdersBundlesDrafts(List.of(
                element(booking.getId(), HearingOrdersBundle.builder()
                    .hearingId(booking.getId())
                    .hearingName(booking.getValue().toLabel())
                    .orders(orders)
                    .build())
            ))
            .build();
        return asCaseDetails(caseData);
    }

    private CaseDetails asCaseDetails(CaseData caseData) {
        return CaseDetails.builder()
            .id(caseData.getId())
            .state(Optional.ofNullable(caseData.getState()).map(State::getValue).orElse(null))
            .data(mapper.convertValue(caseData, MAP_TYPE))
            .build();
    }
}
