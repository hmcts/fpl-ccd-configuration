package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import static java.util.Optional.ofNullable;

@Data
public class Request {
    private String uri;
    private String user;
    private JsonNode data;

    public String getDataAsString() {
        return ofNullable(data).map(JsonNode::toPrettyString).orElse(null);
    }
}
