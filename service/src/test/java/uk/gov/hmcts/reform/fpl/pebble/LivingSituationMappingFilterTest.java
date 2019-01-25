package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class LivingSituationMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private LivingSituationMappingFilter filter = new LivingSituationMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "LIVING_WITH_RESPONDENTS, 'Living with respondents'",
        "LIVING_WITH_OTHER_FAMILY_OR_FRIENDS, 'Living with other family or friends'",
        "REMOVED_BY_POLICE_POWER_ENDING_SOON, 'Removed by Police, powers ending soon'",
        "VOLUNTARILY_IN_SECTION_20_CARE_ORDER, 'Voluntarily in section 20 care order'",
        "IN_HOSPITAL_AND_SOON_TO_BE_DISCHARGED, 'In hospital and soon to be discharged'",
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.LivingSituationListType."
                + incorrectEnum);
    }
}
