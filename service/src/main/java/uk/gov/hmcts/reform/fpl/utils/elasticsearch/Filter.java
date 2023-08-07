package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;

import java.util.List;
import java.util.Map;


@Builder
public class Filter implements ESClause<Map<String, Object>> {
    private final List<ESClause> clauses;
    private TermQuery termQuery;
    private TermsQuery termsQuery;
    private RangeQuery rangeQuery;

    @Override
    public Map<String, Object> toMap() {
        return Map.of("filter", clauses.stream().map(ESClause::toMap).toList());
    }
}
