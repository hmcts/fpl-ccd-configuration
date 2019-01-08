package uk.gov.hmcts.reform.fpl.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("LineLength")
public class EmergencyProtectionOrderDirectionMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private EmergencyProtectionOrderDirectionMappingFilter filter = new EmergencyProtectionOrderDirectionMappingFilter();

    @ParameterizedTest()
    @CsvSource({
        "CONTACT_WITH_NAMED_PERSON, 'Contact with any named person'",
        "CHILD_MEDICAL_ASSESSMENT, 'A medical or psychiatric examination, or another assessment of the child'",
        "MEDICAL_PRACTITIONER, 'To be accompanied by a registered medical practitioner, nurse or midwife'",
        "EXCLUSION_REQUIREMENT, 'An exclusion requirement'",
        "OTHER, 'Other direction relating to an emergency protection order'"
    })
    void shouldReturnCorrectLabel(String code, String label) {
        assertThat(filter.apply(code, NO_ARGS)).isEqualTo(label);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType." + incorrectEnum);
    }
}
