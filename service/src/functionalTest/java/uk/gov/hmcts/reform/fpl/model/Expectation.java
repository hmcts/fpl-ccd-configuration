package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Optional.ofNullable;

@Data
public class Expectation {
    private Integer status;
    private JsonNode data;

    public int getStatus() {
        return ObjectUtils.defaultIfNull(status, HTTP_OK);
    }

    public String getDataAsString() {
        return ofNullable(data).map(JsonNode::toPrettyString).orElse(null);
    }
}
