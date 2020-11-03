package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole.EXTERNAL_LA_BARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ManageLegalRepresentativesValidatorTest {

    private static final String REGISTERED_EMAIL = "email";
    private static final LegalRepresentative VALID_LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName("fullName")
        .organisation("organisation")
        .role(EXTERNAL_LA_BARRISTER)
        .email(REGISTERED_EMAIL)
        .telephoneNumber("2343252345")
        .build();
    private static final String NON_REGISTERED_EMAIL = "email2";

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ManageLegalRepresentativesValidator underTest;

    @Test
    void validateValidElement() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL)).thenReturn(Optional.of("UserId"));

        List<String> actualErrors = underTest.validate(wrapElements(List.of(VALID_LEGAL_REPRESENTATIVE)));

        assertThat(actualErrors).isEmpty();
    }

    @Test
    void validateValidElementMissingOptionals() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL)).thenReturn(Optional.of("UserId"));

        List<String> actualErrors = underTest.validate(wrapElements(List.of(
            VALID_LEGAL_REPRESENTATIVE.toBuilder()
                .telephoneNumber(null)
                .build()
        )));

        assertThat(actualErrors).isEmpty();
    }

    @Test
    void validateValidElementButUnregisteredEmail() {
        when(organisationService.findUserByEmail(NON_REGISTERED_EMAIL)).thenReturn(Optional.empty());

        List<String> actualErrors = underTest.validate(wrapElements(List.of(VALID_LEGAL_REPRESENTATIVE.toBuilder()
            .email(NON_REGISTERED_EMAIL)
            .build())));

        assertThat(actualErrors).containsExactly(
            "Email address for Legal representative is not registered on the system. "
                + "They can register at "
                + "https://manage-org.platform.hmcts.net/register-org/register"
        );
    }

    @Test
    void validateMissingElements() {

        List<String> actualErrors = underTest.validate(wrapElements(List.of(LegalRepresentative.builder().build())));

        assertThat(actualErrors).containsExactly(
            "Enter a full name for Legal representative",
            "Select a role for Legal representative",
            "Enter an organisation for Legal representative",
            "Enter an email address for Legal representative"
        );
    }

    @Test
    void validateMissingElementsForMultipleLegalRepresentatives() {

        List<String> actualErrors = underTest.validate(wrapElements(List.of(
            LegalRepresentative.builder().build(),
            LegalRepresentative.builder().build()))
        );

        assertThat(actualErrors).containsExactly(
            "Enter a full name for Legal representative 1",
            "Select a role for Legal representative 1",
            "Enter an organisation for Legal representative 1",
            "Enter an email address for Legal representative 1",
            "Enter a full name for Legal representative 2",
            "Select a role for Legal representative 2",
            "Enter an organisation for Legal representative 2",
            "Enter an email address for Legal representative 2"
        );
    }

    @Test
    void validateMultipleValidElementButUnregisteredEmail() {
        when(organisationService.findUserByEmail(NON_REGISTERED_EMAIL)).thenReturn(Optional.empty());

        List<String> actualErrors = underTest.validate(wrapElements(List.of(
            VALID_LEGAL_REPRESENTATIVE.toBuilder()
                .email(NON_REGISTERED_EMAIL)
                .build(),
            VALID_LEGAL_REPRESENTATIVE.toBuilder()
                .email(NON_REGISTERED_EMAIL)
                .build()
        )));

        assertThat(actualErrors).containsExactly(
            "Email address for Legal representative 1 is not registered on the system. "
                + "They can register at "
                + "https://manage-org.platform.hmcts.net/register-org/register",
            "Email address for Legal representative 2 is not registered on the system. "
                + "They can register at "
                + "https://manage-org.platform.hmcts.net/register-org/register"
        );
    }
}
