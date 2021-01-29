package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RangeQueryTest {

    @Test
    void shouldRequireFieldBePresent() {
        final RangeQuery query = RangeQuery.builder().build();

        assertThatThrownBy(query::toMap)
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldIgnoreGreaterThanWhenNull() {
        final RangeQuery query = RangeQuery.builder()
            .field("field")
            .greaterThan(null)
            .lessThan(1)
            .greaterThanOrEqual(1)
            .lessThanOrEqual(1)
            .build();

        final Map<String, Object> queryMap = query.toMap();
        final Map<String, Object> expectedMap = Map.of("range", Map.of("field", Map.of("lt", 1, "gte", 1, "lte", 1)));

        assertThat(queryMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldIgnoreLessThanWhenNull() {
        final RangeQuery query = RangeQuery.builder()
            .field("field")
            .greaterThan(1)
            .lessThan(null)
            .greaterThanOrEqual(1)
            .lessThanOrEqual(1)
            .build();

        final Map<String, Object> queryMap = query.toMap();
        final Map<String, Object> expectedMap = Map.of("range", Map.of("field", Map.of("gt", 1, "gte", 1, "lte", 1)));

        assertThat(queryMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldIgnoreGreaterThanOrEqualWhenNull() {
        final RangeQuery query = RangeQuery.builder()
            .field("field")
            .greaterThan(1)
            .lessThan(1)
            .greaterThanOrEqual(null)
            .lessThanOrEqual(1)
            .build();

        final Map<String, Object> queryMap = query.toMap();
        final Map<String, Object> expectedMap = Map.of("range", Map.of("field", Map.of("gt", 1, "lt", 1, "lte", 1)));

        assertThat(queryMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldIgnoreLessThanOrEqualWhenNull() {
        final RangeQuery query = RangeQuery.builder()
            .field("field")
            .greaterThan(1)
            .lessThan(1)
            .greaterThanOrEqual(1)
            .lessThanOrEqual(null)
            .build();

        final Map<String, Object> queryMap = query.toMap();
        final Map<String, Object> expectedMap = Map.of("range", Map.of("field", Map.of("gt", 1, "lt", 1, "gte", 1)));

        assertThat(queryMap).isEqualTo(expectedMap);
    }
}
