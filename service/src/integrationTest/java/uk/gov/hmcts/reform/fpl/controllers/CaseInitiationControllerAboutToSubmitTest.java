package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.Map;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_COURT_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_COURT_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_ORG_ID;
import static uk.gov.hmcts.reform.fpl.enums.OutsourcingType.EPS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerAboutToSubmitTest extends AbstractCallbackTest {

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

        final Court expectedCourt = Court.builder()
            .code(LOCAL_AUTHORITY_1_COURT_ID)
            .name(LOCAL_AUTHORITY_1_COURT_NAME)
            .email(LOCAL_AUTHORITY_1_COURT_EMAIL)
            .build();

        Map<String, Object> caseDetails = postAboutToSubmitEvent(caseData).getData();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(LOCAL_AUTHORITY_1_CODE);
        assertThat(caseDetails.get("caseLocalAuthorityName")).isEqualTo(LOCAL_AUTHORITY_1_NAME);
        assertThat(caseDetails.get("outsourcingPolicy")).isNull();
        assertThat(caseDetails.get("localAuthorityPolicy"))
            .isEqualTo(orgPolicy(organisation.getOrganisationIdentifier(), "[LASOLICITOR]"));
        assertThat(caseDetails.get("court")).isEqualTo(toMap(expectedCourt));
        assertThat(caseDetails.get("multiCourt")).isNull();
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

        final Court expectedCourt = Court.builder()
            .code(LOCAL_AUTHORITY_1_COURT_ID)
            .name(LOCAL_AUTHORITY_1_COURT_NAME)
            .email(LOCAL_AUTHORITY_1_COURT_EMAIL)
            .build();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(LOCAL_AUTHORITY_1_CODE);
        assertThat(caseDetails.get("caseLocalAuthorityName")).isEqualTo(LOCAL_AUTHORITY_1_NAME);
        assertThat(caseDetails.get("localAuthorityPolicy"))
            .isEqualTo(orgPolicy(LOCAL_AUTHORITY_1_ID, "Test 1 Local Authority", "[LASOLICITOR]"));
        assertThat(caseDetails.get("outsourcingPolicy"))
            .isEqualTo(orgPolicy(userOrganisationId, "[EPSMANAGING]"));
        assertThat(caseDetails.get("court")).isEqualTo(toMap(expectedCourt));
        assertThat(caseDetails.get("multiCourt")).isNull();
    }

    @Test
    void shouldNotAddOrganisationPolicyIfUserNotRegisteredInAnyOrganisation() {
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .willThrow(feignException(SC_FORBIDDEN));

        CaseData caseData = CaseData.builder()
            .caseName("name")
            .build();

        final Court expectedCourt = Court.builder()
            .code(LOCAL_AUTHORITY_1_COURT_ID)
            .name(LOCAL_AUTHORITY_1_COURT_NAME)
            .email(LOCAL_AUTHORITY_1_COURT_EMAIL)
            .build();

        Map<String, Object> caseDetails = postAboutToSubmitEvent(caseData).getData();

        assertThat(caseDetails.get("caseName")).isEqualTo(caseData.getCaseName());
        assertThat(caseDetails.get("caseLocalAuthority")).isEqualTo(LOCAL_AUTHORITY_1_CODE);
        assertThat(caseDetails.get("localAuthorityPolicy")).isNull();
        assertThat(caseDetails.get("outsourcingPolicy")).isNull();
        assertThat(caseDetails.get("court")).isEqualTo(toMap(expectedCourt));
        assertThat(caseDetails.get("multiCourt")).isNull();
    }

    @Test
    void shouldNotSetCourtWhenMultipleCourtsAvailable() {
        givenCurrentUserWithEmail(LOCAL_AUTHORITY_3_USER_EMAIL);

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(testOrganisation());

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(CaseData.builder().build()));

        assertThat(updatedCaseData.getMultiCourts()).isEqualTo(YES);
        assertThat(updatedCaseData.getCourt()).isNull();
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
