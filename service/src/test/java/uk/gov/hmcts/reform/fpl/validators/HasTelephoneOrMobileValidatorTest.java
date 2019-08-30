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
class HasTelephoneOrMobileValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Enter at least one telephone number for the contact";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldNotReturnAnErrorIfTelephoneExists() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfMobileNumberExists() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .mobileNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfBothTelephoneAndMobileNumberExist() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .mobileNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfTelephoneNumberIsNotEmptyAndMobileNumberIsEmpty() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .mobileNumber(Telephone.builder()
                .telephoneNumber("")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfBothTelephoneAndMobileNumberAreNotPopulated() {
        ApplicantParty applicantParty = ApplicantParty.builder().build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfApplicantTelephoneIsEmptyString() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .mobileNumber(Telephone.builder()
                .telephoneNumber("")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfTelephoneAndMobileNumberAreEmptyStrings() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .telephoneNumber("")
                .build())
            .mobileNumber(Telephone.builder()
                .telephoneNumber("")
                .build())
            .build();

        List<String> errorMessages = validator.validate(applicantParty).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
