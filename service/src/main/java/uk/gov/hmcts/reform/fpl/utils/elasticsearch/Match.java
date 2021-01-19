package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import java.util.Map;

public class Match implements ESClause {
    private final String field;
    private final Object value;

    public Match(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public static Match match(String field, Object value) {
        return new Match(field, value);
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of("match", Map.of(field, Map.of("query", value)));
    }
}
