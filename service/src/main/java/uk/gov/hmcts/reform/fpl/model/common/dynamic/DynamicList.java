package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.DEFAULT_CODE;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.DEFAULT_LABEL;

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
    public static <T extends DynamicElementParser> DynamicList toDynamicList(List<T> elements) {
        return toDynamicList(elements, -1);
    }

    /**
     * Converts a list of elements to the appropriate structure to then be handled correctly by CCD.
     *
     * @param elements elements to convert into options for the dropdown
     * @param index    index of the element that will become the default selected value, < 0 is an empty element
     * @param <T>      a class that implements {@link DynamicElementParser#toDynamicElement()}
     * @return a {@link DynamicList} to be sent to CCD
     */
    public static <T extends DynamicElementParser> DynamicList toDynamicList(List<T> elements,
                                                                             int index) {
        DynamicListElement selected;

        if (index >= elements.size()) {
            throw new IllegalArgumentException(String.format("The given index (%d) must be less than elements.size()",
                index));
        }

        if (index >= 0) {
            selected = elements.get(index).toDynamicElement();
        } else {
            // Create an empty selected value
            selected = DynamicListElement.builder().code(DEFAULT_CODE).label(DEFAULT_LABEL).build();
        }

        return toDynamicList(elements, selected);
    }

    /**
     * Converts a list of elements to the appropriate structure to then be handled correctly by CCD.
     *
     * @param elements elements to convert into options for the dropdown
     * @param selected a {@link DynamicListElement} that will be the default selected element of the list
     * @param <T>      a class that implements {@link DynamicElementParser#toDynamicElement()}
     * @return a {@link DynamicList} to be sent to CCD
     */
    public static <T extends DynamicElementParser> DynamicList toDynamicList(List<T> elements,
                                                                             DynamicListElement selected) {
        List<DynamicListElement> items = elements.stream()
            .map(DynamicElementParser::toDynamicElement)
            .collect(Collectors.toList());

        return DynamicList.builder().listItems(items).value(selected).build();
    }

    /**
     * Prepares this class for storage in CCD by clearing the list items.
     */
    public void prepareForStorage() {
        // TODO: 14/10/2019 Remove if not needed
        listItems.clear();
    }

    /**
     * Updates the current {@link DynamicList} with the passed {@link DynamicList}. The selected item is updated
     * based on label if no matching item is found.
     *
     * @param update List to update with
     * @return a new instance of an updated list
     */
    public DynamicList update(DynamicList update) {
        if (update == null) {
            return this;
        }

        List<DynamicListElement> list = update.getListItems();

        // Check for change
        if (list.equals(this.listItems)) {
            return this;
        }

        // There has been change, check if current selected value occurs in the list
        // Could be fully equals or just the same label
        DynamicListElement selected;

        if (list.contains(this.value)) {
            selected = this.value;
        } else {
            // Get the item that has a matching id/code
            selected = list.stream()
                .filter(element -> element.getLabel().equals(this.value.getLabel()))
                .findFirst()
                .orElse(null);

            if (selected == null) {
                selected = this.value; // Just set back to value for now
            }
        }

        return DynamicList.builder().value(selected).listItems(list).build();
    }

}
