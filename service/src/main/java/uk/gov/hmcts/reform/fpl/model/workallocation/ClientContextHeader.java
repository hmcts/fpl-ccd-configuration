package uk.gov.hmcts.reform.fpl.model.workallocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.json.deserializer.Base64Deserializer;

@JsonDeserialize(using = Base64Deserializer.class)
@Value
public class ClientContextHeader {

    @JsonProperty("client_context")
    ClientContext clientContext;
}
