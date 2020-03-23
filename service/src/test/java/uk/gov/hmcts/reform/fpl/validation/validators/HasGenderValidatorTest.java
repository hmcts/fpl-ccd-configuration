package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LocalValidatorFactoryBean.class)
class HasGenderValidatorTest {

    private static final String ERROR_MESSAGE = "Tell us the gender of all children in the case";

    @Autowired
    private Validator validator;

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

    private List<String> validate(ChildParty child) {
        return validator.validate(child).stream()
            .map(ConstraintViolation::getMessage)
            .collect(toList());
    }
}
