package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;

import java.util.Map;

public interface ESQuery extends ESClause {
    default JSONObject toQueryContext() {
        return new JSONObject(Map.of("query", this.toMap()));
    }

    default JSONObject toQueryContext(int size) {
        return new JSONObject(Map.of("size", size, "query", this.toMap()));
    }
}
