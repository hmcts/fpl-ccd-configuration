package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testEmail;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testTelephone;

class ApplicantPartyTest extends AbstractValidationTest {

    @Test
    void shouldFailValidation() {
        final ApplicantParty applicantParty = ApplicantParty.builder().build();

        assertThat(validate(applicantParty)).containsExactlyInAnyOrder(
            "Enter the applicant's full name",
            "Enter a PBA number for the contact",
            "Enter the contact's full name",
            "Enter a valid address for the contact",
            "Enter at least one telephone number for the contact",
            "Enter a job title for the contact",
            "Enter an email address for the contact"
        );
    }

    @Test
    void shouldPassValidation() {
        final ApplicantParty applicantParty = ApplicantParty.builder()
            .organisationName("LA 1")
            .firstName("John")
            .lastName("Smith")
            .pbaNumber("pba")
            .jobTitle("Solicitor")
            .email(testEmail())
            .telephoneNumber(testTelephone())
            .address(testAddress())
            .build();

        assertThat(validate(applicantParty)).isEmpty();
    }
}
