package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public class TermsQuery implements ESClause {
    private final String field;
    private final List<String> value;

    public TermsQuery(String field, List<String> value) {
        requireNonNull(field);
        requireNonNull(value);
        this.field = field;
        this.value = value;
    }

    public static TermsQuery of(String field, List<String> value) {
        return new TermsQuery(field, value);
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of("terms", Map.of(field, value));
    }
}
