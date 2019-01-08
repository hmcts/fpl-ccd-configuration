package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmergencyProtectionOrderMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private EmergencyProtectionOrderMappingFilter filter = new EmergencyProtectionOrderMappingFilter();

    @Test
    void shouldReturnCorrectLabelForChildWhereabouts() {
        assertThat(filter.apply("CHILD_WHEREABOUTS", NO_ARGS))
            .isEqualTo("Information on the whereabouts of the child");
    }

    @Test
    void shouldReturnCorrectLabelForEntryPremises() {
        assertThat(filter.apply("ENTRY_PREMISES", NO_ARGS))
            .isEqualTo("Authorisation for entry of premises");
    }

    @Test
    void shouldReturnCorrectLabelForSearchForAnotherChild() {
        assertThat(filter.apply("SEARCH_FOR_ANOTHER_CHILD", NO_ARGS))
            .isEqualTo("Authorisation to search for another child on the premises");
    }

    @Test
    void shouldReturnCorrectLabelForEmergencyProtectionOrderOther() {
        assertThat(filter.apply("OTHER", NO_ARGS))
            .isEqualTo("Other order under section 48 of the Children Act 1989");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderType."
                + incorrectEnum);
    }
}
