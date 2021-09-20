package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.CAFCASS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.LA_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_OTHER_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_PERSON_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOther;

@ExtendWith(SpringExtension.class)
class RepresentativesServiceTest {

    private final String authorisation = RandomStringUtils.randomAlphanumeric(10);

    @Mock
    private CaseService caseService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private RequestData requestData;

    @Mock
    private RepresentativeCaseRoleService representativesCaseRoleService;

    @Mock
    private ValidateEmailService validateEmailService;

    @InjectMocks
    private RepresentativeService representativesService;

    private static final String VALID_EMAIL = "test@test.com";
    private static final String INVALID_EMAIL = "<John Doe> test@test.com";

    @BeforeEach
    private void init() {
        when(requestData.authorisation()).thenReturn(authorisation);
        when(validateEmailService.isValid(VALID_EMAIL)).thenReturn(true);
        when(validateEmailService.isValid(INVALID_EMAIL)).thenReturn(false);
        when(validateEmailService.validate(VALID_EMAIL)).thenReturn(Optional.of(""));
        when(validateEmailService.validate(INVALID_EMAIL))
            .thenReturn(Optional.of("Enter an email address in the correct format, for example name@example.com"));
    }

    @AfterEach
    private void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(organisationService);
        verifyNoMoreInteractions(caseService);
    }

    @Test
    void shouldFailPresenceOfRepresentedPartiesValidation() {
        Representative representative1 = Representative.builder().role(REPRESENTING_RESPONDENT_1).build();
        Representative representative2 = Representative.builder().role(REPRESENTING_PERSON_1).build();
        Representative representative3 = Representative.builder().role(REPRESENTING_OTHER_PERSON_1).build();

        CaseData caseData = caseWithRepresentatives(representative1, representative2, representative3);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).contains(
            "Respondent 1 represented by Representative 1 doesn't exist."
                + " Choose a respondent who is associated with this case",
            "Person represented by Representative 2 doesn't exist."
                + " Choose a person who is associated with this case",
            "Other person 1 represented by Representative 3 doesn't exist."
                + " Choose a person who is associated with this case");
    }

    @Test
    void shouldPassPresenceOfRepresentedPartiesValidation() {
        Representative representative1 = Representative.builder().role(REPRESENTING_RESPONDENT_1).build();
        Representative representative2 = Representative.builder().role(REPRESENTING_PERSON_1).build();
        Representative representative3 = Representative.builder().role(REPRESENTING_OTHER_PERSON_1).build();

        CaseData caseData = caseWithRepresentatives(representative1, representative2, representative3).toBuilder()
            .respondents1(wrapElements(Respondent.builder().build()))
            .others(Others.from(wrapElements(testOther(), testOther())))
            .build();

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly(
            "Enter a full name for Representative 1",
            "Enter a position in the case for Representative 1",
            "Select how Representative 1 wants to get case information",
            "Enter a full name for Representative 2",
            "Enter a position in the case for Representative 2",
            "Select how Representative 2 wants to get case information",
            "Enter a full name for Representative 3",
            "Enter a position in the case for Representative 3",
            "Select how Representative 3 wants to get case information"
        );
    }

    @Test
    void shouldValidateBasicFieldsForSingleRepresentative() {
        Representative representative = Representative.builder().build();
        CaseData caseData = caseWithRepresentatives(representative);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly(
            "Enter a full name for Representative",
            "Enter a position in the case for Representative",
            "Select who Representative is",
            "Select how Representative wants to get case information");
    }

    @Test
    void shouldValidateBasicFieldsForMultipleRepresentatives() {
        Representative representative1 = Representative.builder().build();
        Representative representative2 = Representative.builder().build();
        CaseData caseData = caseWithRepresentatives(representative1, representative2);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly(
            "Enter a full name for Representative 1",
            "Enter a position in the case for Representative 1",
            "Select who Representative 1 is",
            "Select how Representative 1 wants to get case information",
            "Enter a full name for Representative 2",
            "Enter a position in the case for Representative 2",
            "Select who Representative 2 is",
            "Select how Representative 2 wants to get case information");
    }

    @Test
    void shouldValidateEmailPresenceWhenServingPreferenceIsEmail() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Position")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(EMAIL)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly("Enter an email address for Representative");
    }

    @Test
    void shouldValidateEmailWhenServingPreferenceIsEmail() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Position")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(EMAIL)
            .email(INVALID_EMAIL)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);
        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly("Enter an email address in the correct format,"
            + " for example name@example.com for Representative");
    }

    @Test
    void shouldValidateAddressPresenceWhenServingPreferenceIsPost() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Position")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(POST)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly(
            "Enter a postcode for Representative",
            "Enter a valid address for Representative");
    }

    @Test
    void shouldValidateAddressFormatWhenServingPreferenceIsPost() {
        Representative representative1 = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Position")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(POST)
            .address(Address.builder()
                .addressLine1("Flat 1000, Saffron Square,")
                .postcode("CR0 2GE").build())
            .build();

        Representative representative2 = Representative.builder()
            .fullName("Alex White")
            .positionInACase("Position")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(POST)
            .address(Address.builder().build())
            .build();

        CaseData caseData = caseWithRepresentatives(representative1, representative2);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly(
            "Enter a postcode for Representative 2",
            "Enter a valid address for Representative 2");
    }

    @Test
    void shouldValidateEmailPresenceWhenServingPreferenceIsDigitalService() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(DIGITAL_SERVICE)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly("Enter an email address for Representative");
    }

    @Test
    void shouldValidateEmailWhenServingPreferenceIsDigitalService() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(DIGITAL_SERVICE)
            .email(INVALID_EMAIL)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).containsExactly("Enter an email address in the correct format,"
            + " for example name@example.com for Representative");
    }

    @Test
    void shouldValidateAccountExistenceWhenServingPreferenceIsDigitalService() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .email(VALID_EMAIL)
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(DIGITAL_SERVICE)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        when(organisationService.findUserByEmail(any())).thenReturn(Optional.empty());

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors)
            .containsExactly("Representative must already have an account with the digital service");

        verify(organisationService).findUserByEmail(representative.getEmail());
    }

    @Test
    void shouldPassValidationWhenServingPreferenceIsDigitalServiceAndAccountExists() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .email(VALID_EMAIL)
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(EMAIL)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        when(organisationService.findUserByEmail(representative.getEmail()))
            .thenReturn(Optional.of(RandomStringUtils.randomAlphanumeric(10)));

        List<String> validationErrors = representativesService.validateRepresentatives(caseData);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnRepresentativesIfPresent() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .email("test@hmcts.net")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(EMAIL)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        List<Element<Representative>> representatives = representativesService.getDefaultRepresentatives(caseData);

        assertThat(representatives).isEqualTo(caseData.getRepresentatives());
    }

    @Test
    void shouldReturnEmptyRepresentativeIfNoRepresentativePresents() {
        CaseData caseData = CaseData.builder().build();

        List<Element<Representative>> expectedRepresentatives = wrapElements(Representative.builder().build());
        List<Element<Representative>> representatives = representativesService.getDefaultRepresentatives(caseData);

        assertThat(representatives).isEqualTo(expectedRepresentatives);
    }

    @Test
    void shouldAddUserToCase() {
        final Long caseId = RandomUtils.nextLong();

        String representative1UserId = RandomStringUtils.randomAlphabetic(10);
        String representative2UserId = RandomStringUtils.randomAlphabetic(10);

        Other other = Other.builder().build();

        Representative representative1 = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(REPRESENTING_PERSON_1)
            .email("representative1@hmcts.net")
            .build();

        Representative representative2 = Representative.builder()
            .servingPreferences(DIGITAL_SERVICE)
            .role(LA_LEGAL_REPRESENTATIVE)
            .email("representative2@hmcts.net")
            .build();

        Representative representative3 = Representative.builder()
            .servingPreferences(EMAIL)
            .role(CAFCASS_GUARDIAN)
            .build();

        Representative representative4 = Representative.builder()
            .servingPreferences(POST)
            .role(LA_LEGAL_REPRESENTATIVE)
            .build();

        Element<Representative> representative1Element = element(representative1);
        Element<Representative> representative2Element = element(representative2);
        Element<Representative> representative3Element = element(representative3);
        Element<Representative> representative4Element = element(representative4);

        CaseData updatedCaseData = CaseData.builder()
            .respondents1(emptyList())
            .representatives(asList(
                representative1Element,
                representative2Element,
                representative3Element,
                representative4Element))
            .others(Others.builder().firstOther(other).build())
            .build();

        CaseData originalCaseData = CaseData.builder()
            .respondents1(emptyList())
            .representatives(asList(
                representative1Element,
                representative2Element,
                representative3Element))
            .others(Others.builder().firstOther(other).build())
            .build();

        when(representativesCaseRoleService.calculateCaseRoleUpdates(
            unwrapElements(updatedCaseData.getRepresentatives()),
            unwrapElements(originalCaseData.getRepresentatives())
        )).thenReturn(Map.ofEntries(
            entry(representative1.getEmail(), Set.of(SOLICITOR)),
            entry(representative2.getEmail(), Set.of(LASOLICITOR))
        ));

        when(organisationService.findUserByEmail(representative1.getEmail()))
            .thenReturn(ofNullable(representative1UserId));
        when(organisationService.findUserByEmail(representative2.getEmail()))
            .thenReturn(ofNullable(representative2UserId));

        representativesService.updateRepresentatives(caseId, updatedCaseData, originalCaseData);

        verify(organisationService).findUserByEmail(representative1.getEmail());
        verify(organisationService).findUserByEmail(representative2.getEmail());

        verify(caseService).addUser(caseId.toString(), representative1UserId, Set.of(SOLICITOR));
        verify(caseService).addUser(caseId.toString(), representative2UserId, Set.of(LASOLICITOR));
    }

    @Test
    void shouldLinkRepresentativeWithRepresentable() {
        Long caseId = RandomUtils.nextLong();

        Other otherPerson1 = Other.builder().name("first other").build();
        Other otherPerson2 = Other.builder().name("additional other").build();
        Respondent respondent1 = Respondent.builder().build();
        Respondent respondent2 = Respondent.builder().build();

        Element<Representative> person1Representative1 = representativeFor(REPRESENTING_PERSON_1);
        Element<Representative> person1Representative2 = representativeFor(REPRESENTING_PERSON_1);
        Element<Representative> person2Representative = representativeFor(REPRESENTING_OTHER_PERSON_1);
        Element<Representative> responded1Representative1 = representativeFor(REPRESENTING_RESPONDENT_1);
        Element<Representative> responded1Representative2 = representativeFor(REPRESENTING_RESPONDENT_1);
        Element<Representative> responded2Representative = representativeFor(REPRESENTING_RESPONDENT_2);

        CaseData caseData = CaseData.builder()
            .representatives(asList(
                person1Representative1,
                person1Representative2,
                person2Representative,
                responded1Representative1,
                responded1Representative2,
                responded2Representative))
            .others(Others.builder()
                .firstOther(otherPerson1)
                .additionalOthers(wrapElements(otherPerson2))
                .build())
            .respondents1(wrapElements(respondent1, respondent2))
            .build();

        representativesService.updateRepresentatives(caseId, caseData, caseData);

        assertThat(unwrapElements(otherPerson1.getRepresentedBy()))
            .containsExactly(person1Representative1.getId(), person1Representative2.getId());
        assertThat(unwrapElements(otherPerson2.getRepresentedBy()))
            .containsExactly(person2Representative.getId());
        assertThat(unwrapElements(respondent1.getRepresentedBy()))
            .containsExactly(responded1Representative1.getId(), responded1Representative2.getId());
        assertThat(unwrapElements(respondent2.getRepresentedBy()))
            .containsExactly(responded2Representative.getId());
    }

    @Test
    void shouldGetUpdatedRepresentativesWhenNewRepresentativeAdded() {
        CaseData caseDataBefore = CaseData.builder().representatives(emptyList()).build();
        CaseData caseData = buildCaseDataWithRepresentatives(EMAIL);

        List<Element<Representative>> expectedRepresentatives = createRepresentatives(EMAIL);

        List<Representative> updatedRepresentatives = representativesService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), EMAIL);

        assertThat(updatedRepresentatives).isEqualTo(unwrapElements(expectedRepresentatives));
    }

    @Test
    void shouldGetUpdatedRepresentativesWhenNewRepresentativeAddedToEmptyCaseData() {
        CaseData caseDataBefore = CaseData.builder().build();
        CaseData caseData = buildCaseDataWithRepresentatives(DIGITAL_SERVICE);

        List<Element<Representative>> expectedRepresentatives = createRepresentatives(DIGITAL_SERVICE);

        List<Representative> updatedRepresentatives = representativesService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), DIGITAL_SERVICE);

        assertThat(updatedRepresentatives).isEqualTo(unwrapElements(expectedRepresentatives));
    }

    @Test
    void shouldNotReturnAnyRepresentativesIfNewRepresentativeNotAdded() {
        CaseData caseDataBefore = buildCaseDataWithRepresentatives(EMAIL);
        CaseData caseData = buildCaseDataWithRepresentatives(EMAIL);

        List<Representative> updatedRepresentatives = representativesService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), EMAIL);

        assertThat(updatedRepresentatives).isEmpty();
    }

    @Test
    void shouldNotReturnAnyDigitalRepresentativesIfNewDigitalRepresentativeNotAdded() {
        CaseData caseDataBefore = buildCaseDataWithRepresentatives(EMAIL);
        CaseData caseData = buildCaseDataWithRepresentatives(EMAIL);

        List<Representative> updatedRepresentatives = representativesService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), DIGITAL_SERVICE);

        assertThat(updatedRepresentatives).isEmpty();
    }

    @Test
    void shouldNotReturnAnyRepresentativesIfNoRepresentativesExist() {
        CaseData caseDataBefore = CaseData.builder().build();
        CaseData caseData = CaseData.builder().build();

        List<Representative> updatedRepresentatives = representativesService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), DIGITAL_SERVICE);

        assertThat(updatedRepresentatives).isEmpty();
    }

    @Test
    void shouldGetUpdatedRepresentativesWhenRepresentativeChanged() {
        CaseData caseDataBefore = buildCaseDataWithRepresentatives(DIGITAL_SERVICE);
        CaseData caseData = buildCaseDataWithRepresentatives(EMAIL);

        List<Element<Representative>> expectedRepresentatives = createRepresentatives(EMAIL);

        List<Representative> updatedRepresentatives = representativesService.getUpdatedRepresentatives(
            caseData.getRepresentatives(), caseDataBefore.getRepresentatives(), EMAIL);

        assertThat(updatedRepresentatives).isEqualTo(unwrapElements(expectedRepresentatives));
    }

    private CaseData buildCaseDataWithRepresentatives(RepresentativeServingPreferences preference) {
        return CaseData.builder()
            .representatives(createRepresentatives(preference))
            .build();
    }

    private List<Element<Representative>> createRepresentatives(
        RepresentativeServingPreferences servingPreferences) {
        List<Element<Representative>> representatives = new ArrayList<>();
        Representative representative = Representative.builder()
            .email("abc@example.com")
            .fullName("Jon Snow")
            .servingPreferences(servingPreferences)
            .build();

        Element.<Representative>builder().value(representative);
        representatives.add(Element.<Representative>builder().value(representative).build());

        return representatives;
    }

    private static CaseData caseWithRepresentatives(Representative... representatives) {
        return CaseData.builder().representatives(wrapElements(representatives)).build();
    }

    private static Element<Representative> representativeFor(RepresentativeRole representativeRole) {
        Representative representative = Representative.builder()
            .servingPreferences(EMAIL)
            .role(representativeRole)
            .email(String.format("%s@hmcts.net", RandomStringUtils.randomAlphanumeric(5)))
            .build();
        return element(representative);
    }
}
