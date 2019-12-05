package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;

class DynamicListTest {

    private DynamicList.DynamicListBuilder builder;
    private static final UUID UUID = fromString("d733b442-1b70-4abb-87b6-47e406d2e32a");
    private static final DynamicListElement VALUE = DynamicListElement.builder().code(UUID).label("").build();

    @BeforeEach
    void setUp() {
        builder = DynamicList.builder();
    }

    @Test
    void shouldReturnValuesLabelWhenValueIsPopulated() {
        final String label = builder.value(VALUE).build().getValueLabel();
        assertThat(label).isEqualTo("");
    }

    @Test
    void shouldReturnNullLabelWhenValueIsNull() {
        final String label = builder.value(null).build().getValueLabel();
        assertThat(label).isNull();
    }

    @Test
    void shouldReturnValuesCodeWhenValueIsPopulated() {
        final UUID code = builder.value(VALUE).build().getValueCode();
        assertThat(code).isEqualTo(UUID);
    }

    @Test
    void shouldReturnNullCodeWhenValueIsNull() {
        final UUID code = builder.value(null).build().getValueCode();
        assertThat(code).isNull();
    }
}
