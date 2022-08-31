package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class Filter implements ESClause{
    private final List<ESClause> clauses;
    private TermQuery termQuery;
    private TermsQuery termsQuery;
    private RangeQuery rangeQuery;

    @Override
    public Map<String, Object> toMap() {
        return Map.of("filter", clauses.stream().map(ESClause::toMap).collect(Collectors.toList()));
    }
}
