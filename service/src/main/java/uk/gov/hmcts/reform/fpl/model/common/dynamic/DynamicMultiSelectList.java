package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

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

    @JsonIgnore
    public static <T> List<Element<T>> getSelectedElementFromMultiSelectList(
        List<Element<T>> elements,
        DynamicMultiSelectList dynamicMultiSelectList) {
        if (isNull(dynamicMultiSelectList) || isEmpty(dynamicMultiSelectList.getValue())) {
            return Collections.emptyList();
        }
        return elements.stream().filter(element -> dynamicMultiSelectList.getValue().stream()
            .anyMatch(listValue -> listValue.hasCode(element.getId())))
        .toList();
    }
}
