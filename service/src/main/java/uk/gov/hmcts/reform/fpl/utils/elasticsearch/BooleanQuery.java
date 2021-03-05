package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Builder
public class BooleanQuery implements ESQuery {
    private final MustNot mustNot;
    private final Must must;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> query = new HashMap<>();
        if (mustNot != null) {
            query.putAll(mustNot.toMap());
        }
        if (must != null) {
            query.putAll(must.toMap());
        }
        return Map.of("bool", query);
    }
}
