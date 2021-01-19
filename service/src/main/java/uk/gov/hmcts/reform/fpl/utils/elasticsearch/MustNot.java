package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class MustNot implements ESClause {
    private final List<ESClause> clauses;

    @Override
    public Map<String, Object> toMap() {
        List<Map<String, Object>> queries = this.clauses.stream().map(ESClause::toMap).collect(Collectors.toList());
        return Map.of("must_not", queries);
    }
}
