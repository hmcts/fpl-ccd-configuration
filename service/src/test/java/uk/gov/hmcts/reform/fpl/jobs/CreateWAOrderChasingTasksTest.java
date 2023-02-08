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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

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
import static uk.gov.hmcts.reform.fpl.enums.WorkAllocationTaskType.ORDER_NOT_UPLOADED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CreateWAOrderChasingTasksTest {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";
    private static final String EVENT_NAME = "create-work-allocation-task";
    private static final Long CASE_ID = 12345L;
    private static final int SEARCH_SIZE = 50;
    private static final String RANGE_FIELD = "data.hearingDetails.value.endDate";

    private static final ESQuery FIRST_RUN_ES_QUERY = BooleanQuery.builder()
        .mustNot(MustNot.builder()
            .clauses(List.of(
                MatchQuery.of("state", "Open"),
                MatchQuery.of("state", "Submitted"),
                MatchQuery.of("state", "Gatekeeping"),
                MatchQuery.of("state", "CLOSED"),
                MatchQuery.of("state", "Deleted"),
                MatchQuery.of("state", "RETURNED")
            ))
            .build())
        .filter(Filter.builder()
            .clauses(List.of(
                RangeQuery.builder().field(RANGE_FIELD)
                    .lessThanOrEqual("now/d-5d")
                    .build()
            ))
            .build())
        .build();

    private static final ESQuery ES_QUERY = BooleanQuery.builder()
        .mustNot(MustNot.builder()
            .clauses(List.of(
                MatchQuery.of("state", "Open"),
                MatchQuery.of("state", "Submitted"),
                MatchQuery.of("state", "Gatekeeping"),
                MatchQuery.of("state", "CLOSED"),
                MatchQuery.of("state", "Deleted"),
                MatchQuery.of("state", "RETURNED")
            ))
            .build())
        .filter(Filter.builder()
            .clauses(List.of(
                RangeQuery.builder().field(RANGE_FIELD)
                    .lessThanOrEqual("now/d-5d")
                    .greaterThanOrEqual("now/d-5d")
                    .build()
            ))
            .build())
        .build();

    @Mock
    private SearchService searchService;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private WorkAllocationTaskService waTaskService;
    @Mock
    private CoreCaseDataService ccdService;
    @Mock
    private JobExecutionContext executionContext;

    // autowire required due to the model classes not having been set up properly with jackson in the past,
    // springs construction of the object mapper works but a default construction of it doesn't :(
    @Autowired
    private ObjectMapper mapper;

    private CreateWAOrderChasingTasks underTest;

    @BeforeEach
    void initMocks() {
        CaseConverter converter = new CaseConverter(mapper);
        underTest = new CreateWAOrderChasingTasks(converter,
            searchService,
            toggleService,
            waTaskService);

        JobDetail jobDetail = mock(JobDetail.class);
        JobKey jobKey = mock(JobKey.class);
        when(executionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobKey.getName()).thenReturn("test name");
        when(searchService.searchResultsSize(any())).thenReturn(1);
    }

    @Test
    void shouldNotCallCCDWhenNothingToUpdate() {
        when(toggleService.isChaseOrdersFirstCronRunEnabled()).thenReturn(false);

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(any(), eq(SEARCH_SIZE), eq(0))).thenReturn(caseDetails);

        underTest.execute(executionContext);

        verifyNoInteractions(waTaskService);
    }

    @Test
    void shouldCreateWADummyEventIfHearingWas5DaysAgo() {
        when(toggleService.isChaseOrdersFirstCronRunEnabled()).thenReturn(false);

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

        verify(waTaskService).createWorkAllocationTask(caseData, ORDER_NOT_UPLOADED);
    }

    @Test
    void shouldGracefullyHandleErrorsFromCCDWhenUpdatingCaseDetails() {
        when(toggleService.isChaseOrdersFirstCronRunEnabled()).thenReturn(false);
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
            .when(waTaskService).createWorkAllocationTask(caseData, ORDER_NOT_UPLOADED);

        underTest.execute(executionContext);

        verify(waTaskService).createWorkAllocationTask(caseData, ORDER_NOT_UPLOADED);
    }

    @Test
    void shouldPaginateWhenNumberOfCasesAreMoreThanTheSearchSize() {
        when(toggleService.isChaseOrdersFirstCronRunEnabled()).thenReturn(false);
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

        verify(waTaskService, times(75))
            .createWorkAllocationTask(any(), eq(ORDER_NOT_UPLOADED));
    }

    @Test
    void shouldSkipJobIfPaginationQueryFails() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        doThrow(feignException(500)).when(searchService).searchResultsSize(any());

        underTest.execute(executionContext);

        verify(searchService).searchResultsSize(any());
        verifyNoMoreInteractions(searchService);
    }
}
