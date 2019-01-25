package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ChildGenderMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private ChildGenderMappingFilter filter = new ChildGenderMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "GIRL, 'Girl'",
        "BOY, 'Boy'",
        "IDENTIFIED_IN_ANOTHER_WAY, 'They identify in another way'",
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.ChildGenderListType."
                + incorrectEnum);
    }
}
