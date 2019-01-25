package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class HearingTimeFrameMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private HearingTimeFrameMappingFilter filter = new HearingTimeFrameMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "SAME_DAY, 'Same day'",
        "WITHIN_2_DAYS, 'Within 2 days'",
        "WITHIN_7_DAYS, 'Within 7 days'",
        "WITHIN_12_DAYS, 'Within 12 days'",
        "WITHIN_18_DAYS, 'Within 18 days'",
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.HearingTimeFrameType."
                + incorrectEnum);
    }
}
