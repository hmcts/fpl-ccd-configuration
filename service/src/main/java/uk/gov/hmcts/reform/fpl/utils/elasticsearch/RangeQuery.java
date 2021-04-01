package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@Builder
public class RangeQuery implements ESQuery {

    private final Object greaterThan;
    private final Object greaterThanOrEqual;
    private final Object lessThan;
    private final Object lessThanOrEqual;
    private final String field;

    @Override
    public Map<String, Object> toMap() {
        requireNonNull(field);
        Map<String, Object> ranges = new HashMap<>();
        if (greaterThan != null) {
            ranges.put("gt", greaterThan);
        }
        if (greaterThanOrEqual != null) {
            ranges.put("gte", greaterThanOrEqual);
        }
        if (lessThan != null) {
            ranges.put("lt", lessThan);
        }
        if (lessThanOrEqual != null) {
            ranges.put("lte", lessThanOrEqual);
        }
        return Map.of("range", Map.of(field, ranges));
    }
}
