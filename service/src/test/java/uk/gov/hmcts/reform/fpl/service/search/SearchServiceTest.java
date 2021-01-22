package uk.gov.hmcts.reform.fpl.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    private static final List<CaseDetails> EXPECTED_CASES = List.of(CaseDetails.builder().id(nextLong()).build());

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void shouldSearchCasesByDateProperty() {
        String property = "a.b";
        LocalDate date = LocalDate.now();

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(EXPECTED_CASES);

        List<CaseDetails> casesFound = searchService.search(property, date);

        String expectedQuery = format("{\"query\":{\"range\":{\"%s\":{\"lt\":\"%sT00:00\",\"gte\":\"%sT00:00\"}}}}",
            property, date.plusDays(1), date);

        assertThat(casesFound).isEqualTo(EXPECTED_CASES);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), eqJson(expectedQuery));
    }

    @Test
    void shouldSearchCasesWhenGivenESQuery() {
        ESQuery query = BooleanQuery.builder()
            .mustNot(MustNot.builder().clauses(List.of(MatchQuery.of("a", "b"))).build())
            .build();

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(EXPECTED_CASES);

        List<CaseDetails> casesFound = searchService.search(query);

        String expectedQuery = "{\"query\":{\"bool\":{\"must_not\":[{\"match\":{\"a\":{\"query\":\"b\"}}}]}}}";

        assertThat(casesFound).isEqualTo(EXPECTED_CASES);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), eqJson(expectedQuery));
    }

    @Test
    void shouldAddSizePropertyToESQuery() {
        ESQuery query = BooleanQuery.builder()
            .mustNot(MustNot.builder().clauses(List.of(MatchQuery.of("a", "b"))).build())
            .build();

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(EXPECTED_CASES);

        List<CaseDetails> casesFound = searchService.search(query, 15);

        String expectedQuery = "{\"size\":15,\"query\":{\"bool\":{\"must_not\":[{\"match\":{\"a\":{\"query\":\"b"
            + "\"}}}]}}}";
        assertThat(casesFound).isEqualTo(EXPECTED_CASES);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), eqJson(expectedQuery));
    }
}
