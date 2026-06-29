package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Representation of a CCD Dynamic List which is then converted to a select dropdown list.
 */
@Data
@Jacksonized
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class DynamicMultiSelectList {

    /**
     * The selected value for the multiselect options.
     */
    @JsonProperty("value")
    private List<DynamicMultiSelectListElement> value;

    /**
     * List of options for the multiselect options.
     */
    @JsonProperty("list_items")
    private List<DynamicMultiSelectListElement> listItems;
}
