package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmergencyProtectionOrderDirectionMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private EmergencyProtectionOrderDirectionMappingFilter filter = new EmergencyProtectionOrderDirectionMappingFilter();

    @Test
    void shouldReturnCorrectLabelForContactWithNamedPerson() {
        assertThat(filter.apply("CONTACT_WITH_NAMED_PERSON", NO_ARGS))
            .isEqualTo("Contact with any named person");
    }

    @Test
    void shouldReturnCorrectLabelForChildMedicalAssessment() {
        assertThat(filter.apply("CHILD_MEDICAL_ASSESSMENT", NO_ARGS))
            .isEqualTo("A medical or psychiatric examination, or another assessment of the child");
    }

    @Test
    void shouldReturnCorrectLabelForMedicalPractitioner() {
        assertThat(filter.apply("MEDICAL_PRACTITIONER", NO_ARGS))
            .isEqualTo("To be accompanied by a registered medical practitioner, nurse or midwife");
    }

    @Test
    void shouldReturnCorrectLabelForExclusionRequirement() {
        assertThat(filter.apply("EXCLUSION_REQUIREMENT", NO_ARGS))
            .isEqualTo("An exclusion requirement");
    }

    @Test
    void shouldReturnCorrectLabelForEmergencyProtectionDirectionOther() {
        assertThat(filter.apply("OTHER", NO_ARGS))
            .isEqualTo("Other direction relating to an emergency protection order");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionType." + incorrectEnum);
    }
}
