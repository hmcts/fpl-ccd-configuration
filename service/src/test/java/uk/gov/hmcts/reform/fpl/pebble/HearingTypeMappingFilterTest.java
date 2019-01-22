package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class HearingTypeMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private HearingTypeMappingFilter filter = new HearingTypeMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "STANDARD_CASE_HEARING, 'Standard case management hearing'",
        "URGENT_PRELIMINARY_HEARING, 'Urgent preliminary case management hearing'",
        "EMERGENCY_PROTECTION_HEARING, 'Emergency protection order hearing'",
        "CONTESTED_INTERIM_HEARING, 'Contested interim care order hearing'"
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.HearingType."
                + incorrectEnum);
    }
}
