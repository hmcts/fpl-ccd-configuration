package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ESQueryTest {

    @Test
    void shouldWrapWithQueryField() {
        ESQuery query = new TestClass();

        final JSONObject queryInContext = query.toQueryContext();
        final JSONObject expectedContext = new JSONObject(Map.of("query", Map.of("test", "query")));

        assertThat(queryInContext).usingRecursiveComparison().isEqualTo(expectedContext);
    }

    @Test
    void shouldAddSizeFieldWhenProvided() {
        ESQuery query = new TestClass();

        final JSONObject queryInContext = query.toQueryContext(2);
        final JSONObject expectedContext = new JSONObject(Map.of("query", Map.of("test", "query"), "size", 2));

        assertThat(queryInContext).usingRecursiveComparison().isEqualTo(expectedContext);
    }

    private static class TestClass implements ESQuery {
        @Override
        public Map<String, Object> toMap() {
            return Map.of("test", "query");
        }
    }
}
