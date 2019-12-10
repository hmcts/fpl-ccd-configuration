package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;

class DynamicListTest {

    private static final String LABEL = "PLACEHOLDER";

    private static final UUID UUID = fromString("d733b442-1b70-4abb-87b6-47e406d2e32a");
    private static final DynamicListElement VALUE = DynamicListElement.builder().code(UUID).label(LABEL).build();

    @Test
    void shouldReturnValuesLabelWhenValueIsPopulated() {
        final String label = DynamicList.builder().value(VALUE).build().getValueLabel();
        assertThat(label).isEqualTo(LABEL);
    }

    @Test
    void shouldReturnNullLabelWhenValueIsNull() {
        final String label = DynamicList.builder().value(null).build().getValueLabel();
        assertThat(label).isNull();
    }

    @Test
    void shouldReturnValuesCodeWhenValueIsPopulated() {
        final UUID code = DynamicList.builder().value(VALUE).build().getValueCode();
        assertThat(code).isEqualTo(UUID);
    }

    @Test
    void shouldReturnNullCodeWhenValueIsNull() {
        final UUID code = DynamicList.builder().value(null).build().getValueCode();
        assertThat(code).isNull();
    }
}
