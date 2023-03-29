package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

class AfterSubmissionCaseDataUpdatedEventHandlerTest {

    private static final long CASE_DATA_ID = 23L;
    private static final SyntheticCaseSummary SYNTHETIC_CASE_SUMMARY = mock(SyntheticCaseSummary.class);
    private static final SyntheticCaseSummary ANOTHER_SYNTHETIC_CASE_SUMMARY = mock(SyntheticCaseSummary.class);
    private static final CaseData EMPTY_CASE_DATA = CaseData.builder()
        .id(CASE_DATA_ID)
        .build();

    private static final CaseData CASE_DATA_WITH_SUMMARY = CaseData.builder()
        .id(CASE_DATA_ID)
        .syntheticCaseSummary(SYNTHETIC_CASE_SUMMARY)
        .build();

    @SuppressWarnings("unchecked")
    private static final Map<String, Object> UPDATED_CASE_FIELDS = (Map<String, Object>) Mockito.mock(Map.class);
    @SuppressWarnings("unchecked")
    private static final Map<String, Object> BEFORE_CASE_FIELDS = (Map<String, Object>) Mockito.mock(Map.class);
    private static final String EVENT = "internal-update-case-summary";
    private final CoreCaseDataService coreCaseDataService = mock(CoreCaseDataService.class);
    private final CaseSummaryService caseSummaryService = mock(CaseSummaryService.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    AfterSubmissionCaseDataUpdatedEventHandler underTest = new AfterSubmissionCaseDataUpdatedEventHandler(
        coreCaseDataService,
        caseSummaryService,
        objectMapper);

    @Test
    void testIfNoCaseDataBefore() {
        when(objectMapper.convertValue(eq(SyntheticCaseSummary.emptySummary()),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(
            BEFORE_CASE_FIELDS);
        when(caseSummaryService.generateSummaryFields(EMPTY_CASE_DATA)).thenReturn(UPDATED_CASE_FIELDS);

        underTest.handleCaseDataChange(AfterSubmissionCaseDataUpdated.builder()
            .caseData(EMPTY_CASE_DATA)
            .build());

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_DATA_ID, EVENT, UPDATED_CASE_FIELDS);
    }

    @Test
    void testIfCaseDataWithNoSummary() {
        when(objectMapper.convertValue(eq(SyntheticCaseSummary.emptySummary()),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(
            BEFORE_CASE_FIELDS);
        when(caseSummaryService.generateSummaryFields(EMPTY_CASE_DATA)).thenReturn(UPDATED_CASE_FIELDS);

        underTest.handleCaseDataChange(AfterSubmissionCaseDataUpdated.builder()
            .caseData(EMPTY_CASE_DATA)
            .caseDataBefore(EMPTY_CASE_DATA)
            .build());

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_DATA_ID, EVENT, UPDATED_CASE_FIELDS);
    }

    @Test
    void testIfCaseDataWithChangedSummary() {
        when(objectMapper.convertValue(eq(ANOTHER_SYNTHETIC_CASE_SUMMARY),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(
            BEFORE_CASE_FIELDS);
        when(caseSummaryService.generateSummaryFields(CASE_DATA_WITH_SUMMARY)).thenReturn(UPDATED_CASE_FIELDS);

        underTest.handleCaseDataChange(AfterSubmissionCaseDataUpdated.builder()
            .caseData(CASE_DATA_WITH_SUMMARY)
            .caseDataBefore(CaseData.builder()
                .id(CASE_DATA_ID)
                .syntheticCaseSummary(ANOTHER_SYNTHETIC_CASE_SUMMARY)
                .build())
            .build());

        verify(coreCaseDataService).triggerEvent(JURISDICTION, CASE_TYPE, CASE_DATA_ID, EVENT, UPDATED_CASE_FIELDS);
    }

    @Test
    void testIfSummaryDidNotChage() {
        when(objectMapper.convertValue(eq(SYNTHETIC_CASE_SUMMARY),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(
            BEFORE_CASE_FIELDS);
        when(caseSummaryService.generateSummaryFields(CASE_DATA_WITH_SUMMARY)).thenReturn(BEFORE_CASE_FIELDS);

        underTest.handleCaseDataChange(AfterSubmissionCaseDataUpdated.builder()
            .caseData(CASE_DATA_WITH_SUMMARY)
            .caseDataBefore(CaseData.builder()
                .id(CASE_DATA_ID)
                .syntheticCaseSummary(SYNTHETIC_CASE_SUMMARY)
                .build())
            .build());

        verifyNoInteractions(coreCaseDataService);
    }
}
