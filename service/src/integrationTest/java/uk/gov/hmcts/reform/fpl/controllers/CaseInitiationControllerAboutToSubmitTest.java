package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_ORG_ID;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.EPS;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerAboutToSubmitTest extends AbstractControllerTest {

    @MockBean
    private OrganisationApi organisationApi;

    CaseInitiationControllerAboutToSubmitTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        givenFplService();
        givenCurrentUserWithEmail(LOCAL_AUTHORITY_1_USER_EMAIL);
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
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(LOCAL_AUTHORITY_1_CODE);
        assertThat(caseDetails.get("caseLocalAuthorityName")).isEqualTo(LOCAL_AUTHORITY_1_NAME);
        assertThat(caseDetails.get("outsourcingPolicy")).isNull();
        assertThat(caseDetails.get("localAuthorityPolicy"))
            .isEqualTo(orgPolicy(organisation.getOrganisationIdentifier(), "[LASOLICITOR]"));
    }

    @Test
    void shouldAddLocalAuthorityAndOutsourcingPoliciesWhenCaseIsOutsourced() {
        String userOrganisationId = PRIVATE_ORG_ID;

        givenCurrentUserWithEmail(LOCAL_AUTHORITY_2_USER_EMAIL);

        Organisation organisation = testOrganisation(userOrganisationId);

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        CaseData caseData = CaseData.builder()
            .caseName("name")
            .outsourcingLAs(LOCAL_AUTHORITY_1_CODE)
            .outsourcingType(EPS)
            .build();

        Map<String, Object> caseDetails = postAboutToSubmitEvent(caseData).getData();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(LOCAL_AUTHORITY_1_CODE);
        assertThat(caseDetails.get("caseLocalAuthorityName")).isEqualTo(LOCAL_AUTHORITY_1_NAME);
        assertThat(caseDetails.get("localAuthorityPolicy"))
            .isEqualTo(orgPolicy(LOCAL_AUTHORITY_1_ID, "Test 1 Local Authority", "[LASOLICITOR]"));
        assertThat(caseDetails.get("outsourcingPolicy"))
            .isEqualTo(orgPolicy(userOrganisationId, "[EPSMANAGING]"));
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
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(LOCAL_AUTHORITY_1_CODE);
        assertThat(caseDetails.get("localAuthorityPolicy")).isNull();
        assertThat(caseDetails.get("outsourcingPolicy")).isNull();
    }

    private Map<String, Object> orgPolicy(String id, String role) {
        return Map.of("Organisation", Map.of("OrganisationID", id), "OrgPolicyCaseAssignedRole", role);
    }

    private Map<String, Object> orgPolicy(String id, String orgName, String role) {
        return Map.of("Organisation", Map.of(
            "OrganisationID", id,
            "OrganisationName", orgName
        ), "OrgPolicyCaseAssignedRole", role);
    }

}
