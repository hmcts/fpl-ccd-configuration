package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Applicant;

import java.util.List;

import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasTelephoneValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Enter at least one telephone number for the contact";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldNotReturnAnErrorIfApplicantTelephoneExists() {
        Applicant applicant = Applicant.builder()
            .telephone("12345678")
            .build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfApplicantMobileExists() {
        Applicant applicant = Applicant.builder()
            .mobile("12345678")
            .build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfBothApplicantTelephoneAndMobileExist() {
        Applicant applicant = Applicant.builder()
            .telephone("12345678")
            .mobile("12345678")
            .build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfApplicantTelephoneIsNotEmptyAndMobileNumberIsEmpty() {
        Applicant applicant = Applicant.builder()
            .telephone("123")
            .mobile("")
            .build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfBothApplicantTelephoneAndMobileDoNotExist() {
        Applicant applicant = Applicant.builder().build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfApplicantTelephoneIsEmptyString() {
        Applicant applicant = Applicant.builder()
            .telephone("")
            .build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfApplicantTelephoneAndMobileNumberAreEmptyStrings() {
        Applicant applicant = Applicant.builder()
            .telephone("")
            .mobile("")
            .build();

        List<String> errorMessages = validator.validate(applicant).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
