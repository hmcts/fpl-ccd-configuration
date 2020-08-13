package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class OrganisationDetailsCheckerIsStartedTest {

    @InjectMocks
    private OrganisationDetailsChecker organisationDetailsChecker;

    @ParameterizedTest
    @MethodSource("emptyOrganisation")
    void shouldReturnFalseWhenEmptyOrganisationDetails(Applicant applicant, Solicitor solicitor) {
        final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(applicant))
                .solicitor(solicitor)
                .build();

        final boolean isStarted = organisationDetailsChecker.isStarted(caseData);

        assertThat(isStarted).isFalse();
    }

    @ParameterizedTest
    @MethodSource("updatedApplicant")
    void shouldReturnTrueWhenApplicantDetailsProvided(ApplicantParty applicantParty) {
        final Applicant applicant = Applicant.builder()
                .party(applicantParty)
                .build();
        final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(applicant))
                .build();

        final boolean isStarted = organisationDetailsChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    @ParameterizedTest
    @MethodSource("updatedSolicitor")
    void shouldReturnTrueWhenSolicitorDetailsProvided(Solicitor solicitor) {
        final CaseData caseData = CaseData.builder()
                .solicitor(solicitor)
                .build();

        final boolean isStarted = organisationDetailsChecker.isStarted(caseData);

        assertThat(isStarted).isTrue();
    }

    private static Stream<Arguments> updatedApplicant() {
        return Stream.of(
                ApplicantParty.builder().organisationName("Test").build(),
                ApplicantParty.builder().pbaNumber("Test").build(),
                ApplicantParty.builder().clientCode("Test").build(),
                ApplicantParty.builder().customerReference("Test").build(),
                ApplicantParty.builder().jobTitle("Test").build(),
                ApplicantParty.builder().address(Address.builder().addressLine1("Test").build()).build(),
                ApplicantParty.builder().address(Address.builder().addressLine2("Test").build()).build(),
                ApplicantParty.builder().address(Address.builder().addressLine3("Test").build()).build(),
                ApplicantParty.builder().address(Address.builder().postTown("Test").build()).build(),
                ApplicantParty.builder().address(Address.builder().county("Test").build()).build(),
                ApplicantParty.builder().address(Address.builder().country("Test").build()).build(),
                ApplicantParty.builder().address(Address.builder().postcode("Test").build()).build(),
                ApplicantParty.builder()
                        .telephoneNumber(Telephone.builder().telephoneUsageType("Test").build()).build(),
                ApplicantParty.builder()
                        .telephoneNumber(Telephone.builder().telephoneNumber("Test").build()).build(),
                ApplicantParty.builder()
                        .telephoneNumber(Telephone.builder().contactDirection("Test").build()).build(),
                ApplicantParty.builder()
                        .mobileNumber(Telephone.builder().telephoneUsageType("Test").build()).build(),
                ApplicantParty.builder()
                        .mobileNumber(Telephone.builder().telephoneNumber("Test").build()).build(),
                ApplicantParty.builder()
                        .mobileNumber(Telephone.builder().contactDirection("Test").build()).build(),
                ApplicantParty.builder()
                        .email(EmailAddress.builder().email("Test@test.com").build()).build(),
                ApplicantParty.builder()
                        .email(EmailAddress.builder().emailUsageType("Test").build()).build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> updatedSolicitor() {
        return Stream.of(
                Solicitor.builder().name("Test").build(),
                Solicitor.builder().mobile("Test").build(),
                Solicitor.builder().telephone("Test").build(),
                Solicitor.builder().email("Test").build(),
                Solicitor.builder().dx("Test").build(),
                Solicitor.builder().reference("Test").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyOrganisation() {
        return Stream.of(
                Arguments.of(
                        Applicant.builder().build(),
                        Solicitor.builder().build()),
                Arguments.of(
                        Applicant.builder().party(ApplicantParty.builder().build()).build(),
                        Solicitor.builder().build()),
                Arguments.of(
                        Applicant.builder()
                                .party(ApplicantParty.builder()
                                        .organisationName("")
                                        .pbaNumber("")
                                        .clientCode(null)
                                        .customerReference("")
                                        .jobTitle("")
                                        .address(Address.builder()
                                                .addressLine1("")
                                                .addressLine2("")
                                                .addressLine3("")
                                                .county("")
                                                .country("")
                                                .postTown("")
                                                .postcode("")
                                                .build())
                                        .email(EmailAddress.builder()
                                                .email("")
                                                .emailUsageType("")
                                                .build())
                                        .telephoneNumber(Telephone.builder()
                                                .telephoneUsageType("")
                                                .contactDirection("")
                                                .telephoneNumber("")
                                                .build())
                                        .mobileNumber(Telephone.builder()
                                                .telephoneUsageType("")
                                                .contactDirection("")
                                                .telephoneNumber("")
                                                .build())
                                        .build())
                                .build(),
                        Solicitor.builder()
                                .name("")
                                .mobile("")
                                .telephone("")
                                .email("")
                                .dx("")
                                .reference("")
                                .build()));
    }
}
