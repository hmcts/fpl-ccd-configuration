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
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerTest extends AbstractControllerTest {

    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String[] USER_IDS = {"1", "2", "3"};
    private static final String CASE_ID = "12345";
    private static final Set<String> CASE_ROLES = Set.of("[LASOLICITOR]", "[CREATOR]");

    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private CaseUserApi caseUserApi;

    @MockBean
    private IdamClient client;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private SystemUpdateUserConfiguration userConfig;

    CaseInitiationControllerTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        given(client.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).willReturn(userAuthToken);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        given(serviceAuthorisationApi.serviceToken(anyMap()))
            .willReturn(SERVICE_AUTH_TOKEN);

        given(idamApi.retrieveUserInfo(userAuthToken)).willReturn(
            UserInfo.builder().sub("user@example.gov.uk").build());
    }

    @Test
    void shouldAddCaseLocalAuthorityToCaseData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("caseLocalAuthority", "example");
    }

    @Test
    void shouldPopulateErrorsInResponseWhenDomainNameIsNotFound() {
        AboutToStartOrSubmitCallbackResponse expectedResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build();

        given(idamApi.retrieveUserInfo(userAuthToken))
            .willReturn(UserInfo.builder().sub("user@email.gov.uk").build());

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(
            "core-case-data-store-api/empty-case-details.json");

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void updateCaseRolesShouldBeCalledOnceForEachUser() throws Exception {
        postSubmittedEvent(callbackRequest());

        Thread.sleep(3000);

        verifyUpdateCaseRolesWasCalledOnceForEachUser();
    }

    @Test
    void shouldContinueAddingCaseRolesToUsersAfterGrantAccessFailure() throws Exception {
        doThrow(RuntimeException.class).when(caseUserApi).updateCaseRolesForUser(
            any(), any(), any(), any(), any());

        postSubmittedEvent(callbackRequest());

        Thread.sleep(3000);

        verifyUpdateCaseRolesWasCalledOnceForEachUser();
    }

    private void verifyUpdateCaseRolesWasCalledOnceForEachUser() {
        verify(caseUserApi).updateCaseRolesForUser(
            eq(userAuthToken), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[0]),
            refEq(new CaseUser(USER_IDS[0], CASE_ROLES)));
        verify(caseUserApi).updateCaseRolesForUser(
            eq(userAuthToken), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[1]),
            refEq(new CaseUser(USER_IDS[1], CASE_ROLES)));
        verify(caseUserApi).updateCaseRolesForUser(
            eq(userAuthToken), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[2]),
            refEq(new CaseUser(USER_IDS[2], CASE_ROLES)));
    }
}
