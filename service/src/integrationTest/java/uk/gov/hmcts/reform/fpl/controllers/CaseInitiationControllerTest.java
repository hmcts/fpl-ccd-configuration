package uk.gov.hmcts.reform.fpl.controllers;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdamApi idamApi;

    @Test
    void shouldAddCaseLocalAuthorityToCaseData() throws Exception {
        JSONObject caseLocalAuthority = new JSONObject();
        caseLocalAuthority.put("caseLocalAuthority", "EX");

        given(idamApi.retrieveUserDetails(AUTH_TOKEN))
            .willReturn(new UserDetails(null, "user@example.gov.uk", null, null, null));

        MvcResult response = mockMvc
            .perform(post("/callback/case-initiation")
                .header("authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(readBytes("core-case-data-store-api/empty-case-details.json")))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(response.getResponse().getContentAsString()).contains(caseLocalAuthority.toString());
    }
}
