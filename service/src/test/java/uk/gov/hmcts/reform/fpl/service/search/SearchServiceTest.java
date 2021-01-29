package uk.gov.hmcts.reform.fpl.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    private static final List<CaseDetails> EXPECTED_CASES = List.of(CaseDetails.builder().id(nextLong()).build());
    private static final SearchResult SEARCH_RESULT = SearchResult.builder().total(1).cases(EXPECTED_CASES).build();

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void shouldSearchCasesByDateProperty() {
        String property = "a.b";
        LocalDate date = LocalDate.now();

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(SEARCH_RESULT);

        List<CaseDetails> casesFound = searchService.search(property, date);

        String expectedQuery = format("{\"size\": 1000,"
                + " \"query\":{\"range\":{\"%s\":{\"lt\":\"%sT00:00\",\"gte\":\"%sT00:00\"}}}}",
            property, date.plusDays(1), date);

        assertThat(casesFound).isEqualTo(EXPECTED_CASES);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), queryCaptor.capture());

        assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);
    }

    @Test
    void shouldSearchCasesWhenGivenESQuery() {
        ESQuery query = BooleanQuery.builder()
            .mustNot(MustNot.builder().clauses(List.of(MatchQuery.of("a", "b"))).build())
            .build();

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(SEARCH_RESULT);

        List<CaseDetails> casesFound = searchService.search(query, 15, 0);

        String expectedQuery = "{\"size\":15,\"from\":0,\"query\":{\"bool\":{\"must_not\":[{\"match\":{\"a\":{"
            + "\"query\":\"b\"}}}]}}}";

        assertThat(casesFound).isEqualTo(EXPECTED_CASES);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), queryCaptor.capture());

        assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);
    }

    @Test
    void shouldReturnTheNumberOfCasesFound() {
        ESQuery query = MatchQuery.of("a", "b");

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(SEARCH_RESULT);

        int numberOfCases = searchService.searchResultsSize(query);

        String expectedQuery = "{\"size\": 1,\"from\": 0,\"query\": {\"match\": {\"a\":{\"query\": \"b\"}}}}";

        assertThat(numberOfCases).isEqualTo(1);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), queryCaptor.capture());

        assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);
    }
}
