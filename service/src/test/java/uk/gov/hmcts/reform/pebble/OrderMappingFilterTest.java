package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class OrderMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private OrderMappingFilter filter = new OrderMappingFilter();

    @Test
    void shouldReturnCorrectLabelForCareOrder() {
        assertThat(filter.apply("CARE_ORDER", NO_ARGS))
            .isEqualTo("Care order");
    }

    @Test
    void shouldReturnCorrectLabelForInterimCareOrder() {
        assertThat(filter.apply("INTERIM_CARE_ORDER", NO_ARGS))
            .isEqualTo("Interim care order");
    }

    @Test
    void shouldReturnCorrectLabelForSupervivsionOrder() {
        assertThat(filter.apply("SUPERVISION_ORDER", NO_ARGS))
            .isEqualTo("Supervision order");
    }

    @Test
    void shouldReturnCorrectLabelForInterimSupervisionOrder() {
        assertThat(filter.apply("INTERIM_SUPERVISION_ORDER", NO_ARGS))
            .isEqualTo("Interim supervision order");
    }

    @Test
    void shouldReturnCorrectLabelForEducationSupervisionOrder() {
        assertThat(filter.apply("EDUCATION_SUPERVISION_ORDER", NO_ARGS))
            .isEqualTo("Education supervision order");
    }

    @Test
    void shouldReturnCorrectLabelForEmergencyProtectionOrder() {
        assertThat(filter.apply("EMERGENCY_PROTECTION_ORDER", NO_ARGS))
            .isEqualTo("Emergency protection order");
    }

    @Test
    void shouldReturnCorrectLabelForOther() {
        assertThat(filter.apply("OTHER", NO_ARGS))
            .isEqualTo("Other order under part 4 of the Children Act 1989");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.OrderType." + incorrectEnum);
    }
}
