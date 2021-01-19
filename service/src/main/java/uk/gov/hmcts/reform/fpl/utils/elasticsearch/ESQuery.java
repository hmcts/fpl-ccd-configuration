package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;

import java.util.Map;

public interface ESQuery extends ESClause {
    default String toQueryString() {
        return new JSONObject(Map.of("query", this.toMap())).toString();
    }
}
