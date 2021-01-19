package uk.gov.hmcts.reform.fpl.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
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
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

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

        List<CaseDetails> expectedCases = List.of(CaseDetails.builder().id(nextLong()).build());

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(expectedCases);

        List<CaseDetails> casesFound = searchService.search(property, date);

        assertThat(casesFound).isEqualTo(expectedCases);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), queryCaptor.capture());

        String expectedQuery = format("{\"query\":{\"range\":{\"%s\":{\"lt\":\"%sT00:00\",\"gte\":\"%sT00:00\"}}}}",
            property, date.plusDays(1), date);

        JSONAssert.assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);
    }

    @Test
    void shouldSearchCasesWhenGivenESQuery() {
        ESQuery query = BooleanQuery.builder()
            .mustNot(MustNot.builder().clauses(List.of(MatchQuery.of("a", "b"))).build())
            .build();

        List<CaseDetails> expectedCases = List.of(CaseDetails.builder().id(nextLong()).build());

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(expectedCases);

        List<CaseDetails> casesFound = searchService.search(query);

        assertThat(casesFound).isEqualTo(expectedCases);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), queryCaptor.capture());

        String expectedQuery = "{\"query\":{\"bool\":{\"must_not\":[{\"match\":{\"a\":{\"query\":\"b\"}}}]}}}";

        JSONAssert.assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);
    }
}
