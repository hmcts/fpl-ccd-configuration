package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService.MATCH_FIELD;
import static uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService.RANGE_FIELD;
import static uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService.REQUIRED_STATES;

class FilterTest {

    @Test
    void shouldReturnSortClause() {
        TermsQuery termsQuery = TermsQuery.of("state", REQUIRED_STATES);
        TermQuery termQuery = TermQuery.of(MATCH_FIELD, "344");
        RangeQuery rangeQuery = RangeQuery.builder()
            .field(RANGE_FIELD)
            .lessThan("29-Sept-2022")
            .build();

        Filter filter = Filter.builder()
            .clauses(List.of(
                termQuery, termsQuery, rangeQuery
            ))
            .build();

        JSONObject expected = new JSONObject(
            Map.of("filter",
                List.of(
                    Map.of("term", Map.of("data.court.code","344")),
                    Map.of("terms", Map.of("state",
                        List.of("submitted","gatekeeping","prepare_for_hearing","final_hearing"))),
                    Map.of("range", Map.of("data.dateSubmitted", Map.of("lt","29-Sept-2022"))))));

        JSONObject actual = new JSONObject(filter.toMap());
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
}