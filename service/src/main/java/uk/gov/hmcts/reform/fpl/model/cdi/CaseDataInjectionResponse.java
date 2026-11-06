package uk.gov.hmcts.reform.fpl.model.cdi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CaseDataInjectionResponse {
    @JsonProperty("metadataFields")
    List<CdiMetadataField> metadataFields;
}

