package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.summary.CaseSummaryService;

import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private final CaseConverter caseConverter = mock(CaseConverter.class);

    @Captor
    private ArgumentCaptor<CaseData> caseDataArgumentCaptor;
    @Captor
    private ArgumentCaptor<Function<CaseDetails, Map<String, Object>>> changeFunctionCaptor;

    AfterSubmissionCaseDataUpdatedEventHandler underTest = new AfterSubmissionCaseDataUpdatedEventHandler(
        coreCaseDataService,
        caseSummaryService,
        objectMapper,
        caseConverter);

    @Test
    void testIfNoCaseDataBefore() {
        when(objectMapper.convertValue(eq(SyntheticCaseSummary.emptySummary()),
            Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(
            BEFORE_CASE_FIELDS);
        when(caseSummaryService.generateSummaryFields(EMPTY_CASE_DATA)).thenReturn(UPDATED_CASE_FIELDS);

        underTest.handleCaseDataChange(AfterSubmissionCaseDataUpdated.builder()
            .caseData(EMPTY_CASE_DATA)
            .build());

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_DATA_ID), eq(EVENT), any());
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

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_DATA_ID), eq(EVENT), any());
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

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_DATA_ID), eq(EVENT), any());
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

        verify(coreCaseDataService).performPostSubmitCallback(eq(CASE_DATA_ID), eq(EVENT), any());
    }

    @Test
    void shouldPopulateTransientField() {
        // normal case data without the transient field is put into the function
        when(caseConverter.convert(any())).thenReturn(CASE_DATA_WITH_SUMMARY.toBuilder().build());

        // Event caseData has the transient field set (from callback logic)
        underTest.caseSummaryChangeFunction(CaseDetails.builder().data(Map.of()).build(),
            AfterSubmissionCaseDataUpdated.builder().caseData(CaseData.builder()
                    .caseFlagValueUpdated(YesNo.YES)
                .build()).build());

        // verify that we generate the summary fields WITH the transient field set as expected
        verify(caseSummaryService).generateSummaryFields(caseDataArgumentCaptor.capture());
        assertThat(caseDataArgumentCaptor.getValue().getCaseFlagValueUpdated()).isEqualTo(YesNo.YES);
    }
}
