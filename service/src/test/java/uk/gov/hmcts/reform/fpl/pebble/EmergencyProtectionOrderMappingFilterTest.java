package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class EmergencyProtectionOrderMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private EmergencyProtectionOrderMappingFilter filter = new EmergencyProtectionOrderMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "CHILD_WHEREABOUTS, 'Information on the whereabouts of the child'",
        "ENTRY_PREMISES, 'Authorisation for entry of premises'",
        "SEARCH_FOR_CHILD, 'Authorisation to search for another child on the premises'",
        "OTHER, 'Other order under section 48 of the Children Act 1989'"
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType."
                + incorrectEnum);
    }
}
