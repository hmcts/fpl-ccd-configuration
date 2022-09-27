package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.EqualsAndHashCode;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public class SortQuery implements ESQuery {
    private final String field;
    private final SortOrder sortOrder;

    public SortQuery(String field, SortOrder sortOrder) {
        requireNonNull(field);
        requireNonNull(sortOrder);
        this.field = field;
        this.sortOrder = sortOrder;
    }

    public static SortQuery of(String field, SortOrder sortOrder) {
        return new SortQuery(field, sortOrder);
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of(field, Map.of("order", sortOrder.getOrder()));
    }
}
