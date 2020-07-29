package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ApplicantValidator.class, LocalValidatorFactoryBean.class})
class ApplicantValidatorTest {

    @Autowired
    private ApplicantValidator applicantValidator;

    @Test
    void shouldReturnErrorWhenNoRespondentsSpecified() {

        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = applicantValidator.validate(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "You need to add details to applicant",
            "You need to add details to solicitor"
        );
    }

    @Test
    void shouldReturnErrorsWhenNoApplicantDetailsSpecified() {

        final Applicant applicant = Applicant.builder()
            .party(ApplicantParty.builder().build())
            .build();

        final Solicitor solicitor = Solicitor.builder()
            .build();

        final CaseData caseData = CaseData.builder()
            .applicants(ElementUtils.wrapElements(applicant))
            .solicitor(solicitor)
            .build();

        final List<String> errors = applicantValidator.validate(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Enter the applicant's full name",
            "Enter a PBA number for the contact",
            "Enter at least one telephone number for the contact",
            "Enter a valid address for the contact",
            "Enter an email address for the contact",
            "Enter the contact's full name",
            "Enter a job title for the contact",
            "Enter the solicitor's full name",
            "Enter the solicitor's email"
        );
    }

    @Test
    void shouldReturnEmptyErrorsWhenRequiredApplicantDetailsArePresentAndValid() {

        final Applicant applicant = Applicant.builder()
            .party(ApplicantParty.builder()
                .organisationName("Solicitors")
                .pbaNumber("PBA1234567")
                .telephoneNumber(Telephone.builder()
                    .telephoneNumber("07888288288")
                    .contactDirection("Ask for James")
                    .build())
                .address(Address.builder()
                    .addressLine1("Line 1")
                    .postcode("CR0 2GE")
                    .build())
                .email(EmailAddress.builder().email("test@test.com").build())
                .jobTitle("Solicitor")
                .build())
            .build();

        final Solicitor solicitor = Solicitor.builder()
            .email("test@test.com")
            .name("John Smith")
            .build();

        final CaseData caseData = CaseData.builder()
            .applicants(ElementUtils.wrapElements(applicant))
            .solicitor(solicitor)
            .build();

        final List<String> errors = applicantValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
