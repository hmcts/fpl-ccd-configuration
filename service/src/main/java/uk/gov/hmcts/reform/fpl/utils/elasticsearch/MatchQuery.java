package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.EqualsAndHashCode;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public class MatchQuery implements ESQuery {
    private final String field;
    private final Object value;

    public MatchQuery(String field, Object value) {
        requireNonNull(field);
        requireNonNull(value);
        this.field = field;
        this.value = value;
    }

    public static MatchQuery of(String field, Object value) {
        return new MatchQuery(field, value);
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of("match", Map.of(field, Map.of("query", value)));
    }
}
