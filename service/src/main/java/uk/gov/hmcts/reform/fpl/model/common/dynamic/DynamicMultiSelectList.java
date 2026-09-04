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
import java.util.Optional;
import java.util.UUID;

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
    public String getValueLabel() {
        return value == null ? null : value.toString();
    }

    @JsonIgnore
    public <T> List<Element<T>> getSelectedElementsFromMultiSelectList(List<Element<T>> elements) {
        if (isEmpty(value)) {
            return Collections.emptyList();
        }
        return elements.stream().filter(element -> value.stream()
                .anyMatch(listValue -> listValue.hasCode(element.getId())))
            .toList();
    }

    @JsonIgnore
    public UUID getValueCodeAsUuid() {
        return Optional.ofNullable(getValueCode()).map(UUID::fromString).orElse(null);
    }

    @JsonIgnore
    public String getValueCode() {
        return value == null ? null : value.toString();
    }
}
