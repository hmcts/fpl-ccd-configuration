package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of a CCD Dynamic List which is then converted to a select dropdown list.
 */
@Data
@Builder
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
     * <p>The selected value will be blank by default and have a code value of -1.
     *
     * @param elements elements to convert into options for the dropdown
     * @return a {@link DynamicList} to be sent to CCD
     */
    public static <T extends DynamicElementParser> DynamicList toDynamicList(Collection<T> elements) {
        return toDynamicList(elements, -1);
    }

    /**
     * Converts a list of elements to the appropriate structure to then be handled correctly by CCD.
     *
     * @param elements elements to convert into options for the dropdown
     * @param index    index of the element that will become the default selected value
     * @param <T>      a class that implements {@link DynamicElementParser#toDynamicElement()}
     * @return a {@link DynamicList} to be sent to CCD
     */
    public static <T extends DynamicElementParser> DynamicList toDynamicList(Collection<T> elements,
                                                                             int index) {
        List<DynamicListElement> items = elements.stream()
            .map(DynamicElementParser::toDynamicElement)
            .collect(Collectors.toList());

        DynamicListElement value;

        if (index > 0) {
            value = items.get(0);
        } else {
            // Create an empty selected value
            value = DynamicListElement.builder().code("-1").label("").build();
        }

        return DynamicList.builder().listItems(items).value(value).build();
    }
}
