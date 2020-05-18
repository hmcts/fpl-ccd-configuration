package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HasContactDirectionValidatorTest extends AbstractValidationTest {

    private static final String ERROR_MESSAGE = "Enter the contact's full name";

    @Test
    void shouldReturnAnErrorIfTelephoneIsNotPopulated() {
        ApplicantParty applicantParty = ApplicantParty.builder().build();

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfTelephoneIsPopulatedWithoutContactDirection() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder().build())
            .build();

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfContactDirectionIsAnEmptyString() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .contactDirection(" ")
                .build())
            .build();

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfContactDirectionIsPopulated() {
        ApplicantParty applicantParty = ApplicantParty.builder()
            .telephoneNumber(Telephone.builder()
                .contactDirection("James Nelson")
                .build())
            .build();

        List<String> errorMessages = validate(applicantParty);

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }
}
