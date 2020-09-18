package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityValidationService.class, OrganisationService.class})
class LocalAuthorityValidationServiceTest {

    @Autowired
    private LocalAuthorityValidationService validationService;

    @MockBean
    private RequestData requestData;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @MockBean
    private LocalAuthorityService localAuthorityNameService;

    private static final String LOCAL_AUTHORITY_CODE = "SA";
    private static final String LOCAL_AUTHORITY_NAME = "Swansea City Council";
    private static final String USER_ID = "a3850cb6-36ce-4612-b8c0-da00d57f1537";
    private static final String ORGANISATION_IDENTIFIER = "123";


    @BeforeEach
    void setup() {
        given(requestData.userId()).willReturn(USER_ID);
        given(localAuthorityNameService.getLocalAuthorityCode()).willReturn(LOCAL_AUTHORITY_CODE);
        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn(LOCAL_AUTHORITY_NAME);
    }

    @Test
    void shouldNotReturnErrorsWhenToggleIsDisabledAndUserIsOnboarded() {
        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(LOCAL_AUTHORITY_NAME)).willReturn(false);
        Organisation organisation = Organisation.builder().organisationIdentifier(ORGANISATION_IDENTIFIER).build();
        given(organisationService.findOrganisation()).willReturn(organisation);

        final List<String> validationErrors = validationService.validateIfUserIsOnboarded();

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenToggleIsDisabledAndUserHasNotBeenOnboarded() {
        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(LOCAL_AUTHORITY_NAME))
            .willReturn(false);
        given(organisationService.findOrganisation()).willReturn(Organisation.builder().build());

        final List<String> errors = validationService.validateIfUserIsOnboarded();

        verify(organisationService, times(1)).findOrganisation();
        assertThat(errors).containsExactly(
            "Register for an account.",
            "You cannot start an online application until you’re fully registered.",
            "Ask your local authority’s public law administrator for help with registration.");
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
