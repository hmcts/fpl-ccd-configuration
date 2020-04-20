package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HasTelephoneOrMobileValidatorTest extends AbstractValidationTest {

    private static final String ERROR_MESSAGE = "Enter at least one telephone number for the contact";

    @Test
    void shouldNotReturnAnErrorIfTelephoneExists() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .build();

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfMobileNumberExists() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .mobileNumber(Telephone.builder()
                .telephoneNumber("12345678")
                .build())
            .build();

        List<String> errorMessages = validate(applicantParty);

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

        List<String> errorMessages = validate(applicantParty);

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

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfBothTelephoneAndMobileNumberAreNotPopulated() {
        ApplicantParty applicantParty = ApplicantParty.builder().build();

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfApplicantTelephoneIsEmptyString() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .mobileNumber(Telephone.builder()
                .telephoneNumber("")
                .build())
            .build();

        List<String> errorMessages = validate(applicantParty);

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

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
