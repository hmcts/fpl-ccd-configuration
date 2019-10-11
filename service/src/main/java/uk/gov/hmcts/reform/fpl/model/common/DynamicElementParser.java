package uk.gov.hmcts.reform.fpl.model.common;

/**
 * Class that allows another to be converted into a {@link DynamicListElement}.
 */
public interface DynamicElementParser {

    /**
     * Coverts the instance of the class into a {@link DynamicListElement}.
     *
     * <p>To be converted there needs to be a relevant mapping for a property to a code string, and another to a value
     * string.
     *
     * @return the converted {@link DynamicListElement}
     */
    DynamicListElement toDynamicElement();
}
