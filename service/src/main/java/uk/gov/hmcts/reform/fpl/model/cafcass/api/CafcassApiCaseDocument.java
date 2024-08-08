package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CafcassApiCaseDocument {
    @JsonProperty("document_filename")
    private String documentFileName;
    private boolean removed;
    private String documentCategory;
    private String documentId;
}
