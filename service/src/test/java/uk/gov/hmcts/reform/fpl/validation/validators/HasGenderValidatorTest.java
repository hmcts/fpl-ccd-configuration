package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HasGenderValidatorTest extends AbstractValidationTest {

    private static final String ERROR_MESSAGE = "Tell us the gender of all children in the case";

    @ParameterizedTest
    @ValueSource(strings = {"Boy", "Girl"})
    void shouldNotReturnAnErrorIfStandardGenderProvided(String standardGender) {
        ChildParty child = ChildParty.builder()
            .gender(standardGender)
            .build();

        List<String> validationErrors = validate(child);

        assertThat(validationErrors).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfNonStandardGenderProvided() {
        ChildParty child = ChildParty.builder()
            .gender(ChildGender.OTHER.getLabel())
            .genderIdentification("other")
            .build();

        List<String> validationErrors = validate(child);

        assertThat(validationErrors).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfGenderIsNotProvided() {
        ChildParty child = ChildParty.builder().build();

        List<String> validationErrors = validate(child);
        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfNonStandardGenderIsNotProvided() {
        ChildParty child = ChildParty.builder()
            .gender(ChildGender.OTHER.getLabel())
            .build();

        List<String> validationErrors = validate(child);

        assertThat(validationErrors).contains(ERROR_MESSAGE);
    }
}
