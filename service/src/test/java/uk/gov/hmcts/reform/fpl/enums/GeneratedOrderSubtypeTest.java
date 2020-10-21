package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedOrderSubtypeTest {

    @Test
    void testSubtypeNotFound() {
        assertThat(GeneratedOrderSubtype.fromType("blah blah")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void testLabelFound(GeneratedOrderSubtype subtype) {
        assertThat(GeneratedOrderSubtype.fromType("blah blah " + subtype.getLabel())).isEqualTo(Optional.of(subtype));
    }

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void testAllSubtypesAreMapped(GeneratedOrderSubtype subtype) {
        assertThat(GeneratedOrderSubtype.fromType(subtype.getLabel())).isPresent();
    }

}
