package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;

/**
 * Representation of a CCD Dynamic List which is then converted to a select dropdown list.
 */
@Data
@Builder
@ComplexType(generate = false)
public class DynamicList {

    /**
     * The selected value for the dropdown.
     */
    private DynamicListElement value;

    /**
     * List of options for the dropdown.
     */
    @JsonProperty("list_items")
    private List<DynamicListElement> listItems;

    /**
     * Converts a list of elements to the appropriate structure to then be handled correctly by CCD.
     *
     * @param elements elements to convert into options for the dropdown
     * @param selected a {@link DynamicListElement} that will be the default selected element of the list
     * @param <T>      a class that implements {@link DynamicElementIndicator#toDynamicElement()}
     * @return a {@link DynamicList} to be sent to CCD
     */
    public static <T extends DynamicElementIndicator> DynamicList toDynamicList(List<T> elements,
                                                                                DynamicListElement selected) {
        List<DynamicListElement> items = elements.stream()
            .map(DynamicElementIndicator::toDynamicElement)
            .collect(Collectors.toList());

        return DynamicList.builder().listItems(items).value(selected).build();
    }

    public String getValueLabel() {
        return value == null ? null : value.getLabel();
    }

    public UUID getValueCode() {
        return value == null ? null : value.getCode();
    }
}
