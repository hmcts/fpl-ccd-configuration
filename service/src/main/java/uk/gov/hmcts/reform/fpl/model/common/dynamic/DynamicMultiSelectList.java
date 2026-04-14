package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private List<DynamicMultiselectListElement> value;

    /**
     * List of options for the multiselect options.
     */
    @JsonProperty("list_items")
    private List<DynamicMultiselectListElement> listItems;

    @JsonIgnore
    public String getValueLabel() {
        return value == null ? null : value.toString();
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
