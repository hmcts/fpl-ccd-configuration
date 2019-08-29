package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.List;

import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class HasContactDirectionValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Enter the contact's full name";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldReturnAnErrorIfTelephoneIsNotPopulated() {
        ApplicantParty applicantParty = ApplicantParty.builder().build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfTelephoneIsPopulatedWithoutContactDirection() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder().build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfContactDirectionIsAnEmptyString() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .contactDirection(" ")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);

    }

    @Test
    void shouldNotReturnAnErrorIfContactDirectionIsPopulated() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .contactDirection("James Nelson")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);

    }
}
