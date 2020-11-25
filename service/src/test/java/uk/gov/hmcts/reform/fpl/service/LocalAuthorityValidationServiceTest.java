package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LocalAuthorityValidationServiceTest {
    @Mock
    private OrganisationService organisationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Mock
    private LocalAuthorityService localAuthorityNameService;

    @InjectMocks
    private LocalAuthorityValidationService validationService;

    private static final String LOCAL_AUTHORITY_CODE = "SA";
    private static final String LOCAL_AUTHORITY_NAME = "Swansea City Council";
    private static final String ORGANISATION_IDENTIFIER = "123";

    @BeforeEach
    void setup() {
        given(localAuthorityNameService.getLocalAuthorityCode()).willReturn(LOCAL_AUTHORITY_CODE);
        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_NAME);
    }

    @Test
    void shouldNotReturnErrorsWhenToggleIsDisabledAndUserIsOnboarded() {
        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(LOCAL_AUTHORITY_NAME))
            .willReturn(false);
        Organisation organisation = Organisation.builder().organisationIdentifier(ORGANISATION_IDENTIFIER).build();
        given(organisationService.findOrganisation()).willReturn(Optional.of(organisation));

        final List<String> validationErrors = validationService.validateIfUserIsOnboarded();

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenToggleIsDisabledAndUserHasNotBeenOnboarded() {
        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(LOCAL_AUTHORITY_NAME))
            .willReturn(false);
        given(organisationService.findOrganisation()).willReturn(Optional.empty());

        final List<String> errors = validationService.validateIfUserIsOnboarded();

        verify(organisationService).findOrganisation();
        assertThat(errors).containsExactly(
            "Register for an account.",
            "You cannot start an online application until you’re fully registered.",
            "Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                + "for help with registration.");
    }

    @Test
    void shouldNotReturnErrorsWhenToggleIsEnabled() {
        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(LOCAL_AUTHORITY_NAME))
            .willReturn(true);

        List<String> errors = validationService.validateIfUserIsOnboarded();

        verifyNoInteractions(organisationService);
        assertThat(errors.isEmpty());
    }
}
