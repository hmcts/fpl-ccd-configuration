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

    public static final String REGISTERED_EMAIL = "email";
    public static final String NON_REGISTERED_EMAIL = "email2";

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ManageLegalRepresentativesValidator underTest;

    @Test
    void validateValidElement() {
        when(organisationService.findUserByEmail(REGISTERED_EMAIL)).thenReturn(Optional.of("UserId"));

        List<String> actual = underTest.validate(wrapElements(List.of(LegalRepresentative.builder()
            .fullName("fullName")
            .organisation("organisation")
            .role(EXTERNAL_LA_BARRISTER)
            .email(REGISTERED_EMAIL)
            .telephoneNumber("2343252345")
            .build())));

        assertThat(actual).isEmpty();
    }

    @Test
    void validateValidElementButUnregisteredEmail() {
        when(organisationService.findUserByEmail(NON_REGISTERED_EMAIL)).thenReturn(Optional.empty());

        List<String> actual = underTest.validate(wrapElements(List.of(LegalRepresentative.builder()
            .fullName("fullName")
            .organisation("organisation")
            .role(EXTERNAL_LA_BARRISTER)
            .email(NON_REGISTERED_EMAIL)
            .telephoneNumber("2343252345")
            .build())));

        assertThat(actual).isEqualTo(List.of(
            "email2 must already have an account with the digital service"
        ));
    }

    @Test
    void validateMissingElements() {

        List<String> actual = underTest.validate(wrapElements(List.of(LegalRepresentative.builder().build())));

        assertThat(actual).isEqualTo(List.of(
            "Enter a full name",
            "Select a role in the case",
            "Enter an organisation name",
            "Enter an email address",
            "Enter a phone number"
        ));
    }
}
