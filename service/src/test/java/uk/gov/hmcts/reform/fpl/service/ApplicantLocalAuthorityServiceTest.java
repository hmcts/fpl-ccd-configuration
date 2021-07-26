package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.LocalAuthorityEventData;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicantLocalAuthorityServiceTest {

    @Mock
    private PbaNumberService pbaNumberService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ValidateEmailService validateEmailService;

    @InjectMocks
    private ApplicantLocalAuthorityService underTest;

    @Nested
    class UserLocalAuthority {

        @Test
        void shouldGetLocalAuthorityFromCaseIfExists() {
            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .name("ORG1")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(expectedLocalAuthority))
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Legacy org")
                        .build())
                    .build()))
                .solicitor(Solicitor.builder()
                    .email("solicitor@legacy.com")
                    .build())
                .build();

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetLocalAuthorityFromLegacyApplicantWhenNoExistingLocalAuthority() {

            final ApplicantParty legacyApplicant = ApplicantParty.builder()
                .organisationName("Applicant org")
                .email(EmailAddress.builder()
                    .email("applicant@legacy.com")
                    .build())
                .telephoneNumber(Telephone.builder()
                    .telephoneNumber("0777777777")
                    .build())
                .mobileNumber(Telephone.builder()
                    .telephoneNumber("0888888888")
                    .build())
                .address(Address.builder()
                    .addressLine1("Applicant office")
                    .postcode("AP 999")
                    .build())
                .pbaNumber("PBA7654321")
                .customerReference("APPLICANT_REF")
                .clientCode("APPLICANT_CODE")
                .build();

            final Solicitor legacySolicitor = Solicitor.builder()
                .name("Applicant solicitor")
                .mobile("0111111111")
                .telephone("0222222222")
                .dx("SOLICITOR_DX")
                .reference("SOLICITOR_REFERENCE")
                .email("solicitor@legacy.com")
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .name(legacyApplicant.getOrganisationName())
                .address(legacyApplicant.getAddress())
                .email(legacyApplicant.getEmail().getEmail())
                .phone(legacyApplicant.getTelephoneNumber().getTelephoneNumber())
                .pbaNumber(legacyApplicant.getPbaNumber())
                .customerReference(legacyApplicant.getCustomerReference())
                .clientCode(legacyApplicant.getClientCode())
                .colleagues(ElementUtils.wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .email(legacySolicitor.getEmail())
                    .dx(legacySolicitor.getDx())
                    .reference(legacySolicitor.getReference())
                    .fullName(legacySolicitor.getName())
                    .phone(legacySolicitor.getTelephone())
                    .notificationRecipient("Yes")
                    .mainContact("Yes")
                    .build()))
                .build();

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);

            verifyNoInteractions(organisationService);
        }

        @Test
        void shouldGetLocalAuthorityFromLegacyApplicantWithoutSolicitor() {

            final ApplicantParty legacyApplicant = ApplicantParty.builder()
                .organisationName("Applicant org")
                .email(EmailAddress.builder()
                    .email("applicant@legacy.com")
                    .build())
                .telephoneNumber(Telephone.builder()
                    .telephoneNumber("0777777777")
                    .build())
                .mobileNumber(Telephone.builder()
                    .telephoneNumber("0888888888")
                    .build())
                .address(Address.builder()
                    .addressLine1("Applicant office")
                    .postcode("AP 999")
                    .build())
                .pbaNumber("PBA7654321")
                .customerReference("APPLICANT_REF")
                .clientCode("APPLICANT_CODE")
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .name(legacyApplicant.getOrganisationName())
                .address(legacyApplicant.getAddress())
                .email(legacyApplicant.getEmail().getEmail())
                .phone(legacyApplicant.getTelephoneNumber().getTelephoneNumber())
                .pbaNumber(legacyApplicant.getPbaNumber())
                .customerReference(legacyApplicant.getCustomerReference())
                .clientCode(legacyApplicant.getClientCode())
                .build();

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);

            verifyNoInteractions(organisationService);
        }

        @Test
        void shouldGetLocalAuthorityFromLegacyApplicantAndUseMobileNumbersWhenMainNumberNotPresent() {

            final ApplicantParty legacyApplicant = ApplicantParty.builder()
                .mobileNumber(Telephone.builder()
                    .telephoneNumber("0888888888")
                    .build())
                .build();

            final Solicitor legacySolicitor = Solicitor.builder()
                .mobile("0111111111")
                .build();

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .phone(legacyApplicant.getMobileNumber().getTelephoneNumber())
                .colleagues(ElementUtils.wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .phone(legacySolicitor.getMobile())
                    .notificationRecipient("Yes")
                    .mainContact("Yes")
                    .build()))
                .build();

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);

            verifyNoInteractions(organisationService);
        }

        @Test
        void shouldGetLocalAuthorityFromReferenceData() {

            final CaseData caseData = CaseData.builder()
                .build();

            final Organisation organisation = Organisation.builder()
                .name("Organisation 1")
                .organisationIdentifier("ORG1")
                .contactInformation(List.of(ContactInformation.builder()
                    .addressLine1("L1")
                    .addressLine2("L2")
                    .addressLine3("L3")
                    .country("Country")
                    .county("County")
                    .postCode("AB 100")
                    .townCity("City")
                    .build()))
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .name("Organisation 1")
                .id("ORG1")
                .address(Address.builder()
                    .addressLine1("L1")
                    .addressLine2("L2")
                    .addressLine3("L3")
                    .postcode("AB 100")
                    .country("Country")
                    .county("County")
                    .postTown("City")
                    .build())
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.of(organisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetLocalAuthorityWithPartialAddressFromReferenceData() {
            final CaseData caseData = CaseData.builder()
                .build();

            final Organisation organisation = Organisation.builder()
                .name("Organisation 1")
                .organisationIdentifier("ORG1")
                .contactInformation(List.of(ContactInformation.builder()
                    .addressLine1("L1")
                    .postCode("AB 100")
                    .build()))
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .name("Organisation 1")
                .id("ORG1")
                .address(Address.builder()
                    .addressLine1("L1")
                    .postcode("AB 100")
                    .build())
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.of(organisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetEmptyLocalAuthorityWhenOrganisationNotFound() {
            final CaseData caseData = CaseData.builder()
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.empty());

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .address(Address.builder()
                    .build())
                .build();

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }
    }

    @Nested
    class PbaNumber {

        @Test
        void shouldNormalisePbaNumber() {
            final LocalAuthority localAuthority = LocalAuthority.builder()
                .pbaNumber("1234567")
                .build();

            when(pbaNumberService.update("1234567")).thenReturn("PBA1234567");

            underTest.normalisePba(localAuthority);

            assertThat(localAuthority.getPbaNumber()).isEqualTo("PBA1234567");
        }
    }

    @Nested
    class LocalAuthorityValidation {

        final LocalAuthority localAuthority = LocalAuthority.builder()
            .pbaNumber("pba")
            .email("email")
            .build();

        @Test
        void shouldReturnValidationErrorsForLocalAuthority() {
            when(pbaNumberService.validate("pba"))
                .thenReturn(List.of("PBA error 1", "PBA error 2"));

            when(validateEmailService.validateIfPresent("email"))
                .thenReturn(List.of("Email error 1", "Email error 2"));

            final List<String> actualErrors = underTest.validateLocalAuthority(localAuthority);

            assertThat(actualErrors).containsExactlyInAnyOrder(
                "PBA error 1",
                "PBA error 2",
                "Email error 1",
                "Email error 2");

            verify(pbaNumberService).validate("pba");
            verify(validateEmailService).validateIfPresent("email");
        }

        @Test
        void shouldReturnEmptyValidationErrorsForLocalAuthority() {
            when(pbaNumberService.validate("pba")).thenReturn(emptyList());
            when(validateEmailService.validateIfPresent("email")).thenReturn(emptyList());

            final List<String> actualErrors = underTest.validateLocalAuthority(localAuthority);

            assertThat(actualErrors).isEmpty();

            verify(pbaNumberService).validate("pba");
            verify(validateEmailService).validateIfPresent("email");
        }
    }

    @Nested
    class ValidateColleagues {

        final Colleague colleague1 = Colleague.builder()
            .email("email1@test.com")
            .build();

        final Colleague colleague2 = Colleague.builder()
            .email("email2@test.com")
            .build();

        final List<Element<Colleague>> colleagues = wrapElements(colleague1, colleague2);

        @Test
        void shouldReturnValidationErrors() {
            when(validateEmailService.validate(List.of("email1@test.com", "email2@test.com"), "Colleague"))
                .thenReturn(List.of("Error 1", "Error 2"));

            final List<String> errors = underTest.validateColleagues(colleagues);

            assertThat(errors).containsExactlyInAnyOrder("Error 1", "Error 2");

            verify(validateEmailService).validate(List.of("email1@test.com", "email2@test.com"), "Colleague");
        }

        @Test
        void shouldReturnEmptyValidationErrorsForLocalAuthority() {
            when(validateEmailService.validate(List.of("email1@test.com", "email2@test.com"), "Colleague"))
                .thenReturn(emptyList());

            final List<String> actualErrors = underTest.validateColleagues(colleagues);

            assertThat(actualErrors).isEmpty();

            verify(validateEmailService).validate(List.of("email1@test.com", "email2@test.com"), "Colleague");
        }
    }

    @Nested
    class MainContact {

        @Test
        void shouldSetMainContactFromUserSelectionWhenMultipleContacts() {
            final Element<Colleague> colleague1 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("1")
                .mainContact("Yes")
                .build());
            final Element<Colleague> colleague2 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("2")
                .mainContact("No")
                .build());
            final Element<Colleague> colleague3 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("3")
                .mainContact("No")
                .build());

            final DynamicList expectedDynamicList = buildDynamicList(1,
                Pair.of(colleague1.getId(), "1"),
                Pair.of(colleague2.getId(), "2"),
                Pair.of(colleague3.getId(), "3"));

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthorityColleagues(List.of(colleague1, colleague2, colleague3))
                .localAuthorityColleaguesList(expectedDynamicList)
                .build();

            final List<Element<Colleague>> colleagues = underTest.updateMainContact(eventData);

            assertThat(colleagues).containsExactly(
                element(colleague1.getId(), Colleague.builder().fullName("1").mainContact("No").build()),
                element(colleague2.getId(), Colleague.builder().fullName("2").mainContact("Yes").build()),
                element(colleague3.getId(), Colleague.builder().fullName("3").mainContact("No").build()));
        }

        @Test
        void shouldNotSetMainContactWhenMultipleColleaguesAndUsedDidNotSelectAny() {
            final Element<Colleague> colleague1 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("1")
                .build());
            final Element<Colleague> colleague2 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("2")
                .build());
            final Element<Colleague> colleague3 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("3")
                .build());

            final DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(colleague1.getId(), "1"),
                Pair.of(colleague2.getId(), "2"),
                Pair.of(colleague3.getId(), "3"));

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthorityColleagues(List.of(colleague1, colleague2, colleague3))
                .localAuthorityColleaguesList(expectedDynamicList)
                .build();

            final List<Element<Colleague>> colleagues = underTest.updateMainContact(eventData);

            assertThat(colleagues).containsExactly(
                element(colleague1.getId(), Colleague.builder().fullName("1").mainContact("No").build()),
                element(colleague2.getId(), Colleague.builder().fullName("2").mainContact("No").build()),
                element(colleague3.getId(), Colleague.builder().fullName("3").mainContact("No").build()));
        }

        @Test
        void shouldSetSingleColleagueAsMainContact() {
            final Element<Colleague> colleague1 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("1")
                .build());

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthorityColleagues(List.of(colleague1))
                .build();

            final List<Element<Colleague>> colleagues = underTest.updateMainContact(eventData);

            assertThat(colleagues).containsExactly(
                element(colleague1.getId(), Colleague.builder().fullName("1").mainContact("Yes").build()));
        }

        @Test
        void shouldDoNothingWhenNoContactsAvailable() {

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthorityColleagues(emptyList())
                .build();

            final List<Element<Colleague>> colleagues = underTest.updateMainContact(eventData);

            assertThat(colleagues).isEmpty();
        }
    }

    @Nested
    class ContactList {

        @Test
        void shouldBuildListOfContactsFromMultipleColleagues() {
            final Element<Colleague> colleague1 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("1")
                .build());
            final Element<Colleague> colleague2 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("2")
                .build());
            final Element<Colleague> colleague3 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("3")
                .build());

            final DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(colleague1.getId(), "1"),
                Pair.of(colleague2.getId(), "2"),
                Pair.of(colleague3.getId(), "3"));

            DynamicList actualContactList = underTest.buildContactsList(List.of(colleague1, colleague2, colleague3));

            assertThat(actualContactList).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildListOfContactsFromMultipleColleaguesWithMainContact() {
            final Element<Colleague> colleague1 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("1")
                .build());
            final Element<Colleague> colleague2 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("2")
                .mainContact("Yes")
                .build());
            final Element<Colleague> colleague3 = element(UUID.randomUUID(), Colleague.builder()
                .fullName("3")
                .build());

            final DynamicList expectedDynamicList = buildDynamicList(1,
                Pair.of(colleague1.getId(), "1"),
                Pair.of(colleague2.getId(), "2"),
                Pair.of(colleague3.getId(), "3"));

            DynamicList actualContactList = underTest.buildContactsList(List.of(colleague1, colleague2, colleague3));

            assertThat(actualContactList).isEqualTo(expectedDynamicList);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldBuildEmptyListOfContact(List<Element<Colleague>> colleagues) {

            final DynamicList expectedDynamicList = buildDynamicList();

            DynamicList actualContactList = underTest.buildContactsList(colleagues);

            assertThat(actualContactList).isEqualTo(expectedDynamicList);
        }
    }

    @Nested
    class ContactEmails {

        @Test
        void shouldGetContactEmailsFromMultipleLocalAuthorities() {
            final Colleague colleague1 = Colleague.builder()
                .email("email1@test.com")
                .notificationRecipient("Yes")
                .build();
            final Colleague colleague2 = Colleague.builder()
                .email("email2@test.com")
                .notificationRecipient("No")
                .build();
            final Colleague colleague3 = Colleague.builder()
                .email("email3@test.com")
                .notificationRecipient("Yes")
                .build();
            final Colleague colleague4 = Colleague.builder()
                .email("email4@test.com")
                .notificationRecipient("Yes")
                .build();

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1, colleague2, colleague3))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .colleagues(wrapElements(colleague4))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            final List<String> actualEmails = underTest.getContactsEmails(caseData);

            assertThat(actualEmails).containsExactlyInAnyOrder("email1@test.com", "email3@test.com", "email4@test.com");
        }

        @Test
        void shouldGetEmptyContactEmailsWhenNoColleaguesMarkedAsNotificationRecipient() {
            final Colleague colleague1 = Colleague.builder()
                .email("email1@test.com")
                .notificationRecipient("No")
                .build();

            final Colleague colleague2 = Colleague.builder()
                .email("email2@test.com")
                .notificationRecipient("No")
                .build();

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .colleagues(wrapElements(colleague2))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            final List<String> actualEmails = underTest.getContactsEmails(caseData);

            assertThat(actualEmails).isEmpty();
        }

        @Test
        void shouldGetNonEmptyContactEmailsOnly() {
            final Colleague colleague1 = Colleague.builder()
                .email("email1@test.com")
                .notificationRecipient("Yes")
                .build();

            final Colleague colleague2 = Colleague.builder()
                .email("")
                .notificationRecipient("Yes")
                .build();

            final Colleague colleague3 = Colleague.builder()
                .notificationRecipient("Yes")
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1, colleague2, colleague3))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority))
                .build();

            final List<String> actualEmails = underTest.getContactsEmails(caseData);

            assertThat(actualEmails).containsExactly("email1@test.com");
        }

        @Test
        void shouldGetEmailFromLegacySolicitorWhenNoLocalAuthoritiesPresent() {
            final Solicitor solicitor = Solicitor.builder()
                .email("soliciotr@test.com")
                .build();

            final CaseData caseData = CaseData.builder()
                .solicitor(solicitor)
                .build();

            final List<String> actualEmails = underTest.getContactsEmails(caseData);

            assertThat(actualEmails).containsExactly("soliciotr@test.com");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldGetEmptyListWhenNoLegacySolicitorEmailAndNoLocalAuthoritiesPresent(String solicitorEmail) {
            final Solicitor solicitor = Solicitor.builder()
                .email(solicitorEmail)
                .build();

            final CaseData caseData = CaseData.builder()
                .solicitor(solicitor)
                .build();

            final List<String> actualEmails = underTest.getContactsEmails(caseData);

            assertThat(actualEmails).isEmpty();
        }

        @Test
        void shouldGetEmptyListWhenNoLegacySolicitorAndNoLocalAuthoritiesPresent() {
            final CaseData caseData = CaseData.builder()
                .build();

            final List<String> actualEmails = underTest.getContactsEmails(caseData);

            assertThat(actualEmails).isEmpty();
        }
    }

    @Nested
    class Save {

        @Test
        void shouldAddNewLocalAuthority() {
            final LocalAuthority localAuthority = LocalAuthority.builder()
                .name("LA")
                .email("la@test.com")
                .build();

            final List<Element<Colleague>> colleagues = wrapElements(
                Colleague.builder()
                    .fullName("John Smith")
                    .build(),
                Colleague.builder()
                    .fullName("Alex Brown")
                    .mainContact("Yes")
                    .build());

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthority(localAuthority)
                .localAuthorityColleagues(colleagues)
                .build();

            final CaseData caseData = CaseData.builder().build();

            final List<Element<LocalAuthority>> localAuthorities = underTest.save(caseData, eventData);

            assertThat(localAuthorities)
                .extracting(Element::getValue)
                .containsExactly(localAuthority);

        }

        @Test
        void shouldUpdateExistingLocalAuthority() {
            final UUID localAuthorityId = UUID.randomUUID();
            final Element<LocalAuthority> existingLocalAuthority = element(localAuthorityId, LocalAuthority.builder()
                .name("LA old")
                .email("old@test.com")
                .colleagues(wrapElements(Colleague.builder()
                    .fullName("Enrique Green")
                    .build()))
                .build());

            final LocalAuthority updatedLocalAuthority = LocalAuthority.builder()
                .name("LA new")
                .email("new@test.com")
                .build();

            final List<Element<Colleague>> updatedColleagues = wrapElements(
                Colleague.builder()
                    .fullName("John Smith")
                    .build(),
                Colleague.builder()
                    .fullName("Alex Brown")
                    .mainContact("Yes")
                    .build());

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthority(updatedLocalAuthority)
                .localAuthorityColleagues(updatedColleagues)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(List.of(existingLocalAuthority))
                .build();

            final List<Element<LocalAuthority>> actualLocalAuthorities = underTest.save(caseData, eventData);

            assertThat(actualLocalAuthorities).containsExactly(element(localAuthorityId, updatedLocalAuthority));
        }
    }
}
