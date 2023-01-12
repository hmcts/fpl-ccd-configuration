package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Builder
public class Sort implements  ESClause<List<Object>> {
    private final List<ESClause> clauses;

    @Override
    public List<Object> toMap() {
        return this.clauses.stream().map(ESClause::toMap).collect(Collectors.toList());
    }
}
