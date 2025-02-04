package uk.gov.hmcts.reform.fpl.controllers;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.handlers.CaseEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;


@Slf4j
@RestController
@RequestMapping("/callback/getCase")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MetadataController extends CallbackController {

    private final CaseEventHandler caseEventHandler;

    @PostMapping("/metadata")
    public Map<String, Object> getMetaData(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);

        return Map.of("metadataFields", List.of(
            CaseViewField.builder()
                .id("[INJECTED_DATA.HAS_3RD_PARTY]")
                .label("Has a respondent local authority been added to the case?")
                .value(isEmpty(caseData.getRespondentLocalAuthority()) ? "No" : "Yes")
                .fieldType(FieldType.TEXT)
                .build()/*,
            CaseViewField.builder()
                .id("[INJECTED_DATA__TASK_LIST]")
                .label("Task List")
                .value(List.of(OPEN, RETURNED).contains(caseData.getState())
                    ? caseEventHandler.getUpdates(request.getCaseDetails()).get("taskList") : "")
                .fieldType(FieldType.TEXT)
                .hidden(false)
                .build() */
        ));
    }

    @Data
    @Builder
    @AllArgsConstructor
    static class CaseViewField {
        private String id;
        private String label;
        private Object value;

        @JsonProperty("formatted_value")
        private Object formattedValue;

        public Object getFormattedValue() {
            return value;
        }

        @JsonProperty("field_type")
        private FieldType fieldType;
        @JsonProperty("security_label")
        private final String securityLabel = "PUBLIC";

        private final boolean metadata = true;
        @Builder.Default
        private boolean hidden = true;
    }

    @Data
    @Builder
    @AllArgsConstructor
    static class FieldType {
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

