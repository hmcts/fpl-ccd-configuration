package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * An element of the {@link DynamicList}.
 *
 * <p>There are two properties which map to the relevant items of an option html tag.
 */
@Data
@Jacksonized
@Builder
public class DynamicListElement {
    public static final UUID DEFAULT_CODE = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final String DEFAULT_LABEL = "";
    public static final DynamicListElement EMPTY = DynamicListElement.builder().build();

    /**
     * Property that maps to the value attribute of the option tag.
     */
    private final UUID code;

    /**
     * Property that maps to the label attribute of the option tag.
     */
    private final String label;
}
