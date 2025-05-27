package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CaseViewField {
    private String id;
    private String label;
    private Object value;

    @JsonProperty("formatted_value")
    private Object formattedValue;

    @JsonProperty("field_type")
    private FieldType fieldType;
    @JsonProperty("security_label")
    private final String securityLabel = "PUBLIC";

    private final boolean metadata = true;
    @Builder.Default
    private boolean hidden = true;


    @Data
    @Builder
    @AllArgsConstructor
    public static class FieldType {
        private final String id;
        private final String type;
        private final String min = null;
        private final String max = null;

        @JsonProperty("regular_expression")
        private final String regularExpression = null;
        @JsonProperty("fixed_list_items")
        private final List<Object> fixedListItems = List.of();
        @JsonProperty("complex_fields")
        private final List<Object> complexFields = List.of();
        @JsonProperty("collection_field_type")
        private final FieldType collectionFieldType = null;

        public static FieldType TEXT = new FieldType("Text", "Text");
    }

}

