package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanQueryTest {

    @Test
    void shouldNotIncludeMustIfNull() {
        BooleanQuery query = BooleanQuery.builder()
            .mustNot(MustNot.builder().clauses(List.of()).build())
            .build();

        final Map<String, Object> queryMap = query.toMap();
        final Map<String, Object> expectedMap = Map.of("bool", Map.of("must_not", List.of()));

        assertThat(queryMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldNotIncludeMustNotIfNull() {
        BooleanQuery query = BooleanQuery.builder()
            .must(Must.builder().clauses(List.of()).build())
            .build();

        final Map<String, Object> queryMap = query.toMap();
        final Map<String, Object> expectedMap = Map.of("bool", Map.of("must", List.of()));

        assertThat(queryMap).isEqualTo(expectedMap);
    }
}
