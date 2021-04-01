package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;

import java.util.Map;

public interface ESQuery extends ESClause {
    default JSONObject toQueryContext(int size, int from) {
        return new JSONObject(Map.of("size", size, "from", from, "query", this.toMap()));
    }
}
