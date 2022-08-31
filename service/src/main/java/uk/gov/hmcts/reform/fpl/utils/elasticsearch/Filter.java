package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@Builder
public class Filter implements ESClause{
    private TermQuery termQuery;
    private TermsQuery termsQuery;
    private RangeQuery rangeQuery;

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> query = new HashMap<>();

        if(termQuery != null) {
            query.putAll(termQuery.toMap());
        }
        if (termsQuery != null) {
            query.putAll(termsQuery.toMap());
        }
        if (rangeQuery != null) {
            query.putAll(rangeQuery.toMap());
        }

        return Map.of("filter", query);
    }
}
