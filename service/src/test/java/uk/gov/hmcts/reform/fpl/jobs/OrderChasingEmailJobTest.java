package uk.gov.hmcts.reform.fpl.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.cmo.SendOrderReminderEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.SendOrderReminderService;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class OrderChasingEmailJobTest {

    private static final Long CASE_ID = 12345L;
    private static final int SEARCH_SIZE = 50;

    @Mock
    private SearchService searchService;
    @Mock
    private SendOrderReminderService sendOrderReminderService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CoreCaseDataService ccdService;
    @Mock
    private JobExecutionContext executionContext;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    // autowire required due to the model classes not having been set up properly with jackson in the past,
    // springs construction of the object mapper works but a default construction of it doesn't :(
    @Autowired
    private ObjectMapper mapper;

    private OrderChasingEmailJob underTest;

    @BeforeEach
    void initMocks() {
        CaseConverter converter = new CaseConverter(mapper);
        underTest = new OrderChasingEmailJob(converter,
            searchService,
            applicationEventPublisher,
            sendOrderReminderService);

        JobDetail jobDetail = mock(JobDetail.class);
        JobKey jobKey = mock(JobKey.class);
        when(executionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobKey.getName()).thenReturn("test name");
        when(searchService.searchResultsSize(any())).thenReturn(1);
    }

    @Test
    void shouldNotCallCCDWhenNothingToUpdate() {
        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(any(), eq(SEARCH_SIZE), eq(0))).thenReturn(caseDetails);

        underTest.execute(executionContext);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailEventIfHearingWas5DaysAgo() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .endDate(LocalDateTime.now().minusDays(5).minusHours(1))
                    .build())
            ))
            .build();

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder()
            .id(CASE_ID)
            .data(mapper.convertValue(caseData, new TypeReference<>() {}))
            .build());

        caseData = caseData.toBuilder().id(CASE_ID).build();

        when(searchService.search(any(), eq(SEARCH_SIZE), eq(0))).thenReturn(caseDetails);

        underTest.execute(executionContext);

        verify(applicationEventPublisher).publishEvent(new SendOrderReminderEvent(any()));
    }

    @Test
    void shouldGracefullyHandleErrorsFromCCDWhenUpdatingCaseDetails() {
        when(searchService.searchResultsSize(any())).thenReturn(2);

        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .endDate(LocalDateTime.now().minusDays(5).minusHours(1))
                    .build())
            ))
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(mapper.convertValue(caseData, new TypeReference<>() {}))
            .build();

        CaseDetails caseDetails2 = CaseDetails.builder()
            .id(54321L)
            .data(mapper.convertValue(caseData, new TypeReference<>() {}))
            .build();

        List<CaseDetails> allCaseDetails = List.of(caseDetails, caseDetails2);

        when(searchService.search(any(), eq(SEARCH_SIZE), eq(0))).thenReturn(allCaseDetails);

        doThrow(feignException(500))
            .when(applicationEventPublisher).publishEvent(new SendOrderReminderEvent(any()));

        underTest.execute(executionContext);

        verify(applicationEventPublisher, times(2)).publishEvent(new SendOrderReminderEvent(any()));
    }

    @Test
    void shouldPaginateWhenNumberOfCasesAreMoreThanTheSearchSize() {
        when(searchService.searchResultsSize(any())).thenReturn(75);

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .endDate(LocalDateTime.now().minusDays(5))
                    .build())
            ))
            .build();

        List<CaseDetails> caseDetails = new ArrayList<>();
        Map<String, Object> data = mapper.convertValue(caseData, new TypeReference<>() {});

        for (int i = 0; i < 75; i++) {
            caseDetails.add(CaseDetails.builder()
                .id(nextLong())
                .data(data)
                .build());
        }

        List<CaseDetails> firstSearchList = caseDetails.subList(0, 50);
        List<CaseDetails> secondSearchList = caseDetails.subList(50, 75);

        when(searchService.search(any(), eq(SEARCH_SIZE), eq(0))).thenReturn(firstSearchList);
        when(searchService.search(any(), eq(SEARCH_SIZE), eq(50))).thenReturn(secondSearchList);

        underTest.execute(executionContext);

        verify(applicationEventPublisher, times(75)).publishEvent(new SendOrderReminderEvent(any()));
    }

    @Test
    void shouldSkipJobIfPaginationQueryFails() {
        doThrow(feignException(500)).when(searchService).searchResultsSize(any());

        underTest.execute(executionContext);

        verify(searchService).searchResultsSize(any());
        verifyNoMoreInteractions(searchService);
    }
}
