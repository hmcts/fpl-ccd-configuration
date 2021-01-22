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
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JacksonAutoConfiguration.class })
class UpdateSummaryCaseDetailsTest {

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";
    private static final String EVENT_NAME = "internal-update-case-summary";
    private static final Long CASE_ID = 12345L;
    private static final int SEARCH_SIZE = 3000;

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
        .build();

    @Mock
    private SearchService searchService;
    @Mock
    private FeatureToggleService toggleService;
    @Mock
    private CaseSummaryService summaryService;
    @Mock
    private CoreCaseDataService ccdService;

    // autowire required due to the model classes not having been set up properly with jackson in the past,
    // springs construction of the object mapper works but a default construction of it doesn't :(
    @Autowired
    private ObjectMapper mapper;

    private UpdateSummaryCaseDetails underTest;

    @Mock
    private JobExecutionContext executionContext;

    @BeforeEach
    void initMocks() {
        CaseConverter converter = new CaseConverter(mapper);
        underTest = new UpdateSummaryCaseDetails(converter, mapper, searchService, ccdService, toggleService, summaryService);

        JobDetail jobDetail = mock(JobDetail.class);
        JobKey jobKey = mock(JobKey.class);
        when(executionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobKey.getName()).thenReturn("test name");
    }

    @Test
    void shouldNotPerformSearchWhenFeatureToggledOff() {
        when(toggleService.isSummaryTabEnabled()).thenReturn(false);

        underTest.execute(executionContext);

        verifyNoInteractions(searchService);
    }

    @Test
    void shouldUseNonStandardQueryWhenFirstRunIsEnabled() {
        when(toggleService.isSummaryTabEnabled()).thenReturn(true);
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(true);

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(FIRST_RUN_ES_QUERY, SEARCH_SIZE)).thenReturn(caseDetails);
        when(summaryService.generateSummaryFields(CaseData.builder().build())).thenReturn(Map.of());

        underTest.execute(executionContext);

        verify(searchService).search(FIRST_RUN_ES_QUERY, SEARCH_SIZE);
        verifyNoMoreInteractions(searchService);
    }

    @Test
    void shouldUseStandardQueryWhenFirstRunIsDisabled() {
        when(toggleService.isSummaryTabEnabled()).thenReturn(true);
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(ES_QUERY, SEARCH_SIZE)).thenReturn(caseDetails);
        when(summaryService.generateSummaryFields(CaseData.builder().build())).thenReturn(Map.of());

        underTest.execute(executionContext);

        verify(searchService).search(ES_QUERY, SEARCH_SIZE);
        verifyNoMoreInteractions(searchService);
    }

    @Test
    void shouldNotCallCCDWhenNothingToUpdate() {
        when(toggleService.isSummaryTabEnabled()).thenReturn(true);
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder().data(Map.of()).build());

        when(searchService.search(ES_QUERY, SEARCH_SIZE)).thenReturn(caseDetails);
        when(summaryService.generateSummaryFields(CaseData.builder().build())).thenReturn(Map.of());

        underTest.execute(executionContext);

        verifyNoInteractions(ccdService);
    }

    @Test
    void shouldUpdateCaseWhenSummaryTabInformationIsUpdated() {
        when(toggleService.isSummaryTabEnabled()).thenReturn(true);
        when(toggleService.isSummaryTabFirstCronRunEnabled()).thenReturn(false);

        CaseData caseData = CaseData.builder()
            .syntheticCaseSummary(SyntheticCaseSummary.builder()
                .caseSummaryHasNextHearing("No")
                .build())
            .build();

        List<CaseDetails> caseDetails = List.of(CaseDetails.builder()
            .id(CASE_ID)
            .data(mapper.convertValue(caseData, new TypeReference<>() {}))
            .build());

        SyntheticCaseSummary caseSummary = SyntheticCaseSummary.builder()
            .caseSummaryNextHearingDate(LocalDate.now())
            .caseSummaryNextHearingType("Hearing type")
            .caseSummaryNextHearingJudge("Dave")
            .caseSummaryHasNextHearing("Yes")
            .build();

        caseData = caseData.toBuilder().id(CASE_ID).build();

        Map<String, Object> caseSummaryData = mapper.convertValue(caseSummary, new TypeReference<>() {});

        when(searchService.search(ES_QUERY, SEARCH_SIZE)).thenReturn(caseDetails);
        when(summaryService.generateSummaryFields(caseData)).thenReturn(caseSummaryData);

        underTest.execute(executionContext);

        verify(summaryService).generateSummaryFields(caseData);
        verify(ccdService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, EVENT_NAME, caseSummaryData);
    }
}
