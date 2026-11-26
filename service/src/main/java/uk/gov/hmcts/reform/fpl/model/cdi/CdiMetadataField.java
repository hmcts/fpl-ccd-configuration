package uk.gov.hmcts.reform.fpl.model.cdi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CdiMetadataField {
    String id;
    String label;
    boolean hidden;
    Object value;
    boolean metadata;

    @JsonProperty("field_type")
    CdiFieldType fieldType;

    @JsonProperty("security_label")
    String securityLabel;

    public static CdiMetadataField text(String id, String label, Object value) {
        return CdiMetadataField.builder()
            .id(id)
            .label(label)
            .hidden(false)
            .value(value)
            .metadata(true)
            .fieldType(CdiFieldType.TEXT)
            .securityLabel("PUBLIC")
            .build();
    }

    public static CdiMetadataField number(String id, String label, Object value) {
        return CdiMetadataField.builder()
            .id(id)
            .label(label)
            .hidden(false)
            .value(value)
            .metadata(true)
            .fieldType(CdiFieldType.NUMBER)
            .securityLabel("PUBLIC")
            .build();
    }
}

