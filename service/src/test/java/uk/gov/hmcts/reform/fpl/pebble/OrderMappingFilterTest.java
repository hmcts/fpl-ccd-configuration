package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.fpl.pebble.OrderMappingFilter;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private OrderMappingFilter filter = new OrderMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "CARE_ORDER, 'Care order'",
        "INTERIM_CARE_ORDER, 'Interim care order'",
        "SUPERVISION_ORDER, 'Supervision order'",
        "INTERIM_SUPERVISION_ORDER, 'Interim supervision order'",
        "EDUCATION_SUPERVISION_ORDER, 'Education supervision order'",
        "EMERGENCY_PROTECTION_ORDER, 'Emergency protection order'",
        "OTHER, 'Other order under part 4 of the Children Act 1989'"
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.OrderType." + incorrectEnum);
    }
}
