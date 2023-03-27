package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService.SORT_FIELD;

class ESQueryTest {
    @Test
    void shouldCreateQueryWithSizeAndFromFields() {
        ESQuery query = new TestClass();

        final JSONObject queryInContext = query.toQueryContext(2, 3);
        final JSONObject expectedContext = new JSONObject(
            Map.of("query", Map.of("test", "query"), "size", 2,  "from", 3)
        );

        assertThat(queryInContext).usingRecursiveComparison().isEqualTo(expectedContext);
    }

    @Test
    void shouldCreateQueryWithSizeAndFromFieldsSort() {
        ESQuery query = new TestClass();
        Sort sort = Sort.builder()
            .clauses(List.of(
                    SortQuery.of(SORT_FIELD, SortOrder.DESC)
            ))
            .build();
        final JSONObject queryInContext = query.toQueryContext(2, 3, sort);
        final JSONObject expectedContext = new JSONObject(
                Map.of("query", Map.of("test", "query"), "size", 2,  "from", 3,
                        "sort", List.of(Map.of("data.dateSubmitted", Map.of("order","desc")))));

        assertThat(queryInContext).usingRecursiveComparison().isEqualTo(expectedContext);
    }

    private static class TestClass implements ESQuery {
        @Override
        public Map<String, Object> toMap() {
            return Map.of("test", "query");
        }
    }
}
