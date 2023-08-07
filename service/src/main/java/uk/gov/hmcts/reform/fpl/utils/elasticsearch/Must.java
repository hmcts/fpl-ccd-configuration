package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;


@EqualsAndHashCode
@Builder
public class Must implements ESClause {
    private final List<ESClause> clauses;

    @Override
    public Map<String, Object> toMap() {
        return Map.of("must", this.clauses.stream().map(ESClause::toMap).toList());
    }
}
