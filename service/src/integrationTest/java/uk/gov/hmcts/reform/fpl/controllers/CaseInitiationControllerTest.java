package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String[] USER_IDS = {"1", "2", "3"};
    private static final String CASE_ID = "12345";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> caseRoles = Set.of("[LASOLICITOR]", "[CREATOR]");

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setup() {
        given(client.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).willReturn(AUTH_TOKEN);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        given(serviceAuthorisationApi.serviceToken(anyMap()))
            .willReturn(SERVICE_AUTH_TOKEN);

        given(idamApi.retrieveUserInfo(AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub("user@example.gov.uk").build());
    }

    @Test
    void shouldAddCaseLocalAuthorityToCaseData() throws Exception {
        CallbackRequest request = CallbackRequest.builder().caseDetails(CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("caseName", "title")
                .build()).build())
            .build();

        MvcResult response = mockMvc
            .perform(post("/callback/case-initiation/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse callbackResponse = MAPPER.readValue(response.getResponse()
            .getContentAsByteArray(), AboutToStartOrSubmitCallbackResponse.class);

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("caseLocalAuthority", "example");
    }

    @Test
    void shouldPopulateErrorsInResponseWhenDomainNameIsNotFound() throws Exception {
        AboutToStartOrSubmitCallbackResponse expectedResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(ImmutableList.<String>builder()
                .add("The email address was not linked to a known Local Authority")
                .build())
            .build();

        given(idamApi.retrieveUserInfo(AUTH_TOKEN))
            .willReturn(UserInfo.builder().sub("user@email.gov.uk").build());

        MvcResult response = mockMvc
            .perform(post("/callback/case-initiation/about-to-submit")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("core-case-data-store-api/empty-case-details.json")))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).isEqualTo(MAPPER.writeValueAsString(expectedResponse));
    }

    @Test
    void updateCaseRolesShouldBeCalledOnceForEachUser() throws Exception {
        CallbackRequest request = callbackRequest();

        performResponseCallBackSubmitted(request);

        Thread.sleep(3000);

        verifyUpdateCaseRolesWasCalledOnceForEachUser();
    }

    @Test
    void shouldContinueAddingCaseRolesToUsersAfterGrantAccessFailure() throws Exception {
        doThrow(RuntimeException.class).when(caseUserApi).updateCaseRolesForUser(
            any(), any(), any(), any(), any());

        CallbackRequest request = callbackRequest();

        performResponseCallBackSubmitted(request);

        Thread.sleep(3000);

        verifyUpdateCaseRolesWasCalledOnceForEachUser();
    }

    private void verifyUpdateCaseRolesWasCalledOnceForEachUser() {
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[0]),
            refEq(new CaseUser(USER_IDS[0], caseRoles)));
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[1]),
            refEq(new CaseUser(USER_IDS[1], caseRoles)));
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[2]),
            refEq(new CaseUser(USER_IDS[2], caseRoles)));
    }

    private void performResponseCallBackSubmitted(CallbackRequest request) throws Exception {
        mockMvc
            .perform(post("/callback/case-initiation/submitted")
                .header("authorization", AUTH_TOKEN)
                .header("user-id", USER_IDS[0])
                .contentType(MediaType.APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(request)))
            .andExpect(status().isOk()).andReturn();
    }
}
