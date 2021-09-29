package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.common.Element.newElement;

class ElementTest {

    @Test
    void shouldCreatNewElement() {

        final Element<String> actualElement = newElement("A");
        final Element<String> expectedElement = Element.<String>builder().value("A").build();

        assertThat(actualElement).isEqualTo(expectedElement);
    }
}
