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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ManageLegalRepresentativesValidatorTest {

    private static final String REGISTERED_EMAIL = "email";
    private static final String ANOTHER_REGISTERED_EMAIL = "email3";
    private static final LegalRepresentative VALID_LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .email(REGISTERED_EMAIL)
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
    void validateMultipleValidElement() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL)).thenReturn(Optional.of("UserId"));
        when(organisationService.findUserByEmail(ANOTHER_REGISTERED_EMAIL)).thenReturn(Optional.of("AnotherUserId"));

        List<String> actualErrors = underTest.validate(wrapElements(List.of(
            VALID_LEGAL_REPRESENTATIVE.toBuilder()
                .email(REGISTERED_EMAIL)
                .build(),
            VALID_LEGAL_REPRESENTATIVE.toBuilder()
                .email(ANOTHER_REGISTERED_EMAIL)
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
