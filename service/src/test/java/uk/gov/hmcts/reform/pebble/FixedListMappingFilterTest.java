package uk.gov.hmcts.reform.pebble;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class FixedListMappingFilterTest {

    private static final ImmutableMap<String, Object> NO_ARGS = ImmutableMap.<String, Object>builder().build();

    private FixedListMappingFilter filter = new FixedListMappingFilter();

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
        assertThat(filter.apply("EMERGENCY_PROTECTION_ORDER_OTHER", NO_ARGS))
            .isEqualTo("Other order under section 48 of the Children Act 1989");
    }

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
        assertThat(filter.apply("EMERGENCY_PROTECTION_DIRECTION_OTHER", NO_ARGS))
            .isEqualTo("Other direction relating to an emergency protection order");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithIncorrectEnum() {
        String incorrectEnum = "TEST";

        Assertions.assertThatThrownBy(() -> filter.apply(incorrectEnum, NO_ARGS))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No enum constant uk.gov.hmcts.reform.fpl.config.utils.FixedListMappingType." + incorrectEnum);
    }
}
