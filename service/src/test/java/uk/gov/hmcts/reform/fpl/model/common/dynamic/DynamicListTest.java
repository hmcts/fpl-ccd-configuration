package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;

class DynamicListTest {

    private static final String LABEL = "PLACEHOLDER";

    private static final UUID UUID = fromString("d733b442-1b70-4abb-87b6-47e406d2e32a");

    @Test
    void shouldReturnValuesLabelWhenValueIsPopulated() {
        final DynamicListElement element = DynamicListElement.builder()
            .code(UUID)
            .label(LABEL)
            .build();

        final String label = DynamicList.builder().value(element).build().getValueLabel();
        assertThat(label).isEqualTo(LABEL);
    }

    @Test
    void shouldReturnNullLabelWhenValueIsNull() {
        final String label = DynamicList.builder().value(null).build().getValueLabel();
        assertThat(label).isNull();
    }

    @Test
    void shouldReturnValuesCodeWhenValueIsPopulated() {
        final DynamicListElement element = DynamicListElement.builder()
            .code(UUID)
            .label(LABEL)
            .build();

        final DynamicList dynamicList = DynamicList.builder().value(element).build();

        assertThat(dynamicList.getValueCode()).isEqualTo(UUID.toString());
        assertThat(dynamicList.getValueCodeAsUUID()).isEqualTo(UUID);
    }

    @Test
    void shouldReturnNullCodeWhenValueIsNull() {
        final DynamicList dynamicList = DynamicList.builder().value(null).build();
        assertThat(dynamicList.getValueCode()).isNull();
        assertThat(dynamicList.getValueCodeAsUUID()).isNull();
    }

    @Test
    void shouldAcceptNonUUIDCode() {
        final DynamicListElement element = DynamicListElement.builder()
            .code("Test string")
            .label(LABEL)
            .build();
        final DynamicList dynamicList = DynamicList.builder().value(element).build();
        assertThat(dynamicList.getValueCode()).isEqualTo("Test string");
    }

}
