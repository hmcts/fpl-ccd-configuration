package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationMidEventControllerTest extends AbstractControllerTest {
    private static final String ORGANISATION_IDENTIFIER = "123";

    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @MockBean
    private IdamClient client;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private AddCaseAssignedUserRolesRequest addCaseAssignedUserRolesRequest;

    @Autowired
    private SystemUpdateUserConfiguration userConfig;

    CaseInitiationMidEventControllerTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        given(client.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).willReturn(USER_AUTH_TOKEN);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        given(serviceAuthorisationApi.serviceToken(anyMap())).willReturn(SERVICE_AUTH_TOKEN);

        given(client.getUserInfo(USER_AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub("user@example.gov.uk").build());
    }

    @Test
    void shouldNotPopulateErrorsWhenToggleIsEnabled() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title",
                "caseLocalAuthority", "example"))
            .build();

        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(anyString())).willReturn(true);

        AboutToStartOrSubmitCallbackResponse actualResponse = postMidEvent(caseDetails);

        assertThat(actualResponse.getErrors().isEmpty());
    }

    @Test
    void shouldNotPopulateErrorsWhenToggleIsDisabledAndUserHasBeenOnboarded() {
        Organisation organisation = Organisation.builder().organisationIdentifier(ORGANISATION_IDENTIFIER).build();

        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(anyString())).willReturn(true);
        given(organisationService.findOrganisation()).willReturn(Optional.of(organisation));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldPopulateErrorsWhenToggleIsDisabledAndUserHasNotBeenOnboarded() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title",
                "caseLocalAuthority", "example"))
            .build();

        given(featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(anyString())).willReturn(false);

        given(organisationService.findOrganisation()).willReturn(Optional.empty());

        AboutToStartOrSubmitCallbackResponse actualResponse = postMidEvent(caseDetails);

        assertThat(actualResponse.getErrors()).containsExactly("Register for an account.",
            "You cannot start an online application until you’re fully registered.",
            "Ask your local authority’s public law administrator for help with registration.");
    }
}
