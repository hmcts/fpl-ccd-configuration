package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Applicant;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasTelephoneValidatorTest {
    private HasTelephoneValidator validator = new HasTelephoneValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    void shouldReturnFalseIfBothApplicantTelephoneAndMobileDoNotExist() {
        Applicant applicant = Applicant.builder().build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicantTelephoneExists() {
        Applicant applicant = Applicant.builder()
            .telephone("12345678")
            .build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueIfApplicantMobileExists() {
        Applicant applicant = Applicant.builder()
            .mobile("12345678")
            .build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueIfBothApplicantTelephoneAndMobileExist() {
        Applicant applicant = Applicant.builder()
            .telephone("12345678")
            .mobile("12345678")
            .build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseIfApplicantTelephoneIsEmptyString() {
        Applicant applicant = Applicant.builder()
            .telephone("")
            .build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseIfApplicantTelephoneAndMobileNumberAreEmptyStrings() {
        Applicant applicant = Applicant.builder()
            .telephone("")
            .mobile("")
            .build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueIfApplicantTelephoneIsNotEmptyAndMobileNumberIsEmpty() {
        Applicant applicant = Applicant.builder()
            .telephone("123")
            .mobile("")
            .build();
        Boolean isValid = validator.isValid(applicant, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }
}
