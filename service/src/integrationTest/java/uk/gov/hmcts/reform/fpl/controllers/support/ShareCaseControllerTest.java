package uk.gov.hmcts.reform.fpl.controllers.support;

import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("integration-test")
@WebMvcTest(ShareCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class ShareCaseControllerTest {
    private static final String USER_AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-token";

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseUserApi caseUser;

    @Autowired
    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    @Nested
    class ShareAllCasesFromGivenLocalAuthority {

        @BeforeEach
        void init() {
            Mockito.reset(caseUser, coreCaseDataService, authTokenGenerator);
        }

        @Test
        @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
        void shouldShareCases() throws Exception {
            final String localAuthority = "LA1";
            final String localAuthority2 = "LA12";
            final String user1 = UUID.randomUUID().toString();
            final String user2 = UUID.randomUUID().toString();

            CaseDetails case1 = caseDetails(localAuthority);
            CaseDetails case2 = caseDetails(localAuthority2);
            CaseDetails case3 = caseDetails(null);
            CaseDetails case4 = caseDetails("");
            CaseDetails case5 = caseDetails(localAuthority);

            given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
            given(coreCaseDataService.searchCases(any(), queryCaptor.capture()))
                .willReturn(List.of(case1, case2, case3, case4, case5));

            final MvcResult result = shareAllCases(localAuthority, List.of(user1, user2));

            assertThat(result.getResponse().getStatus()).isEqualTo(SC_OK);

            verifyAccessGranted(case1.getId().toString(), user1);
            verifyAccessGranted(case1.getId().toString(), user2);
            verifyAccessGranted(case5.getId().toString(), user1);
            verifyAccessGranted(case5.getId().toString(), user2);

            final String expectedQuery = "{'size':1000,'query':{'match':{'data.caseLocalAuthority':'LA1'}},'from':0}";

            JSONAssert.assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);

            verifyNoMoreInteractions(caseUser);
        }

        @Test
        @WithMockUser(authorities = {
            "caseworker",
            "caseworker-publiclaw",
            "caseworker-publiclaw-solicitor",
            "caseworker-publiclaw-admin",
            "caseworker-publiclaw-gatekeeper",
            "caseworker-publiclaw-judicary"})
        void shouldReturnForbiddenStatusForNonSystemUsers() throws Exception {
            final String user1 = UUID.randomUUID().toString();

            final MvcResult result = shareAllCases("LA", List.of(user1));

            assertThat(result.getResponse().getStatus()).isEqualTo(SC_FORBIDDEN);
            verifyNoInteractions(caseUser, coreCaseDataService);
        }

        @Test
        @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
        void shouldReturnBadRequestStatusWhenPayloadIsInvalid() throws Exception {

            given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

            final MvcResult result = shareCase("LA", "Not a json");

            assertThat(result.getResponse().getStatus()).isEqualTo(SC_BAD_REQUEST);
            verifyNoInteractions(caseUser, coreCaseDataService);
        }
    }

    @Nested
    class ShareGivenCase {

        @BeforeEach
        void init() {
            Mockito.reset(caseUser, authTokenGenerator);
        }

        @Test
        @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
        void shouldShareCaseWithUsers() throws Exception {
            final String caseId = UUID.randomUUID().toString();
            final String user1 = UUID.randomUUID().toString();
            final String user2 = UUID.randomUUID().toString();

            given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

            final MvcResult result = shareCase(caseId, List.of(user1, user2));

            assertThat(result.getResponse().getStatus()).isEqualTo(SC_OK);
            verifyAccessGranted(caseId, user1);
            verifyAccessGranted(caseId, user2);
        }


        @Test
        @WithMockUser(authorities = {
            "caseworker",
            "caseworker-publiclaw",
            "caseworker-publiclaw-solicitor",
            "caseworker-publiclaw-admin",
            "caseworker-publiclaw-gatekeeper",
            "caseworker-publiclaw-judicary"})
        void shouldReturnForbiddenStatusForNonSystemUsers() throws Exception {
            final String caseId = UUID.randomUUID().toString();
            final String user1 = UUID.randomUUID().toString();

            final MvcResult result = shareCase(caseId, List.of(user1));

            assertThat(result.getResponse().getStatus()).isEqualTo(SC_FORBIDDEN);
            verifyNoInteractions(caseUser);
        }

        @Test
        @WithMockUser(authorities = "caseworker-publiclaw-systemupdate")
        void shouldReturnBadRequestStatusWhenPayloadIsInvalid() throws Exception {
            final String caseId = UUID.randomUUID().toString();

            given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

            final MvcResult result = shareCase(caseId, "Not a json");

            assertThat(result.getResponse().getStatus()).isEqualTo(SC_BAD_REQUEST);
            verifyNoInteractions(caseUser);
        }
    }

    private void verifyAccessGranted(String caseId, String userId) {
        verify(caseUser).updateCaseRolesForUser(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseId, userId,
            new CaseUser(userId, Set.of("[CREATOR]", "[LASOLICITOR]")));
    }

    private MvcResult shareCase(final String caseId, List<String> userIds) throws Exception {
        final JSONObject jsonObject = new JSONObject().put("ids", new JSONArray(userIds));
        return shareCase(caseId, jsonObject.toString());
    }

    private MvcResult shareCase(final String caseId, String payload) throws Exception {
        return mockMvc
            .perform(post(format("/support/case/%s/share", caseId))
                .content(payload)
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andReturn();
    }

    private MvcResult shareAllCases(String localAuthority, List<String> userIds) throws Exception {
        final JSONObject jsonObject = new JSONObject()
            .put("localAuthority", localAuthority)
            .put("usersIds", new JSONArray(userIds));
        return shareAllCases(jsonObject.toString());
    }

    private MvcResult shareAllCases(String payload) throws Exception {
        return mockMvc
            .perform(post("/support/cases/share")
                .content(payload)
                .header("authorization", USER_AUTH_TOKEN)
                .contentType(APPLICATION_JSON))
            .andReturn();
    }

    private static CaseDetails caseDetails(String localAuthority) {
        Map<String, Object> caseData = new HashMap<>();

        if (Objects.nonNull(localAuthority)) {
            caseData.put("caseLocalAuthority", localAuthority);
        }

        return CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(caseData)
            .build();
    }
}
