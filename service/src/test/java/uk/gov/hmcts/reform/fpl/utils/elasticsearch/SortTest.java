package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService.SORT_FIELD;

class SortTest {

    @Test
    void shouldReturnSortClause() {
        Sort sort = Sort.builder()
            .clauses(List.of(
                    SortQuery.of(SORT_FIELD, SortOrder.DESC)
            ))
            .build();
        List<Object> objects = sort.toMap();
        JSONObject jsonObject = new JSONObject(Map.of("sort", objects));
        JSONObject expected = new JSONObject(Map.of("sort",
                List.of(Map.of("data.dateSubmitted", Map.of("order","desc")))));
        assertThat(jsonObject).usingRecursiveComparison().isEqualTo(expected);
    }
}