package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_ORG_ID;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_PRIVATE_ORG_ID;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private IdamClient client;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    CaseInitiationControllerAboutToSubmitTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(client.getUserInfo(USER_AUTH_TOKEN))
            .willReturn(UserInfo.builder().sub("user@example.gov.uk").build());
    }

    @Test
    void shouldAddLocalAuthorityPolicy() {
        final Organisation organisation = testOrganisation();

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        CaseData caseData = CaseData.builder()
            .caseName("name")
            .build();

        Map<String, Object> caseDetails = postAboutToSubmitEvent(caseData).getData();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(DEFAULT_LA_CODE);
        assertThat(caseDetails.get("caseLocalAuthorityName")).isEqualTo(DEFAULT_LA_NAME);
        assertThat(caseDetails.get("outsourcingPolicy")).isNull();
        assertThat(caseDetails.get("localAuthorityPolicy"))
            .isEqualTo(orgPolicy(organisation.getOrganisationIdentifier(), "[LASOLICITOR]"));
    }

    @Test
    void shouldAddLocalAuthorityAndOutsourcingPoliciesWhenCaseIsOutsourced() {
        Organisation organisation = testOrganisation(DEFAULT_PRIVATE_ORG_ID);

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        CaseData caseData = CaseData.builder()
            .caseName("name")
            .outsourcingLAs(DEFAULT_LA_CODE)
            .build();

        Map<String, Object> caseDetails = postAboutToSubmitEvent(caseData).getData();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(DEFAULT_LA_CODE);
        assertThat(caseDetails.get("caseLocalAuthorityName")).isEqualTo(DEFAULT_LA_NAME);
        assertThat(caseDetails.get("localAuthorityPolicy"))
            .isEqualTo(orgPolicy(DEFAULT_LA_ORG_ID, "[LASOLICITOR]"));
        assertThat(caseDetails.get("outsourcingPolicy"))
            .isEqualTo(orgPolicy(DEFAULT_PRIVATE_ORG_ID, "[EPSMANAGING]"));
    }

    @Test
    void shouldNotAddOrganisationPolicyIfUserNotRegisteredInAnyOrganisation() {
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .willThrow(feignException(SC_FORBIDDEN));

        CaseData caseData = CaseData.builder()
            .caseName("name")
            .build();

        Map<String, Object> caseDetails = postAboutToSubmitEvent(caseData).getData();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(DEFAULT_LA_CODE);
        assertThat(caseDetails.get("localAuthorityPolicy")).isNull();
        assertThat(caseDetails.get("outsourcingPolicy")).isNull();
    }

    @Test
    void shouldReturnErrorsWhenUserDomainNotRecognised() {
        given(client.getUserInfo(USER_AUTH_TOKEN))
            .willReturn(UserInfo.builder().sub("user@unknown.domain").build());

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(CaseData.builder().build());

        assertThat(actualResponse.getErrors())
            .containsExactly("The email address was not linked to a known Local Authority");
    }

    private Map<String, Object> orgPolicy(String id, String role) {
        return Map.of("Organisation", Map.of("OrganisationID", id), "OrgPolicyCaseAssignedRole", role);
    }

}
