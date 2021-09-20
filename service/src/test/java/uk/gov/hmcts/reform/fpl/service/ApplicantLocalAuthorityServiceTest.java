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
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityIdLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.OrganisationNotFound;
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

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
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

    @Mock
    private LocalAuthorityIdLookupConfiguration localAuthorityIds;

    @Mock
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmails;

    @InjectMocks
    private ApplicantLocalAuthorityService underTest;

    @Nested
    class UserLocalAuthority {

        @Test
        void shouldGetLocalAuthorityFromCaseIfExists() {

            final Organisation userOrganisation = Organisation.builder()
                .name("Organisation 1")
                .organisationIdentifier("ORG1")
                .build();

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id("ORG0")
                .name("Organisation 0")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(userOrganisation.getOrganisationIdentifier())
                .name(userOrganisation.getName())
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .applicants(wrapElements(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .organisationName("Legacy org")
                        .build())
                    .build()))
                .solicitor(Solicitor.builder()
                    .email("solicitor@legacy.com")
                    .build())
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.of(userOrganisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(localAuthority2);
        }

        @Test
        void shouldGetLocalAuthorityFromLegacyApplicantWhenNoExistingLocalAuthorityAndUserBelongsToDesignatedOrg() {

            final Organisation userOrganisation = Organisation.builder()
                .name("Organisation 1")
                .organisationIdentifier("ORG1")
                .build();

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

            final OrganisationPolicy designatedOrg = organisationPolicy(
                userOrganisation.getOrganisationIdentifier(),
                userOrganisation.getName(),
                LASOLICITOR);

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(userOrganisation.getOrganisationIdentifier())
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

            when(organisationService.findOrganisation()).thenReturn(Optional.of(userOrganisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetLocalAuthorityFromLegacyApplicantWithoutSolicitor() {

            final Organisation userOrganisation = Organisation.builder()
                .name("Organisation 1")
                .organisationIdentifier("ORG1")
                .build();

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

            final OrganisationPolicy designatedOrg = organisationPolicy(
                userOrganisation.getOrganisationIdentifier(),
                userOrganisation.getName(),
                LASOLICITOR);

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(userOrganisation.getOrganisationIdentifier())
                .name(legacyApplicant.getOrganisationName())
                .address(legacyApplicant.getAddress())
                .email(legacyApplicant.getEmail().getEmail())
                .phone(legacyApplicant.getTelephoneNumber().getTelephoneNumber())
                .pbaNumber(legacyApplicant.getPbaNumber())
                .customerReference(legacyApplicant.getCustomerReference())
                .clientCode(legacyApplicant.getClientCode())
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.of(userOrganisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetLocalAuthorityFromLegacyApplicantAndUseMobileNumbersWhenMainNumberNotPresent() {

            final Organisation userOrganisation = Organisation.builder()
                .name("Organisation 1")
                .organisationIdentifier("ORG1")
                .build();

            final ApplicantParty legacyApplicant = ApplicantParty.builder()
                .mobileNumber(Telephone.builder()
                    .telephoneNumber("0888888888")
                    .build())
                .build();

            final Solicitor legacySolicitor = Solicitor.builder()
                .mobile("0111111111")
                .build();

            final OrganisationPolicy designatedOrg = organisationPolicy(
                userOrganisation.getOrganisationIdentifier(),
                userOrganisation.getName(),
                LASOLICITOR);

            final CaseData caseData = CaseData.builder()
                .applicants(wrapElements(Applicant.builder().party(legacyApplicant).build()))
                .solicitor(legacySolicitor)
                .localAuthorityPolicy(designatedOrg)
                .build();

            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .id(userOrganisation.getOrganisationIdentifier())
                .phone(legacyApplicant.getMobileNumber().getTelephoneNumber())
                .colleagues(ElementUtils.wrapElements(Colleague.builder()
                    .role(SOLICITOR)
                    .phone(legacySolicitor.getMobile())
                    .notificationRecipient("Yes")
                    .mainContact("Yes")
                    .build()))
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.of(userOrganisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetLocalAuthorityFromReferenceData() {

            final CaseData caseData = CaseData.builder()
                .build();

            final Organisation userOrganisation = Organisation.builder()
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

            when(localAuthorityIds.getLocalAuthorityCode(userOrganisation.getOrganisationIdentifier()))
                .thenReturn(Optional.of("LA1"));

            when(localAuthorityEmails.getSharedInbox("LA1"))
                .thenReturn(Optional.of("la@shared.com"));


            final LocalAuthority expectedLocalAuthority = LocalAuthority.builder()
                .name("Organisation 1")
                .id("ORG1")
                .email("la@shared.com")
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

            when(organisationService.findOrganisation()).thenReturn(Optional.of(userOrganisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldGetLocalAuthorityWithPartialAddressFromReferenceData() {
            final CaseData caseData = CaseData.builder()
                .build();

            final Organisation userOrganisation = Organisation.builder()
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

            when(organisationService.findOrganisation()).thenReturn(Optional.of(userOrganisation));

            final LocalAuthority actualLocalAuthority = underTest.getUserLocalAuthority(caseData);

            assertThat(actualLocalAuthority).isEqualTo(expectedLocalAuthority);
        }

        @Test
        void shouldThrowExceptionWhenNoUserOrganisation() {
            final CaseData caseData = CaseData.builder()
                .build();

            when(organisationService.findOrganisation()).thenReturn(Optional.empty());


            assertThatThrownBy(() -> underTest.getUserLocalAuthority(caseData))
                .isInstanceOf(OrganisationNotFound.class);
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
            final Element<Colleague> colleague1 = element(randomUUID(), Colleague.builder()
                .fullName("1")
                .mainContact("Yes")
                .build());
            final Element<Colleague> colleague2 = element(randomUUID(), Colleague.builder()
                .fullName("2")
                .mainContact("No")
                .build());
            final Element<Colleague> colleague3 = element(randomUUID(), Colleague.builder()
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
            final Element<Colleague> colleague1 = element(randomUUID(), Colleague.builder()
                .fullName("1")
                .build());
            final Element<Colleague> colleague2 = element(randomUUID(), Colleague.builder()
                .fullName("2")
                .build());
            final Element<Colleague> colleague3 = element(randomUUID(), Colleague.builder()
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
            final Element<Colleague> colleague1 = element(randomUUID(), Colleague.builder()
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
            final Element<Colleague> colleague1 = element(randomUUID(), Colleague.builder()
                .fullName("1")
                .build());
            final Element<Colleague> colleague2 = element(randomUUID(), Colleague.builder()
                .fullName("2")
                .build());
            final Element<Colleague> colleague3 = element(randomUUID(), Colleague.builder()
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
            final Element<Colleague> colleague1 = element(randomUUID(), Colleague.builder()
                .fullName("1")
                .build());
            final Element<Colleague> colleague2 = element(randomUUID(), Colleague.builder()
                .fullName("2")
                .mainContact("Yes")
                .build());
            final Element<Colleague> colleague3 = element(randomUUID(), Colleague.builder()
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
                .designated("Yes")
                .colleagues(wrapElements(colleague1))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .designated("No")
                .colleagues(wrapElements(colleague2))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            final List<String> actualEmails = underTest.getDesignatedLocalAuthorityContactsEmails(caseData);

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
                .designated("Yes")
                .colleagues(wrapElements(colleague1, colleague2, colleague3))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority))
                .build();

            final List<String> actualEmails = underTest.getDesignatedLocalAuthorityContactsEmails(caseData);

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

            final List<String> actualEmails = underTest.getDesignatedLocalAuthorityContactsEmails(caseData);

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

            final List<String> actualEmails = underTest.getDesignatedLocalAuthorityContactsEmails(caseData);

            assertThat(actualEmails).isEmpty();
        }

        @Test
        void shouldGetEmptyListWhenNoLegacySolicitorAndNoLocalAuthoritiesPresent() {
            final CaseData caseData = CaseData.builder()
                .build();

            final List<String> actualEmails = underTest.getDesignatedLocalAuthorityContactsEmails(caseData);

            assertThat(actualEmails).isEmpty();
        }
    }

    @Nested
    class Save {

        @Test
        void shouldAddNewLocalAuthorityWhenNoLocalAuthoritiesPresent() {
            final LocalAuthority localAuthority = LocalAuthority.builder()
                .id("ORG1")
                .name("LA")
                .email("la@test.com")
                .build();

            final List<Element<Colleague>> colleagues = wrapElements(
                Colleague.builder()
                    .fullName("John Smith")
                    .build());

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthority(localAuthority)
                .localAuthorityColleagues(colleagues)
                .build();

            final OrganisationPolicy organisationPolicy = organisationPolicy(localAuthority.getId(), "LA", LASOLICITOR);

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .build();

            final List<Element<LocalAuthority>> localAuthorities = underTest.save(caseData, eventData);

            final LocalAuthority expectedLocalAuthority = localAuthority.toBuilder()
                .designated("Yes")
                .colleagues(colleagues)
                .build();

            assertThat(localAuthorities)
                .extracting(Element::getValue)
                .containsExactly(expectedLocalAuthority);
        }

        @Test
        void shouldAddNewLocalAuthorityWhenNoLocalAuthorityForCurrentUserPresent() {
            final LocalAuthority newLocalAuthority = LocalAuthority.builder()
                .id("ORGNEW")
                .name("LA NEW")
                .build();

            final LocalAuthority existingLocalAuthority1 = LocalAuthority.builder()
                .id("ORG1")
                .name("LA 2")
                .build();

            final LocalAuthority existingLocalAuthority2 = LocalAuthority.builder()
                .id("ORG2")
                .name("LA 2")
                .build();

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthority(newLocalAuthority)
                .build();

            final OrganisationPolicy organisationPolicy = organisationPolicy(existingLocalAuthority1.getId(), "LA",
                LASOLICITOR);

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(existingLocalAuthority1, existingLocalAuthority2))
                .localAuthorityPolicy(organisationPolicy)
                .build();

            final List<Element<LocalAuthority>> localAuthorities = underTest.save(caseData, eventData);

            assertThat(localAuthorities)
                .extracting(Element::getValue)
                .containsExactly(existingLocalAuthority1, existingLocalAuthority2, newLocalAuthority);

            assertThat(existingLocalAuthority1.getDesignated()).isEqualTo("Yes");
            assertThat(existingLocalAuthority2.getDesignated()).isEqualTo("No");
            assertThat(newLocalAuthority.getDesignated()).isEqualTo("No");
        }

        @Test
        void shouldUpdateExistingLocalAuthority() {
            final Element<LocalAuthority> existingLocalAuthority1 = element(randomUUID(), LocalAuthority.builder()
                .id("ORG1")
                .name("LA old 1")
                .email("old1@test.com")
                .colleagues(wrapElements(Colleague.builder()
                    .fullName("Enrique Green")
                    .build()))
                .build());

            final Element<LocalAuthority> existingLocalAuthority2 = element(randomUUID(), LocalAuthority.builder()
                .id("ORG2")
                .name("LA old 2")
                .email("old2@test.com")
                .build());

            final LocalAuthority updatedLocalAuthority = LocalAuthority.builder()
                .id("ORG2")
                .name("LA new")
                .email("new@test.com")
                .build();

            final List<Element<Colleague>> updatedColleagues = wrapElements(
                Colleague.builder()
                    .fullName("John Smith")
                    .build());

            final LocalAuthorityEventData eventData = LocalAuthorityEventData.builder()
                .localAuthority(updatedLocalAuthority)
                .localAuthorityColleagues(updatedColleagues)
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(List.of(existingLocalAuthority1, existingLocalAuthority2))
                .build();

            final List<Element<LocalAuthority>> actualLocalAuthorities = underTest.save(caseData, eventData);

            assertThat(actualLocalAuthorities).containsExactly(
                existingLocalAuthority1,
                element(existingLocalAuthority2.getId(), updatedLocalAuthority));

        }
    }

    @Nested
    class UpdateDesignatedLocalAuthority {

        @Test
        void shouldMarkLocalAuthorityAsDesignated() {

            final OrganisationPolicy organisationPolicy = organisationPolicy(randomAlphanumeric(3), "ORG", LASHARED);

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(organisationPolicy.getOrganisation().getOrganisationID())
                .build();

            final LocalAuthority localAuthority3 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2, localAuthority3))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("Yes");
            assertThat(localAuthority3.getDesignated()).isEqualTo("No");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyListWhenNoLocalAuthorities(List<Element<LocalAuthority>> localAuthorities) {

            final CaseData caseData = CaseData.builder()
                .localAuthorities(localAuthorities)
                .build();

            assertThat(underTest.updateDesignatedLocalAuthority(caseData)).isEmpty();
        }

        @Test
        void shouldMarkAllAsNotDesignatedWhenNoneMatchLocalAuthorityPolicy() {

            final OrganisationPolicy organisationPolicy = organisationPolicy(randomAlphanumeric(3), "ORG", LASHARED);

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("No");
        }

        @Test
        void shouldMarkAllAsNotDesignatedWhenNoLocalAuthorityPolicy() {

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("No");
        }

        @Test
        void shouldChangeDesignatedLocalAuthority() {

            final OrganisationPolicy organisationPolicy = organisationPolicy(randomAlphanumeric(3), "ORG", LASHARED);

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .designated("Yes")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(organisationPolicy.getOrganisation().getOrganisationID())
                .designated("No")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("Yes");
        }
    }

    @Nested
    class DesignatedLocalAuthority {

        @Test
        void shouldMarkLocalAuthorityAsDesignated() {

            final OrganisationPolicy organisationPolicy = organisationPolicy(randomAlphanumeric(3), "ORG", LASHARED);

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(organisationPolicy.getOrganisation().getOrganisationID())
                .build();

            final LocalAuthority localAuthority3 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2, localAuthority3))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("Yes");
            assertThat(localAuthority3.getDesignated()).isEqualTo("No");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyListWhenNoLocalAuthorities(List<Element<LocalAuthority>> localAuthorities) {

            final CaseData caseData = CaseData.builder()
                .localAuthorities(localAuthorities)
                .build();

            assertThat(underTest.updateDesignatedLocalAuthority(caseData)).isEmpty();
        }

        @Test
        void shouldMarkAllAsNotDesignatedWhenNoneMatchLocalAuthorityPolicy() {

            final OrganisationPolicy organisationPolicy = organisationPolicy(randomAlphanumeric(3), "ORG", LASHARED);

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("No");
        }

        @Test
        void shouldMarkAllAsNotDesignatedWhenNoLocalAuthorityPolicy() {

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("No");
        }

        @Test
        void shouldChangeDesignatedLocalAuthority() {

            final OrganisationPolicy organisationPolicy = organisationPolicy(randomAlphanumeric(3), "ORG", LASHARED);

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .id(randomAlphanumeric(5))
                .designated("Yes")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .id(organisationPolicy.getOrganisation().getOrganisationID())
                .designated("No")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorityPolicy(organisationPolicy)
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            underTest.updateDesignatedLocalAuthority(caseData);

            assertThat(localAuthority1.getDesignated()).isEqualTo("No");
            assertThat(localAuthority2.getDesignated()).isEqualTo("Yes");
        }
    }
}
