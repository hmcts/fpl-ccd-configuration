package uk.gov.hmcts.reform.fpl.model.common.dynamic;

/**
 * Interface that provides a contract that allows an object to be converted into a {@link DynamicListElement}.
 */
public interface DynamicElementIndicator {

    /**
     * Coverts the instance of the implementor into a {@link DynamicListElement}.
     *
     * <p>To be converted there needs to be a relevant mapping for a property to a code string, and another to a value
     * string.
     *
     * @return the converted {@link DynamicListElement}
     */
    DynamicListElement toDynamicElement();
}
