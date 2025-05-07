package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.abbreviate;

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

    static final int MAX_DYNAMIC_LIST_LABEL_LENGTH = 250;

    public DynamicListElement(String code, String label) {
        this.code = code;
        this.label = abbreviate(label, MAX_DYNAMIC_LIST_LABEL_LENGTH);
    }

    /**
     * Property that maps to the value attribute of the option tag.
     */
    private final String code;

    /**
     * Property that maps to the label attribute of the option tag.
     */
    private final String label;

    public boolean hasCode(String code) {
        return Objects.equals(this.code, code);
    }

    public boolean hasCode(UUID code) {
        return hasCode(Optional.ofNullable(code).map(UUID::toString).orElse(null));
    }

    public static DynamicListElement defaultListItem(String label) {
        return DynamicListElement.builder()
            .code(DEFAULT_CODE)
            .label(label)
            .build();
    }

    public static class DynamicListElementBuilder {
        private String code;

        public DynamicListElementBuilder code(String code) {
            this.code = code;
            return this;
        }

        public DynamicListElementBuilder code(UUID code) {
            this.code = Optional.ofNullable(code).map(UUID::toString).orElse(null);
            return this;
        }

    }
}
