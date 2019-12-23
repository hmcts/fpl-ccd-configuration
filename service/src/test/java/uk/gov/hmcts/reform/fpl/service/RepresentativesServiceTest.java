package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
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

@ExtendWith(SpringExtension.class)
class RepresentativesServiceTest {

    private final String authentication = RandomStringUtils.randomAlphanumeric(10);

    @Mock
    private CaseService caseService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CaseDataExtractionService caseDataExtractionService;

    @InjectMocks
    private RepresentativeService representativesService;

    @AfterEach
    private void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(organisationService);
        verifyNoMoreInteractions(caseService);
    }

    @Test
    void shouldValidateBasicFieldsForSingleRepresentative() {
        Representative representative = Representative.builder().build();
        CaseData caseData = caseWithRepresentatives(representative);

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

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

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

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

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

        assertThat(validationErrors).containsExactly("Enter an email address for Representative");
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

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

        assertThat(validationErrors).containsExactly(
            "Enter a postcode for Representative",
            "Enter a valid address for Representative");
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

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

        assertThat(validationErrors).containsExactly("Enter an email address for Representative");
    }

    @Test
    void shouldValidateAccountExistenceWhenServingPreferenceIsDigitalService() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .email("test@hmcts.net")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(DIGITAL_SERVICE)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        when(organisationService.findUserByEmail(any(), any())).thenReturn(Optional.empty());

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

        assertThat(validationErrors)
            .containsExactly("Representative must already have an account with the digital service");

        verify(organisationService).findUserByEmail(authentication, representative.getEmail());
    }

    @Test
    void shouldPassValidationWhenServingPreferenceIsDigitalServiceAndAccountExists() {
        Representative representative = Representative.builder()
            .fullName("John Smith")
            .positionInACase("Solicitor")
            .email("test@hmcts.net")
            .role(LA_LEGAL_REPRESENTATIVE)
            .servingPreferences(EMAIL)
            .build();

        CaseData caseData = caseWithRepresentatives(representative);

        when(organisationService.findUserByEmail(authentication, representative.getEmail()))
            .thenReturn(Optional.of(RandomStringUtils.randomAlphanumeric(10)));

        List<String> validationErrors = representativesService.validateRepresentatives(caseData, authentication);

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

        CaseData caseData = CaseData.builder()
            .representatives(asList(
                representative1Element,
                representative2Element,
                representative3Element,
                representative4Element))
            .others(Others.builder().firstOther(other).build())
            .build();

        when(organisationService.findUserByEmail(authentication, representative1.getEmail()))
            .thenReturn(ofNullable(representative1UserId));
        when(organisationService.findUserByEmail(authentication, representative2.getEmail()))
            .thenReturn(ofNullable(representative2UserId));

        representativesService.addRepresentatives(caseData, caseId, authentication);

        verify(organisationService).findUserByEmail(authentication, representative1.getEmail());
        verify(organisationService).findUserByEmail(authentication, representative2.getEmail());

        verify(caseService).addUser(authentication, caseId.toString(), representative1UserId, Set.of(SOLICITOR));
        verify(caseService).addUser(authentication, caseId.toString(), representative2UserId, Set.of(LASOLICITOR));

        assertThat(representative1.getIdamId()).isEqualTo(representative1UserId);
        assertThat(representative2.getIdamId()).isEqualTo(representative2UserId);
    }

    @Test
    void shouldLinkRepresentativeWithRepresentable() {
        Long caseId = RandomUtils.nextLong();

        Other otherPerson1 = Other.builder().build();
        Other otherPerson2 = Other.builder().build();
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

        representativesService.addRepresentatives(caseData, caseId, authentication);

        assertThat(unwrapElements(otherPerson1.getRepresentedBy()))
            .containsExactly(person1Representative1.getId(), person1Representative2.getId());
        assertThat(unwrapElements(otherPerson2.getRepresentedBy()))
            .containsExactly(person2Representative.getId());
        assertThat(unwrapElements(respondent1.getRepresentedBy()))
            .containsExactly(responded1Representative1.getId(), responded1Representative2.getId());
        assertThat(unwrapElements(respondent2.getRepresentedBy()))
            .containsExactly(responded2Representative.getId());
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
