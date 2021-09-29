package uk.gov.hmcts.reform.fpl.model.common.dynamic;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.MAX_DYNAMIC_LIST_LABEL_LENGTH;

class DynamicListElementTest {

    private final UUID id = UUID.randomUUID();

    @Test
    void shouldTrimLabelWhenLongerThanMaxAllowedLength() {

        final String label = RandomStringUtils.randomAlphanumeric(MAX_DYNAMIC_LIST_LABEL_LENGTH + 50);
        final String expectedLabel = label.substring(0, MAX_DYNAMIC_LIST_LABEL_LENGTH - 3) + "...";

        final DynamicListElement element = DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();

        assertThat(element.getLabel()).isEqualTo(expectedLabel);
    }

    @Test
    void shouldNotTrimLabelWhenLMaxAllowedLengthLong() {

        final String label = RandomStringUtils.randomAlphanumeric(MAX_DYNAMIC_LIST_LABEL_LENGTH);

        final DynamicListElement element = DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();

        assertThat(element.getLabel()).isEqualTo(label);
    }

    @Test
    void shouldNotTrimLabelWhenShorterThanMaxAllowedLength() {

        final String label = RandomStringUtils.randomAlphanumeric(MAX_DYNAMIC_LIST_LABEL_LENGTH - 5);

        final DynamicListElement element = DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();

        assertThat(element.getLabel()).isEqualTo(label);
    }

    @Test
    void shouldReturnNullLabel() {

        final DynamicListElement element = DynamicListElement.builder()
            .code(id)
            .build();

        assertThat(element.getLabel()).isNull();
    }

}
