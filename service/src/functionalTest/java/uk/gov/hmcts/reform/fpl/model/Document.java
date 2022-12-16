package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    @JsonProperty("document_binary_url")
    String documentBinaryUrl;

    @JsonProperty("document_url")
    String documentUrl;

}
