package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class EmergencyProtectionOrderReasonMappingFilterTest {
    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private EmergencyProtectionOrderReasonMappingFilter filter = new EmergencyProtectionOrderReasonMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "HARM_IF_NOT_MOVED_TO_NEW_ACCOMMODATION, 'There’s reasonable cause to believe the child is "
            + "likely to suffer significant harm if they’re not moved to accommodation provided by you, or on "
            + "your behalf'",
        "HARM_IF_KEPT_IN_CURRENT_ACCOMMODATION, 'There’s reasonable cause to believe the child is likely to "
            + "suffer significant harm if they don’t stay in their current accommodation'",
        "URGENT_ACCESS_TO_CHILD_IS_OBSTRUCTED, 'You’re making enquiries and need urgent access to the child to find "
            + "out about their welfare, and access is being unreasonably refused'"
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderReasonsType."
                + incorrectEnum);
    }
}
