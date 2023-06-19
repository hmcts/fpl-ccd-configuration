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
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Must;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.service.CaseConverter.MAP_TYPE;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class UpdateSummaryCaseDetailsTest {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";
    private static final String EVENT_NAME = "internal-update-case-summary";
    private static final Long CASE_ID = 12345L;
    private static final int SEARCH_SIZE = 50;
    private static final String RANGE_FIELD = "data.caseSummaryNextHearingDate";

    private static final ESQuery FIRST_RUN_ES_QUERY = BooleanQuery.builder()
        .mustNot(MustNot.builder()
            .clauses(List.of(
                MatchQuery.of("state", "Open"),
                MatchQuery.of("state", "Deleted"),
                MatchQuery.of("state", "RETURNED")
            ))
            .build())
        .build();

    private static final ESQuery ES_QUERY = BooleanQuery.builder()
        .mustNot(MustNot.builder()
            .clauses(List.of(
                MatchQuery.of("state", "Open"),
                MatchQuery.of("state", "Deleted"),
                MatchQuery.of("state", "RETURNED"),
                MatchQuery.of("state", "CLOSED")
            ))
            .build())
        .must(Must.builder()
            .clauses(List.of(
                RangeQuery.builder().field(RANGE_FIELD).lessThan("now/d").build()
            ))
            .build())
        .build();

    private static final SyntheticCaseSummary SUMMARY = SyntheticCaseSummary.builder()
        .caseSummaryNextHearingDate(LocalDate.now())
        .caseSummaryNextHearingType("Hearing type")
        .caseSummaryNextHearingJudge("Dave")
        .caseSummaryHasNextHearing("Yes")
        .build();

    private static Map<String, Object> caseSummaryData;

    @Mock
    private SearchService searchService;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private CaseSummaryService summaryService;
    @Mock
    private CoreCaseDataService ccdService;
    @Mock
    private JobExecutionContext executionContext;
    private CaseConverter caseConverter;

    // autowire required due to the model classes not having been set up properly with jackson in the past,
    // springs construction of the object mapper works but a default construction of it doesn't :(
    @Autowired
    private ObjectMapper mapper;

    private UpdateSummaryCaseDetails underTest;

    @BeforeEach
    void initMocks() {
        caseConverter = new CaseConverter(mapper);
        underTest = new UpdateSummaryCaseDetails(caseConverter,
            mapper,
            searchService,
            ccdService,
            toggleService,
            summaryService);

        caseSummaryData = mapper.convertValue(SUMMARY, new TypeReference<>() {
        });

        JobDetail jobDetail = mock(JobDetail.class);
        JobKey jobKey = mock(JobKey.class);
        when(executionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobKey.getName()).thenReturn("test name");
        when(searchService.searchResultsSize(any())).thenReturn(1);
    }

    @Test
    void shouldPerformUpdateFunction() {
        CaseData caseData = CaseData.builder()
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .build();

        CaseData expectedCaseData1 = caseData.toBuilder().id(CASE_ID)
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .caseProgressionReportEventData(CaseProgressionReportEventData
                .builder()
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(expectedCaseData1.getId())
            .data(mapper.convertValue(expectedCaseData1, MAP_TYPE))
            .build();
        underTest.getUpdates(caseDetails);

        verify(summaryService).generateSummaryFields(expectedCaseData1);
    }

    @Test
    void shouldNotCallCCDWhenNothingToUpdate() {
        CaseDetails caseDetails = CaseDetails.builder().data(Map.of()).build();

        when(summaryService.generateSummaryFields(CaseData.builder()
            .caseProgressionReportEventData(CaseProgressionReportEventData
                .builder()
                .build()).build()))
            .thenReturn(Map.of());

        Map<String, Object> updates = underTest.getUpdates(caseDetails);
        assertThat(updates).isEmpty();
    }

    @Test
    void shouldUseNonStandardQueryWhenFirstRunIsEnabled() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(true);

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(FIRST_RUN_ES_QUERY, SEARCH_SIZE, 0)).thenReturn(caseDetails);
        when(summaryService.generateSummaryFields(CaseData.builder()
            .caseProgressionReportEventData(CaseProgressionReportEventData
                .builder()
                .build())
            .build())).thenReturn(Map.of());

        underTest.execute(executionContext);

        verify(searchService).search(FIRST_RUN_ES_QUERY, SEARCH_SIZE, 0);
    }

    @Test
    void shouldUseStandardQueryWhenFirstRunIsDisabled() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(ES_QUERY, SEARCH_SIZE, 0)).thenReturn(caseDetails);
        when(summaryService.generateSummaryFields(CaseData.builder()
            .caseProgressionReportEventData(CaseProgressionReportEventData
                .builder()
                .build())
            .build())).thenReturn(Map.of());

        underTest.execute(executionContext);

        verify(searchService).search(ES_QUERY, SEARCH_SIZE, 0);
    }

    @Test
    void shouldUpdateCaseWhenSummaryTabInformationIsUpdated() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .caseProgressionReportEventData(CaseProgressionReportEventData
                .builder()
                .build())
            .build();

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder()
            .id(CASE_ID)
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build());

        caseData = caseData.toBuilder().id(CASE_ID).build();

        when(searchService.search(ES_QUERY, SEARCH_SIZE, 0)).thenReturn(caseDetails);

        underTest.execute(executionContext);

        verify(ccdService).performPostSubmitCallback(eq(CASE_ID), eq(EVENT_NAME), any());
    }

    @Test
    void shouldGracefullyHandleErrorsFromCCDWhenUpdatingCaseDetails() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);
        when(searchService.searchResultsSize(any())).thenReturn(2);

        CaseData caseData = CaseData.builder()
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build();

        CaseDetails caseDetails2 = CaseDetails.builder()
            .id(54321L)
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build();

        List<CaseDetails> allCaseDetails = List.of(caseDetails, caseDetails2);

        when(searchService.search(ES_QUERY, SEARCH_SIZE, 0)).thenReturn(allCaseDetails);
        when(summaryService.generateSummaryFields(any())).thenReturn(caseSummaryData);
        doThrow(feignException(500))
            .when(ccdService).performPostSubmitCallback(eq(CASE_ID), eq(EVENT_NAME), any());

        underTest.execute(executionContext);


        verify(ccdService).performPostSubmitCallback(eq(CASE_ID), eq(EVENT_NAME), any());
        verify(ccdService).performPostSubmitCallback(eq(54321L), eq(EVENT_NAME), any());
    }

    @Test
    void shouldGracefullyHandleErrorsFromGenerateSummaryFields() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);
        when(searchService.searchResultsSize(any())).thenReturn(2);

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build();

        CaseDetails caseDetails2 = CaseDetails.builder()
            .id(54321L)
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build();

        List<CaseDetails> allCaseDetails = List.of(caseDetails, caseDetails2);

        when(searchService.search(ES_QUERY, SEARCH_SIZE, 0)).thenReturn(allCaseDetails);
        when(summaryService.generateSummaryFields(any()))
            .thenThrow(new RuntimeException("boom"))
            .thenReturn(caseSummaryData);

        underTest.execute(executionContext);

        verify(ccdService, times(2)).performPostSubmitCallback(any(), eq(EVENT_NAME), any());
        verifyNoMoreInteractions(ccdService);
    }

    @Test
    void shouldGracefullyHandleErrorsFromConversion() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);
        when(searchService.searchResultsSize(any())).thenReturn(2);

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(UploadDraftOrdersData.builder().build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .build();

        CaseDetails caseDetails2 = CaseDetails.builder()
            .id(54321L)
            .data(mapper.convertValue(caseData, new TypeReference<>() {
            }))
            .build();

        List<CaseDetails> allCaseDetails = List.of(caseDetails, caseDetails2);

        when(searchService.search(ES_QUERY, SEARCH_SIZE, 0)).thenReturn(allCaseDetails);
        when(summaryService.generateSummaryFields(any())).thenReturn(caseSummaryData);

        underTest.execute(executionContext);

        verify(ccdService, times(2)).performPostSubmitCallback(any(), eq(EVENT_NAME), any());
        verifyNoMoreInteractions(summaryService, ccdService);
    }

    @Test
    void shouldPaginateWhenNumberOfCasesAreMoreThanTheSearchSize() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);
        when(searchService.searchResultsSize(any())).thenReturn(75);

        CaseData caseData = CaseData.builder()
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .build();

        List<CaseDetails> caseDetails = new ArrayList<>();
        Map<String, Object> data = mapper.convertValue(caseData, new TypeReference<>() {
        });

        for (int i = 0; i < 75; i++) {
            caseDetails.add(CaseDetails.builder()
                .id(nextLong())
                .data(data)
                .build());
        }

        List<CaseDetails> firstSearchList = caseDetails.subList(0, 50);
        List<CaseDetails> secondSearchList = caseDetails.subList(50, 75);

        when(searchService.search(ES_QUERY, SEARCH_SIZE, 0)).thenReturn(firstSearchList);
        when(searchService.search(ES_QUERY, SEARCH_SIZE, 50)).thenReturn(secondSearchList);

        underTest.execute(executionContext);

        verify(ccdService, times(75))
            .performPostSubmitCallback(anyLong(), eq(EVENT_NAME), any());
    }

    @Test
    void shouldSkipJobIfPaginationQueryFails() {
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        doThrow(feignException(500)).when(searchService).searchResultsSize(ES_QUERY);

        underTest.execute(executionContext);

        verify(searchService).searchResultsSize(ES_QUERY);
        verifyNoMoreInteractions(searchService);
    }
}
