package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("integration-test")
@WebMvcTest(ShareCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class ShareCaseControllerTest {
    private static final String USER_AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-token";

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseUserApi caseUser;

    @Autowired
    private MockMvc mockMvc;

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
}
