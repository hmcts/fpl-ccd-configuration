package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.CaseDefinitionConstants;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CafcassApiSearchCaseServiceTest {
    private static final LocalDateTime SEARCH_END_DATE = LocalDateTime.now();
    private static final LocalDateTime SEARCH_START_DATE = SEARCH_END_DATE.minusDays(1);
    private static final BooleanQuery SEARCH_QUERY = BooleanQuery.builder()
        .mustNot(MustNot.builder()
            .clauses(List.of(
                MatchQuery.of("state", "Open"),
                MatchQuery.of("state", "CLOSED"),
                MatchQuery.of("state", "Deleted"),
                MatchQuery.of("state", "RETURNED")))
            .build())
        .filter(Filter.builder()
            .clauses(List.of(RangeQuery.builder().field("last_modified")
                .greaterThanOrEqual(SEARCH_START_DATE).lessThanOrEqual(SEARCH_END_DATE).build()))
            .build())
        .build();

    private static final CaseData MOCK_CASE_DATA_1 = mock(CaseData.class);
    private static final CaseDetails MOCK_CASE_DETAILS_1 = CaseDetails.builder()
        .id(1L)
        .jurisdiction(CaseDefinitionConstants.JURISDICTION)
        .state(State.CASE_MANAGEMENT.getValue())
        .caseTypeId(CaseDefinitionConstants.CASE_TYPE)
        .createdDate(LocalDateTime.MIN)
        .lastModified(LocalDateTime.now().minusHours(1))
        .build();
    private static final CaseData MOCK_CASE_DATA_2 = mock(CaseData.class);
    private static final CaseDetails MOCK_CASE_DETAILS_2 = MOCK_CASE_DETAILS_1.toBuilder().id(2L).build();

    private static final CafcassApiCaseData MOCK_CONVERTED_CAFCASSAPICASEDATA = mock(CafcassApiCaseData.class);

    @Mock
    private CaseConverter caseConverter;
    @Mock
    private SearchService searchService;
    @Mock
    private CafcassApiCaseDataConverter cafcassApiCaseDataConverter1;
    @Mock
    private CafcassApiCaseDataConverter cafcassApiCaseDataConverter2;
    @Mock
    private CafcassApiCaseDataConverter cafcassApiCaseDataConverter3;
    @Captor
    private ArgumentCaptor<ESQuery> searchQueryCaptor;

    private CafcassApiSearchCaseService underTest;

    @BeforeEach
    void setUpWithMockConverters() {
        underTest = new CafcassApiSearchCaseService(caseConverter, searchService,
            List.of(cafcassApiCaseDataConverter1, cafcassApiCaseDataConverter2, cafcassApiCaseDataConverter3));
    }

    @Test
    void shouldReturnSearchResultAndBuildCafcassApiCaseByInvokingAllConverter() {
        final CafcassApiCaseData.CafcassApiCaseDataBuilder mockBuilder =
            mock(CafcassApiCaseData.CafcassApiCaseDataBuilder.class);
        when(mockBuilder.build()).thenReturn(MOCK_CONVERTED_CAFCASSAPICASEDATA);
        when(cafcassApiCaseDataConverter1.convert(any(), any())).thenReturn(mockBuilder);
        when(cafcassApiCaseDataConverter2.convert(any(), any())).thenReturn(mockBuilder);
        when(cafcassApiCaseDataConverter3.convert(any(), any())).thenReturn(mockBuilder);

        final List<CaseDetails> caseDetails = List.of(MOCK_CASE_DETAILS_1, MOCK_CASE_DETAILS_2);
        when(searchService.search(searchQueryCaptor.capture(), anyInt(), anyInt())).thenReturn(caseDetails);
        when(caseConverter.convert(MOCK_CASE_DETAILS_1)).thenReturn(MOCK_CASE_DATA_1);
        when(caseConverter.convert(MOCK_CASE_DETAILS_2)).thenReturn(MOCK_CASE_DATA_2);


        CafcassApiCase expectedCafcassApiCase1 = CafcassApiCase.builder()
            .caseId(1L)
            .jurisdiction(CaseDefinitionConstants.JURISDICTION)
            .state(State.CASE_MANAGEMENT.getValue())
            .caseTypeId(CaseDefinitionConstants.CASE_TYPE)
            .createdDate(MOCK_CASE_DETAILS_1.getCreatedDate())
            .lastModified(MOCK_CASE_DETAILS_1.getLastModified())
            .caseData(MOCK_CONVERTED_CAFCASSAPICASEDATA)
            .build();
        CafcassApiCase expectedCafcassApiCase2 = expectedCafcassApiCase1.toBuilder()
            .caseId(2L)
            .caseData(MOCK_CONVERTED_CAFCASSAPICASEDATA)
            .build();

        List<CafcassApiCase> actual = underTest.searchCaseByDateRange(SEARCH_START_DATE, SEARCH_END_DATE);
        List<CafcassApiCase> expected = List.of(expectedCafcassApiCase1, expectedCafcassApiCase2);

        assertEquals(expected, actual);
        assertEquals(SEARCH_QUERY.toMap(), searchQueryCaptor.getValue().toMap());

        // verify calling all converters to build CafcassApiCaseData
        verify(cafcassApiCaseDataConverter1).convert(eq(MOCK_CASE_DATA_1), any());
        verify(cafcassApiCaseDataConverter2).convert(eq(MOCK_CASE_DATA_1), any());
        verify(cafcassApiCaseDataConverter3).convert(eq(MOCK_CASE_DATA_1), any());
        verify(cafcassApiCaseDataConverter1).convert(eq(MOCK_CASE_DATA_2), any());
        verify(cafcassApiCaseDataConverter2).convert(eq(MOCK_CASE_DATA_2), any());
        verify(cafcassApiCaseDataConverter3).convert(eq(MOCK_CASE_DATA_2), any());
        verifyNoMoreInteractions(cafcassApiCaseDataConverter1);
        verifyNoMoreInteractions(cafcassApiCaseDataConverter2);
        verifyNoMoreInteractions(cafcassApiCaseDataConverter3);
    }

    @Test
    void shouldReturnEmptyListIfNoCaseFound() {
        when(searchService.search(any(), anyInt(), anyInt())).thenReturn(List.of());
        List<CafcassApiCase> actual = underTest.searchCaseByDateRange(SEARCH_START_DATE, SEARCH_END_DATE);

        assertEquals(List.of(), actual);
    }
}
