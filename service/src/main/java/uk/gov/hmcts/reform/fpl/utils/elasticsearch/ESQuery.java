package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface ESQuery extends ESClause {
    default JSONObject toQueryContext(int size, int from) {
        return new JSONObject(Map.of("size", size, "from", from, "query", this.toMap()));
    }

    default JSONObject toQueryContext(int size, int from, Sort sort) {
        return new JSONObject(Map.of(
                "size", size,
                "from", from,
                "query", this.toMap(),
                "sort", sort.toMap())
        );
    }

    default JSONObject toQueryContext(int size, int from, List<String> soruce) {
        return new JSONObject(Map.of(
            "size", size,
            "from", from,
            "query", this.toMap(),
            "_source", soruce));
    }
}
